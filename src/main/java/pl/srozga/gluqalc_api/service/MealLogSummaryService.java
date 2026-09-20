package pl.srozga.gluqalc_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.common.MacroType;
import pl.srozga.gluqalc_api.component.diabetes.DiabetesCalculator;
import pl.srozga.gluqalc_api.component.nutrition.NutritionCalculator;
import pl.srozga.gluqalc_api.dto.internal.DiabetesCalcDataDto;
import pl.srozga.gluqalc_api.dto.response.DailyInsulinSummaryResponse;
import pl.srozga.gluqalc_api.dto.response.NutritionalValuesResponse;
import pl.srozga.gluqalc_api.dto.internal.UserCalcDataDto;
import pl.srozga.gluqalc_api.dto.response.DaySummaryResponse;
import pl.srozga.gluqalc_api.entity.MealEntry;
import pl.srozga.gluqalc_api.entity.UserProfile;
import pl.srozga.gluqalc_api.exception.DomainValidationException;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.repository.MealEntryRepository;
import pl.srozga.gluqalc_api.repository.UserProfileRepository;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MealLogSummaryService {
    private final UserProfileRepository userProfileRepository;
    private final MealEntryRepository mealEntryRepository;
    private final NutritionCalculator nutritionCalculator;
    private final ObjectMapper objectMapper;
    private final DiabetesCalculator diabetesCalculator;

    @Transactional(readOnly = true)
    public DaySummaryResponse getDaySummary(AuthUser user, LocalDate date) {
        UserProfile profile = userProfileRepository.findById(user.id())
                .orElseThrow(() -> new NotFoundException("User profile not found"));

        List<MealEntry> consumedMeals = mealEntryRepository.findAllByUserIdAndConsumedAt(user.id(), date);
        BigDecimal weeklyBalanceAdjustment = calculateWeeklyBalance(user, profile, date);

        return calculateSummary(profile, date, consumedMeals, weeklyBalanceAdjustment);
    }

    private DaySummaryResponse calculateSummary(UserProfile profile, LocalDate date, List<MealEntry> meals, BigDecimal adjustment) {
        UserCalcDataDto baseCalc;
        try {
            baseCalc = nutritionCalculator.calculate(profile);
        } catch (DomainValidationException | IllegalArgumentException e) {
            log.debug("Cannot calculate targets for user {}: {}", profile.getId(), e.getMessage());
            return emptySummary(date, meals, profile);
        }

        int dayOffset = getDayOffset(profile, date);
        BigDecimal todayKcalTarget = baseCalc.dailyGoalKcal().add(BigDecimal.valueOf(dayOffset));

        BigDecimal finalTargetKcal = todayKcalTarget.add(adjustment);
        if (finalTargetKcal.compareTo(BigDecimal.ZERO) < 0)
            finalTargetKcal = BigDecimal.ZERO;

        NutritionalValuesResponse target = recalculateMacrosForKcal(finalTargetKcal, profile);
        NutritionalValuesResponse consumed = sumConsumed(meals);
        NutritionalValuesResponse remaining = new NutritionalValuesResponse(
                target.energyKcal().subtract(consumed.energyKcal()),
                target.protein().subtract(consumed.protein()),
                target.fat().subtract(consumed.fat()),
                target.carbohydrates().subtract(consumed.carbohydrates())
        );
        DailyInsulinSummaryResponse insulinSummary = buildInsulinSummary(meals, profile);

        return new DaySummaryResponse(date, target, consumed, remaining, insulinSummary);
    }

    private DaySummaryResponse emptySummary(LocalDate date, List<MealEntry> meals, UserProfile profile) {
        NutritionalValuesResponse consumed = sumConsumed(meals);
        NutritionalValuesResponse zero = NutritionalValuesResponse.zero();
        NutritionalValuesResponse remaining = new NutritionalValuesResponse(
                zero.energyKcal().subtract(consumed.energyKcal()),
                zero.protein().subtract(consumed.protein()),
                zero.fat().subtract(consumed.fat()),
                zero.carbohydrates().subtract(consumed.carbohydrates())
        );
        DailyInsulinSummaryResponse insulinSummary = buildInsulinSummary(meals, profile);
        return new DaySummaryResponse(date, zero, consumed, remaining, insulinSummary);
    }

    private DailyInsulinSummaryResponse buildInsulinSummary(List<MealEntry> meals, UserProfile profile) {
        BigDecimal totalCu = BigDecimal.ZERO;
        BigDecimal totalFpu = BigDecimal.ZERO;
        BigDecimal consumedCarbDose = BigDecimal.ZERO;
        BigDecimal consumedFatProteinDose = BigDecimal.ZERO;
        BigDecimal consumedBolusDose = BigDecimal.ZERO;

        Map<UUID, List<MealEntry>> groupedByCategory = meals.stream()
                .collect(Collectors.groupingBy(m -> m.getMealCategory().getId()));

        for (List<MealEntry> categoryMeals : groupedByCategory.values()) {
            if (categoryMeals.isEmpty()) continue;

            BigDecimal carbs = BigDecimal.ZERO;
            BigDecimal protein = BigDecimal.ZERO;
            BigDecimal fat = BigDecimal.ZERO;
            BigDecimal fiber = BigDecimal.ZERO;
            BigDecimal weightedGiSum = BigDecimal.ZERO;
            BigDecimal carbsWithGi = BigDecimal.ZERO;

            for (MealEntry m : categoryMeals) {
                BigDecimal c = m.getCarbohydrates() != null ? m.getCarbohydrates() : BigDecimal.ZERO;
                carbs = carbs.add(c);
                protein = protein.add(m.getProtein() != null ? m.getProtein() : BigDecimal.ZERO);
                fat = fat.add(m.getFat() != null ? m.getFat() : BigDecimal.ZERO);
                fiber = fiber.add(m.getFiber() != null ? m.getFiber() : BigDecimal.ZERO);

                if (m.getGlycemicIndex() != null && c.compareTo(BigDecimal.ZERO) > 0) {
                    weightedGiSum = weightedGiSum.add(c.multiply(BigDecimal.valueOf(m.getGlycemicIndex())));
                    carbsWithGi = carbsWithGi.add(c);
                }
            }

            BigDecimal averageGi = carbsWithGi.compareTo(BigDecimal.ZERO) > 0
                    ? weightedGiSum.divide(carbsWithGi, 0, RoundingMode.HALF_UP)
                    : null;

            LocalTime time = categoryMeals.getFirst().getConsumedAtTime();

            DiabetesCalcDataDto calc = diabetesCalculator.calculate(
                    carbs, protein, fat, fiber, averageGi, profile, time
            );

            if (calc.carbUnit() != null) totalCu = totalCu.add(calc.carbUnit().setScale(1, RoundingMode.HALF_UP));
            if (calc.fatProteinUnit() != null) totalFpu = totalFpu.add(calc.fatProteinUnit().setScale(1, RoundingMode.HALF_UP));
            if (calc.carbDose() != null) consumedCarbDose = consumedCarbDose.add(calc.carbDose().setScale(2, RoundingMode.HALF_UP));
            if (calc.fatProteinDose() != null) consumedFatProteinDose = consumedFatProteinDose.add(calc.fatProteinDose().setScale(2, RoundingMode.HALF_UP));
            if (calc.totalDose() != null) consumedBolusDose = consumedBolusDose.add(calc.totalDose().setScale(2, RoundingMode.HALF_UP));
        }

        BigDecimal estimatedTdd = null;
        BigDecimal basal = profile.getDailyBasalInsulin();
        BigDecimal estimatedBolusTarget = diabetesCalculator.calculateEstimatedDailyBolusTarget(profile);
        BigDecimal remainingBolusTarget = null;

        if (profile.getWeightInKg() != null && profile.getTddMultiplier() != null) {
            estimatedTdd = profile.getWeightInKg()
                    .multiply(profile.getTddMultiplier())
                    .setScale(1, RoundingMode.HALF_UP);
        }

        if (estimatedBolusTarget != null) {
            remainingBolusTarget = estimatedBolusTarget
                    .subtract(consumedBolusDose)
                    .setScale(1, RoundingMode.HALF_UP);
        }

        return new DailyInsulinSummaryResponse(
                totalCu,
                totalFpu,
                consumedCarbDose,
                consumedFatProteinDose,
                consumedBolusDose,
                estimatedTdd,
                basal,
                estimatedBolusTarget,
                remainingBolusTarget
        );
    }

    private NutritionalValuesResponse sumConsumed(List<MealEntry> meals) {
        BigDecimal k = BigDecimal.ZERO;
        BigDecimal p = BigDecimal.ZERO;
        BigDecimal f = BigDecimal.ZERO;
        BigDecimal c = BigDecimal.ZERO;

        for (MealEntry m : meals) {
            if (m.getEnergyKcal() != null)
                k = k.add(m.getEnergyKcal());
            if (m.getProtein() != null)
                p = p.add(m.getProtein());
            if (m.getFat() != null)
                f = f.add(m.getFat());
            if (m.getCarbohydrates() != null)
                c = c.add(m.getCarbohydrates());
        }
        return new NutritionalValuesResponse(k, p, f, c);
    }

    private int getDayOffset(UserProfile profile, LocalDate date) {
        if (profile.getWeeklyKcalDistributionJson() == null)
            return 0;
        try {
            Map<DayOfWeek, Integer> dist = objectMapper.readValue(
                    profile.getWeeklyKcalDistributionJson(),
                    new TypeReference<>() {}
            );
            return dist.getOrDefault(DayOfWeek.valueOf(date.getDayOfWeek().name()), 0);
        } catch (Exception e) {
            log.error("Failed to parse weekly distribution for user {}", profile.getId());
            return 0;
        }
    }

    private NutritionalValuesResponse recalculateMacrosForKcal(BigDecimal kcal, UserProfile profile) {
        Map<MacroType, BigDecimal> ratios = nutritionCalculator.getMacroRatios(profile.getMacroStrategyJson());

        BigDecimal p = kcal.multiply(ratios.getOrDefault(MacroType.PROTEIN, BigDecimal.ZERO)).divide(new BigDecimal("4"), 0, RoundingMode.HALF_UP);
        BigDecimal f = kcal.multiply(ratios.getOrDefault(MacroType.FAT, BigDecimal.ZERO)).divide(new BigDecimal("9"), 0, RoundingMode.HALF_UP);
        BigDecimal c = kcal.multiply(ratios.getOrDefault(MacroType.CARBOHYDRATE, BigDecimal.ZERO)).divide(new BigDecimal("4"), 0, RoundingMode.HALF_UP);

        return new NutritionalValuesResponse(kcal, p, f, c);
    }

    private BigDecimal calculateWeeklyBalance(AuthUser user, UserProfile profile, LocalDate today) {
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (today.isEqual(startOfWeek) || today.isBefore(startOfWeek))
            return BigDecimal.ZERO;

        LocalDate yesterday = today.minusDays(1);
        List<MealEntry> pastEntries = mealEntryRepository.findAllByUserIdAndConsumedAtBetween(user.id(), startOfWeek, yesterday);

        Map<LocalDate, List<MealEntry>> entriesByDate = pastEntries.stream()
                .collect(Collectors.groupingBy(MealEntry::getConsumedAt));

        BigDecimal accumulatedBalance = BigDecimal.ZERO;
        UserCalcDataDto baseCalc = nutritionCalculator.calculate(profile);

        LocalDate iterDate = startOfWeek;
        while (!iterDate.isAfter(yesterday)) {
            int dayOffset = getDayOffset(profile, iterDate);
            BigDecimal dailyTarget = baseCalc.dailyGoalKcal().add(BigDecimal.valueOf(dayOffset));
            List<MealEntry> dayMeals = entriesByDate.getOrDefault(iterDate, Collections.emptyList());
            BigDecimal consumedKcal = sumConsumed(dayMeals).energyKcal();
            BigDecimal dayBalance = dailyTarget.subtract(consumedKcal);

            accumulatedBalance = accumulatedBalance.add(dayBalance);

            iterDate = iterDate.plusDays(1);
        }

        return accumulatedBalance.min(BigDecimal.ZERO);
    }
}
