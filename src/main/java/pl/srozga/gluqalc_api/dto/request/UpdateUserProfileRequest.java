package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.*;
import pl.srozga.gluqalc_api.common.BmrCalculationMethod;
import pl.srozga.gluqalc_api.common.MacroCalculationStrategy;
import pl.srozga.gluqalc_api.common.UserGender;
import pl.srozga.gluqalc_api.validation.SumZero;
import pl.srozga.gluqalc_api.validation.ValidHourlyMap;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

public record UpdateUserProfileRequest(
        UserGender gender,

        @Positive(message = "Weight must be positive")
        BigDecimal weightInKg,

        @Positive(message = "Height must be positive")
        BigDecimal heightInCm,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @DecimalMin(value = "1.0", message = "Activity level too low")
        @DecimalMax(value = "3.0", message = "Activity level too high")
        BigDecimal physicalActivityLevel,

        Integer kcalGoalDifference,
        @SumZero
        Map<DayOfWeek, Integer> weeklyKcalDistribution,

        @Positive(message = "Body fat percentage must be positive")
        @DecimalMin(value = "0.0", message = "Body fat percentage must be at least 0")
        @DecimalMax(value = "100.0", message = "Body fat percentage cannot exceed 100")
        BigDecimal bodyFatPercentage,
        BmrCalculationMethod bmrCalculationMethod,
        MacroCalculationStrategy macroCalculationStrategy,

        @Positive(message = "ISF must be positive")
        BigDecimal insulinSensitivityFactor,
        @Positive(message = "IFP Ratio must be positive")
        BigDecimal insulinFatProteinRatio,
        @ValidHourlyMap
        Map<Integer, BigDecimal> hourlyCarbRatio
) {}
