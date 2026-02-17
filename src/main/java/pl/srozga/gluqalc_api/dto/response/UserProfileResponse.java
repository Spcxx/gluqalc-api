package pl.srozga.gluqalc_api.dto.response;

import pl.srozga.gluqalc_api.common.BmrCalculationMethod;
import pl.srozga.gluqalc_api.common.InsulinFatProteinStrategy;
import pl.srozga.gluqalc_api.common.MacroCalculationStrategy;
import pl.srozga.gluqalc_api.common.UserGender;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

public record UserProfileResponse(
        UserGender gender,
        BigDecimal weightInKg,
        BigDecimal heightInCm,
        LocalDate birthDate,
        Integer age,
        BigDecimal physicalActivityLevel,
        Integer kcalGoalDifference,
        Map<DayOfWeek, Integer> weeklyKcalDistribution,
        BigDecimal bodyFatPercentage,
        BmrCalculationMethod bmrMethod,
        MacroCalculationStrategy macroStrategy,
        InsulinFatProteinStrategy ifpStrategy,
        BigDecimal insulinSensitivityFactor,
        BigDecimal insulinFatProteinRatio,
        Map<Integer, BigDecimal> hourlyCarbRatio,
        NutritionTargetsResponse targets
) {
}
