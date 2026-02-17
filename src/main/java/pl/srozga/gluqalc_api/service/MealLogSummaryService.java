package pl.srozga.gluqalc_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.component.calculator.NutritionCalculator;
import pl.srozga.gluqalc_api.dto.response.NutritionalValuesResponse;
import pl.srozga.gluqalc_api.dto.internal.UserCalcDataDto;
import pl.srozga.gluqalc_api.dto.response.DaySummaryResponse;
import pl.srozga.gluqalc_api.entity.MealEntry;
import pl.srozga.gluqalc_api.entity.UserProfile;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.repository.MealEntryRepository;
import pl.srozga.gluqalc_api.repository.UserProfileRepository;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MealLogSummaryService {
    private final UserProfileRepository userProfileRepository;
    private final MealEntryRepository mealEntryRepository;
    private final NutritionCalculator nutritionCalculator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public DaySummaryResponse getDaySummary(AuthUser user, LocalDate date) {
        UserProfile profile = userProfileRepository.findById(user.id())
                .orElseThrow(() -> new NotFoundException("User profile not found"));

        List<MealEntry> consumedMeals = mealEntryRepository.findAllByUserIdAndConsumedAt(user.id(), date);

        return calculateSummary(profile, date, consumedMeals);
    }

    private DaySummaryResponse calculateSummary(UserProfile profile, LocalDate date, List<MealEntry> meals) {
        UserCalcDataDto baseCalc;
        try {
            baseCalc = nutritionCalculator.calculate(profile);
        } catch (IllegalArgumentException e) {
            log.debug("Cannot calculate targets for user {}: {}", profile.getId(), e.getMessage());
            return emptySummary(date, meals);
        }

        int dayOffset = getDayOffset(profile, date);
        BigDecimal todayKcalTarget = baseCalc.dailyGoalKcal().add(BigDecimal.valueOf(dayOffset));

        NutritionalValuesResponse target = recalculateMacrosForKcal(todayKcalTarget, profile);
        NutritionalValuesResponse consumed = sumConsumed(meals);
        NutritionalValuesResponse remaining = new NutritionalValuesResponse(
                target.energyKcal().subtract(consumed.energyKcal()),
                target.protein().subtract(consumed.protein()),
                target.fat().subtract(consumed.fat()),
                target.carbohydrates().subtract(consumed.carbohydrates())
        );

        return new DaySummaryResponse(date, target, consumed, remaining);
    }

    private DaySummaryResponse emptySummary(LocalDate date, List<MealEntry> meals) {
        NutritionalValuesResponse consumed = sumConsumed(meals);
        NutritionalValuesResponse zero = NutritionalValuesResponse.zero();
        NutritionalValuesResponse remaining = new NutritionalValuesResponse(
                zero.energyKcal().subtract(consumed.energyKcal()),
                zero.protein().subtract(consumed.protein()),
                zero.fat().subtract(consumed.fat()),
                zero.carbohydrates().subtract(consumed.carbohydrates())
        );
        return new DaySummaryResponse(date, zero, consumed, remaining);
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
        var ratios = nutritionCalculator.getMacroRatios(profile.getMacroStrategy());

        BigDecimal p = kcal.multiply(BigDecimal.valueOf(ratios.protein())).divide(new BigDecimal("4"), 0, RoundingMode.HALF_UP);
        BigDecimal f = kcal.multiply(BigDecimal.valueOf(ratios.fat())).divide(new BigDecimal("9"), 0, RoundingMode.HALF_UP);
        BigDecimal c = kcal.multiply(BigDecimal.valueOf(ratios.carb())).divide(new BigDecimal("4"), 0, RoundingMode.HALF_UP);

        return new NutritionalValuesResponse(kcal, p, f, c);
    }
}
