package pl.srozga.gluqalc_api.component.diabetes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.common.CombinedInsulinCalculationMethod;
import pl.srozga.gluqalc_api.common.InsulinDeliveryMethod;
import pl.srozga.gluqalc_api.dto.internal.DiabetesCalcDataDto;
import pl.srozga.gluqalc_api.dto.response.MealEntryResponse;
import pl.srozga.gluqalc_api.entity.MealEntry;
import pl.srozga.gluqalc_api.entity.UserProfile;
import pl.srozga.gluqalc_api.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiabetesCalculator {
    private final ObjectMapper objectMapper;

    private static final BigDecimal CU_DIVISOR = new BigDecimal("10");
    private static final BigDecimal FPU_DIVISOR = new BigDecimal("100");
    private static final BigDecimal PROTEIN_KCAL = new BigDecimal("4");
    private static final BigDecimal FAT_KCAL = new BigDecimal("9");
    private static final BigDecimal SIERADZKI_EXTENDED_PERCENTAGE = new BigDecimal("0.30");
    private static final BigDecimal SIERADZKI_RICH_MEAL_THRESHOLD_WBT = new BigDecimal("2");
    private static final int SIERADZKI_DURATION_MINUTES = 240;
    private static final BigDecimal MIN_PANKOWSKA_WBT = new BigDecimal("1");
    private static final BigDecimal PANKOWSKA_REDUCTION = new BigDecimal("0.25");
    private static final BigDecimal ADDITIONAL_REDUCTION_MIN = new BigDecimal("0.25");
    private static final BigDecimal ADDITIONAL_REDUCTION_MAX = new BigDecimal("0.40");
    private static final int PANKOWSKA_MIN_DURATION_MINUTES = 120;
    private static final int PANKOWSKA_MAX_DURATION_MINUTES = 240;

    public DiabetesCalcDataDto calculateForMeal(MealEntry entry, UserProfile profile, LocalTime time) {
        if (entry == null)
            return DiabetesCalcDataDto.empty();

        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;

        if (entry.getCarbohydrates() != null)
            totalCarbs = entry.getCarbohydrates();
        if (entry.getProtein() != null)
            totalProtein = entry.getProtein();
        if (entry.getFat() != null)
            totalFat = entry.getFat();
        if (entry.getFiber() != null)
            totalFiber = entry.getFiber();

        Integer gi = entry.getGlycemicIndex();

        return calculateInternal(totalCarbs, totalProtein, totalFat, totalFiber, gi != null ? BigDecimal.valueOf(gi) : null, profile, time);
    }

    public DiabetesCalcDataDto calculateForCategory(List<MealEntryResponse> entries, UserProfile profile, LocalTime time) {
        if (entries == null || entries.isEmpty())
            return DiabetesCalcDataDto.empty();

        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;

        for (MealEntryResponse entry : entries) {
            BigDecimal carbs = entry.nutrition().carbohydrates() != null ? entry.nutrition().carbohydrates() : BigDecimal.ZERO;
            totalCarbs = totalCarbs.add(carbs);

            if (entry.nutrition().protein() != null)
                totalProtein = totalProtein.add(entry.nutrition().protein());
            if (entry.nutrition().fat() != null)
                totalFat = totalFat.add(entry.nutrition().fat());
            if (entry.nutrition().fiber() != null)
                totalFiber = totalFiber.add(entry.nutrition().fiber());
        }

        BigDecimal averageGi = calculateAverageGlycemicIndex(entries);

        return calculateInternal(totalCarbs, totalProtein, totalFat, totalFiber, averageGi, profile, time);
    }

    public DiabetesCalcDataDto calculate(BigDecimal carbs, BigDecimal protein, BigDecimal fat, BigDecimal fiber, BigDecimal glycemicIndex, UserProfile profile, LocalTime time) {
        if (profile == null)
            return DiabetesCalcDataDto.empty();

        return calculateInternal(
                carbs != null ? carbs : BigDecimal.ZERO,
                protein != null ? protein : BigDecimal.ZERO,
                fat != null ? fat : BigDecimal.ZERO,
                fiber != null ? fiber : BigDecimal.ZERO,
                glycemicIndex != null ? glycemicIndex : BigDecimal.ZERO,
                profile,
                time
        );
    }

    public BigDecimal calculateAverageGlycemicIndex(List<MealEntryResponse> entries) {
        if (entries == null || entries.isEmpty())
            return null;

        BigDecimal weightedGiSum = BigDecimal.ZERO;
        BigDecimal carbsWithGi = BigDecimal.ZERO;

        for (MealEntryResponse entry : entries) {
            BigDecimal carbs = entry.nutrition().carbohydrates() != null ? entry.nutrition().carbohydrates() : BigDecimal.ZERO;
            Integer gi = entry.nutrition().glycemicIndex();

            if (gi != null && carbs.compareTo(BigDecimal.ZERO) > 0) {
                weightedGiSum = weightedGiSum.add(carbs.multiply(BigDecimal.valueOf(gi)));
                carbsWithGi = carbsWithGi.add(carbs);
            }
        }

        if (carbsWithGi.compareTo(BigDecimal.ZERO) > 0)
            return weightedGiSum.divide(carbsWithGi, 0, RoundingMode.HALF_UP);

        return null;
    }

    private DiabetesCalcDataDto calculateInternal(BigDecimal carbs, BigDecimal protein, BigDecimal fat, BigDecimal fiber, BigDecimal glycemicIndex, UserProfile profile, LocalTime time) {
        BigDecimal cu = carbs.divide(CU_DIVISOR, 2, RoundingMode.HALF_UP);

        BigDecimal proteinKcal = protein.multiply(PROTEIN_KCAL);
        BigDecimal fatKcal = fat.multiply(FAT_KCAL);
        BigDecimal fpu = (proteinKcal.add(fatKcal)).divide(FPU_DIVISOR, 2, RoundingMode.HALF_UP);

        BigDecimal icr = getHourlyCarbRatio(profile, time);
        BigDecimal ifpRatio = profile.getInsulinFatProteinRatio() != null ? profile.getInsulinFatProteinRatio() : BigDecimal.ZERO;
        InsulinDeliveryMethod insulinDeliveryMethod = profile.getInsulinDeliveryMethod();
        CombinedInsulinCalculationMethod calculationMethod = profile.getCombinedInsulinCalculationMethod() != null
            ? profile.getCombinedInsulinCalculationMethod()
            : CombinedInsulinCalculationMethod.PANKOWSKA;

        return calculateDoseAndDuration(cu, fpu, icr, ifpRatio, glycemicIndex, insulinDeliveryMethod, calculationMethod);
    }

    private DiabetesCalcDataDto calculateDoseAndDuration(
            BigDecimal cu,
            BigDecimal fpu,
            BigDecimal icr,
            BigDecimal ifpRatio,
            BigDecimal glycemicIndex,
            InsulinDeliveryMethod insulinDeliveryMethod,
            CombinedInsulinCalculationMethod calculationMethod
    ) {
        BigDecimal carbDose = cu.multiply(icr).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unadjustedFatProteinDose = fpu.multiply(ifpRatio);
        BigDecimal fatProteinDose;

        int durationMinutes = 0;
        if (calculationMethod == CombinedInsulinCalculationMethod.SIERADZKI) {
            fatProteinDose = fpu.compareTo(BigDecimal.ZERO) > 0
                    ? carbDose.multiply(SIERADZKI_EXTENDED_PERCENTAGE)
                    : BigDecimal.ZERO;
            durationMinutes = fatProteinDose.compareTo(BigDecimal.ZERO) > 0 ? SIERADZKI_DURATION_MINUTES : 0;
        } else if (fpu.compareTo(MIN_PANKOWSKA_WBT) < 0) {
            fatProteinDose = BigDecimal.ZERO;
        } else {
            fatProteinDose = unadjustedFatProteinDose.multiply(BigDecimal.ONE.subtract(PANKOWSKA_REDUCTION));
            int calculatedMinutes = (int) (fpu.doubleValue() * 60) + 120;
            durationMinutes = Math.min(Math.max(calculatedMinutes, PANKOWSKA_MIN_DURATION_MINUTES), PANKOWSKA_MAX_DURATION_MINUTES);
            if (fatProteinDose.compareTo(BigDecimal.ZERO) == 0)
                durationMinutes = 0;
        }

        BigDecimal totalDose = carbDose.add(fatProteinDose).setScale(2, RoundingMode.HALF_UP);
        fatProteinDose = fatProteinDose.setScale(2, RoundingMode.HALF_UP);

        String description = generateTherapeuticAdvice(
                carbDose,
                fatProteinDose,
                unadjustedFatProteinDose,
                durationMinutes,
                glycemicIndex,
                insulinDeliveryMethod,
                calculationMethod,
                fpu
        );

        return new DiabetesCalcDataDto(
                cu.setScale(1, RoundingMode.HALF_UP),
                fpu.setScale(1, RoundingMode.HALF_UP),
                carbDose,
                fatProteinDose,
                totalDose,
                durationMinutes,
                description
        );
    }

    private BigDecimal getHourlyCarbRatio(UserProfile profile, LocalTime time) {
        if (profile.getHourlyCarbRatioJson() == null)
            return BigDecimal.ZERO;
        try {
            Map<Integer, BigDecimal> map = objectMapper.readValue(
                    profile.getHourlyCarbRatioJson(),
                    new TypeReference<>() {}
            );

            TreeMap<Integer, BigDecimal> sortedMap = new TreeMap<>(map);
            Map.Entry<Integer, BigDecimal> entry = sortedMap.floorEntry(time.getHour());
            if (entry == null)
                return sortedMap.lastEntry() != null ? sortedMap.lastEntry().getValue() : BigDecimal.ZERO;

            return entry.getValue();
        } catch (Exception e) {
            log.error("Error parsing ICR for user {}", profile.getId());
            throw new DomainValidationException("Invalid ICR configuration");
        }
    }

    private String generateTherapeuticAdvice(
            BigDecimal carbDose,
            BigDecimal extendedDose,
            BigDecimal unadjustedExtendedDose,
            int durationMinutes,
            BigDecimal gi,
            InsulinDeliveryMethod insulinDeliveryMethod,
            CombinedInsulinCalculationMethod calculationMethod,
            BigDecimal fpu
    ) {
        StringBuilder advice = new StringBuilder();
        boolean hasCarbs = carbDose.compareTo(BigDecimal.ZERO) > 0;
        boolean hasExtended = extendedDose.compareTo(BigDecimal.ZERO) > 0;

        if (!hasCarbs && !hasExtended) {
            return "No insulin required for this meal. This does not include current glucose, active insulin, or physical activity.";
        }

        if (insulinDeliveryMethod == InsulinDeliveryMethod.PEN)
            buildPenAdvice(advice, gi, hasCarbs, hasExtended, extendedDose, calculationMethod);
        else if (insulinDeliveryMethod == InsulinDeliveryMethod.PUMP)
            buildPumpAdvice(advice, gi, hasCarbs, hasExtended, carbDose, extendedDose, durationMinutes);
        else
            return "No advice available for the selected insulin delivery method.";

        appendCalculationMethodAdvice(advice, calculationMethod, extendedDose, unadjustedExtendedDose, fpu, insulinDeliveryMethod);
        advice.append(" This does not include current glucose, active insulin, or physical activity; apply only according to the user's individual needs.");
        return advice.toString().trim();
    }

    private void appendCalculationMethodAdvice(
            StringBuilder advice,
            CombinedInsulinCalculationMethod calculationMethod,
            BigDecimal extendedDose,
            BigDecimal unadjustedExtendedDose,
            BigDecimal fpu,
            InsulinDeliveryMethod insulinDeliveryMethod
    ) {
        if (calculationMethod == CombinedInsulinCalculationMethod.PANKOWSKA && extendedDose.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal doseAtMaxReduction = unadjustedExtendedDose.multiply(new BigDecimal("0.60"));

            advice.append(String.format(
                    " Depending on individual response, a reduction of up to 40%% may be needed (approx. %sU).",
                    formatDose(doseAtMaxReduction)
            ));
        } else if (calculationMethod == CombinedInsulinCalculationMethod.SIERADZKI && extendedDose.compareTo(BigDecimal.ZERO) > 0) {
            if (insulinDeliveryMethod == InsulinDeliveryMethod.PEN) {
                advice.append(" A pen cannot deliver this as one continuous extended bolus; timing and splitting must follow the user's needs.");
            }

            if (fpu.compareTo(SIERADZKI_RICH_MEAL_THRESHOLD_WBT) >= 0) {
                advice.append(" This is a heavily fat/protein-rich meal. Individual increase up to 70% and extension up to 5-6 hours may be necessary depending on your experience.");
            }
        }
    }

    private String formatDose(BigDecimal dose) {
        return dose.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private StringBuilder buildPumpAdvice(
        StringBuilder advice,
        BigDecimal gi,
        boolean hasCarbs,
        boolean hasExtended,
        BigDecimal carbDose,
        BigDecimal extendedDose,
        int durationMinutes
    ) {
        if (hasCarbs) {
            advice.append("For rapid-acting insulin give bolus 15-20 min before meal; for ultra-rapid insulin 2-10 min before meal.");
        }

        if (hasCarbs && hasExtended) {
            String timeStr = formatDuration(durationMinutes);
            advice.append(String.format(" Meal with FPU: use dual/combo bolus - %sU now and %sU extended over %s.",
                    carbDose, extendedDose, timeStr));
        } else if (hasExtended) {
            String timeStr = formatDuration(durationMinutes);
            advice.append(String.format(" Use extended (square) bolus: %sU over %s.", extendedDose, timeStr));
        } else if (hasCarbs) {
            if (gi != null && gi.compareTo(new BigDecimal("55")) < 0) {
                advice.append(" Low GI meal: consider extending the bolus (square wave) to match slow absorption.");
            } else {
                advice.append(" Standard normal bolus is sufficient.");
            }
        }

        return advice;
    }

    private StringBuilder buildPenAdvice(
        StringBuilder advice,
        BigDecimal gi,
        boolean hasCarbs,
        boolean hasExtended,
        BigDecimal extendedDose,
        CombinedInsulinCalculationMethod calculationMethod
    ) {
        if (hasCarbs && gi != null && gi.compareTo(BigDecimal.ZERO) > 0) {
            if (gi.compareTo(new BigDecimal("70")) >= 0) {
                advice.append("High GI meal: pre-bolus is recommended (rapid insulin typically 15-20 min before meal, ultra-rapid 2-10 min before meal).");
            } else if (gi.compareTo(new BigDecimal("35")) < 0) {
                advice.append("Very low GI meal: consider giving insulin during the meal.");
            } else if (gi.compareTo(new BigDecimal("55")) < 0) {
                advice.append("Low GI meal: consider giving insulin at meal start or shortly after first bites.");
            } else {
                advice.append("Medium GI meal: use standard timing, usually close to meal start.");
            }
        }

        if (hasCarbs && hasExtended && !advice.isEmpty()) {
            advice.append(" ");
        }

        if (hasExtended) {
            String timing = calculationMethod == CombinedInsulinCalculationMethod.PANKOWSKA ? "2-4 hours" : "according to the individual plan";
            advice.append(String.format("High FPU meal: consider a second injection %s after meal (about %sU).", timing, extendedDose));
        }

        return advice;
    }

    private String formatDuration(int durationMinutes) {
        int hours = durationMinutes / 60;
        int mins = durationMinutes % 60;
        return mins > 0 ? String.format("%dh %dmin", hours, mins) : String.format("%dh", hours);
    }
}
