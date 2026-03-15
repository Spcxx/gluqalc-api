package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.*;
import pl.srozga.gluqalc_api.common.BmrCalculationMethod;
import pl.srozga.gluqalc_api.common.MacroType;
import pl.srozga.gluqalc_api.common.UserGender;
import pl.srozga.gluqalc_api.validation.SumZero;
import pl.srozga.gluqalc_api.validation.ValidHourlyMap;
import pl.srozga.gluqalc_api.validation.ValidMacroStrategy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

public record UpdateUserProfileRequest(
        UserGender gender,

        @Positive(message = "Weight must be positive")
        @Digits(integer = 3, fraction = 2, message = "Invalid weight format")
        BigDecimal weightInKg,

        @Positive(message = "Height must be positive")
        @Digits(integer = 3, fraction = 1, message = "Invalid height format")
        BigDecimal heightInCm,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @DecimalMin(value = "1.0", message = "Activity level too low")
        @DecimalMax(value = "3.0", message = "Activity level too high")
        @Digits(integer = 1, fraction = 2)
        BigDecimal physicalActivityLevel,

        Integer kcalGoalDifference,

        @SumZero(message = "Weekly distribution must sum up to zero")
        Map<DayOfWeek, Integer> weeklyKcalDistribution,

        @Positive(message = "Body fat percentage must be positive")
        @DecimalMin(value = "0.0", message = "Body fat percentage must be at least 0")
        @DecimalMax(value = "100.0", message = "Body fat percentage cannot exceed 100")
        @Digits(integer = 3, fraction = 1)
        BigDecimal bodyFatPercentage,
        BmrCalculationMethod bmrCalculationMethod,
        @ValidMacroStrategy
        Map<MacroType, @PositiveOrZero BigDecimal> macroStrategy,

        @Positive(message = "ISF must be positive")
        @Digits(integer = 3, fraction = 2)
        BigDecimal insulinSensitivityFactor,
        @Positive(message = "IFP Ratio must be positive")
        @Digits(integer = 3, fraction = 2)
        BigDecimal insulinFatProteinRatio,
        @ValidHourlyMap
        Map<@Min(0) @Max(23) Integer, @Positive BigDecimal> hourlyCarbRatio
) {}
