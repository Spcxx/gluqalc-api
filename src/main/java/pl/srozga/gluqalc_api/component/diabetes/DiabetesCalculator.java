package pl.srozga.gluqalc_api.component.diabetes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.common.InsulinFatProteinStrategy;
import pl.srozga.gluqalc_api.dto.internal.DiabetesCalcDataDto;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.response.MealEntryResponse;
import pl.srozga.gluqalc_api.entity.MealEntry;
import pl.srozga.gluqalc_api.entity.UserProfile;

import java.math.BigDecimal;
import java.math.MathContext;
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
    private static final BigDecimal MIN_EXTENDED_DOSE_THRESHOLD = new BigDecimal("1");

    public DiabetesCalcDataDto calculateForMeal(MealEntry entry, UserProfile profile, LocalTime time) {
        if (entry == null)
            return DiabetesCalcDataDto.empty();

        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;

        if (entry.getCarbohydrates() != null)
            totalCarbs = entry.getCarbohydrates();
        if (entry.getProtein() != null)
            totalProtein = entry.getProtein();
        if (entry.getFat() != null)
            totalFat = entry.getFat();

        return calculateInternal(totalCarbs, totalProtein, totalFat, profile, time);
    }

    public DiabetesCalcDataDto calculateForCategory(List<MealEntryResponse> entries, UserProfile profile, LocalTime time) {
        if (entries == null || entries.isEmpty())
            return DiabetesCalcDataDto.empty();

        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;

        for (MealEntryResponse entry : entries) {
            if (entry.nutrition().carbohydrates() != null)
                totalCarbs = totalCarbs.add(entry.nutrition().carbohydrates());
            if (entry.nutrition().protein() != null)
                totalProtein = totalProtein.add(entry.nutrition().protein());
            if (entry.nutrition().fat() != null)
                totalFat = totalFat.add(entry.nutrition().fat());
        }

        return calculateInternal(totalCarbs, totalProtein, totalFat, profile, time);
    }

    public DiabetesCalcDataDto calculate(BigDecimal carbs, BigDecimal protein, BigDecimal fat, UserProfile profile, LocalTime time) {
        if (profile == null)
            return DiabetesCalcDataDto.empty();

        return calculateInternal(
                carbs != null ? carbs : BigDecimal.ZERO,
                protein != null ? protein : BigDecimal.ZERO,
                fat != null ? fat : BigDecimal.ZERO,
                profile,
                time
        );
    }

    private DiabetesCalcDataDto calculateInternal(BigDecimal carbs, BigDecimal protein, BigDecimal fat, UserProfile profile, LocalTime time) {
        BigDecimal cu = carbs.divide(CU_DIVISOR, 2, RoundingMode.HALF_UP);
        BigDecimal proteinKcal = protein.multiply(PROTEIN_KCAL);
        BigDecimal fatKcal = fat.multiply(FAT_KCAL);
        BigDecimal fpu = (proteinKcal.add(fatKcal)).divide(FPU_DIVISOR, 2, RoundingMode.HALF_UP);

        if (profile == null || profile.getIfpStrategy() == null || profile.getIfpStrategy() == InsulinFatProteinStrategy.NONE)
            return new DiabetesCalcDataDto(cu, fpu, null, null, null, null, "No insulin strategy configured");

        BigDecimal icr = getHourlyCarbRatio(profile, time);
        BigDecimal ifpRatio = profile.getInsulinFatProteinRatio() != null ? profile.getInsulinFatProteinRatio() : BigDecimal.ZERO;

        BigDecimal carbDose = cu.multiply(icr).setScale(2, RoundingMode.HALF_UP);

        return calculateDoseAndDuration(cu, fpu, carbDose, ifpRatio, profile.getIfpStrategy());
    }

    private DiabetesCalcDataDto calculateDoseAndDuration(
            BigDecimal cu,
            BigDecimal fpu,
            BigDecimal carbDose,
            BigDecimal ifpRatio,
            InsulinFatProteinStrategy strategy
    ) {
        BigDecimal fatProteinDose = BigDecimal.ZERO;
        int durationMinutes = 0;
        String description = "";

        switch (strategy) {
            case WBT_STANDARD:
                // dose: fpu * ratio
                // time: fpu + 2 hours
                fatProteinDose = fpu.multiply(ifpRatio);
                if (fpu.compareTo(BigDecimal.ZERO) > 0) {
                    int calculatedMinutes = (int) (fpu.doubleValue() * 60) + 120;
                    durationMinutes = Math.min(calculatedMinutes, 480);
                } else {
                    durationMinutes = 0;
                }
                description = "Standard Warsaw FPU Dose based on caloric content (100kcal unit). Time is FPU-based plus 2h.";
                break;

            case PANKOWSKA_ALGORITHM:
                // dose: fpu * ratio
                // time: 3h for FPU <=1, 4h for FPU <=2, 5h for FPU <=3, 8h for FPU >3
                fatProteinDose = fpu.multiply(ifpRatio);
                double wbtVal = fpu.doubleValue();

                if (wbtVal <= 0)
                    durationMinutes = 0;
                else if (wbtVal <= 1.0)
                    durationMinutes = 180; // 3h
                else if (wbtVal <= 2.0)
                    durationMinutes = 240; // 4h
                else if (wbtVal <= 3.0)
                    durationMinutes = 300; // 5h
                else
                    durationMinutes = 480; // 8h

                description = "Pankowska Algorithm - strict duration based on FPU.";
                break;

            case FPU_STANDARD:
                // dose: fpu * ratio
                // time: standard 3-4h
                fatProteinDose = fpu.multiply(ifpRatio);
                durationMinutes = (fpu.doubleValue() > 0) ? 240 : 0;
                description = "FPU: International Fat-Protein Unit method.";
                break;

            case PERCENTAGE_ADDON:
                // dose: add a percentage of the carb dose (e.g., 20% more insulin than carb dose)
                // time: standard 3h or based on carb dose
                fatProteinDose = carbDose.multiply(ifpRatio);
                durationMinutes = (fatProteinDose.compareTo(BigDecimal.ZERO) > 0) ? 180 : 0;
                description = "Percentage Add-on: Extra insulin calculated as percentage of carb dose.";
                break;

            case NONE:
            default:
                description = "No strategy selected.";
                break;
        }

        if (fatProteinDose.compareTo(MIN_EXTENDED_DOSE_THRESHOLD) < 0) {
            if (fatProteinDose.compareTo(BigDecimal.ZERO) > 0)
                description += " (Extended dose < 0.5j ignored)";
            fatProteinDose = BigDecimal.ZERO;
            durationMinutes = 0;
        }

        BigDecimal totalDose = carbDose.add(fatProteinDose).setScale(2, RoundingMode.HALF_UP);
        fatProteinDose = fatProteinDose.setScale(2, RoundingMode.HALF_UP);

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
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal safeMultiply(BigDecimal val, BigDecimal ratio) {
        return val != null ? val.multiply(ratio) : BigDecimal.ZERO;
    }
}
