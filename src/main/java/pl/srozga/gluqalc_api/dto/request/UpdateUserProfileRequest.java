package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import pl.srozga.gluqalc_api.common.BmrCalculationMethod;
import pl.srozga.gluqalc_api.common.InsulinDeliveryMethod;
import pl.srozga.gluqalc_api.common.MacroType;
import pl.srozga.gluqalc_api.common.UserGender;
import pl.srozga.gluqalc_api.validation.SumZero;
import pl.srozga.gluqalc_api.validation.ValidHourlyMap;
import pl.srozga.gluqalc_api.validation.ValidMacroStrategy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "Request payload for setting or updating the user's health profile, physical attributes and diabetes parameters")
public record UpdateUserProfileRequest(
        @Schema(description = "User's biological gender, used for BMR calculations", example = "MALE")
        UserGender gender,
        @Schema(description = "Current body weight in kilograms", example = "75.50")
        @Positive(message = "Weight must be positive")
        @Digits(integer = 3, fraction = 2, message = "Invalid weight format")
        BigDecimal weightInKg,
        @Schema(description = "Height in centimeters", example = "180.0")
        @Positive(message = "Height must be positive")
        @Digits(integer = 3, fraction = 1, message = "Invalid height format")
        BigDecimal heightInCm,
        @Schema(description = "Date of birth in ISO format", example = "1990-05-15")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,
        @Schema(description = "Physical Activity Level (PAL) multiplier", example = "1.55")
        @DecimalMin(value = "1.0", message = "Activity level too low")
        @DecimalMax(value = "3.0", message = "Activity level too high")
        @Digits(integer = 1, fraction = 2)
        BigDecimal physicalActivityLevel,
        @Schema(description = "Daily caloric deficit (negative) or surplus (positive) to reach weight goals", example = "-300")
        Integer kcalGoalDifference,
        @Schema(description = "Weekly distribution of the caloric goal variance. Must sum to zero.", example = "{\"MONDAY\": -100, \"SATURDAY\": 200, \"SUNDAY\": -100}")
        @SumZero(message = "Weekly distribution must sum up to zero")
        Map<DayOfWeek, Integer> weeklyKcalDistribution,
        @Schema(description = "Body fat percentage (0-100)", example = "15.5")
        @Positive(message = "Body fat percentage must be positive")
        @DecimalMin(value = "0.0", message = "Body fat percentage must be at least 0")
        @DecimalMax(value = "100.0", message = "Body fat percentage cannot exceed 100")
        @Digits(integer = 3, fraction = 1)
        BigDecimal bodyFatPercentage,
        @Schema(description = "Preferred formula for BMR calculation", example = "KATCH_MCARDLE")
        BmrCalculationMethod bmrCalculationMethod,
        @Schema(description = "Macronutrient distribution strategy (percentage of daily calories)", example = "{\"PROTEIN\": 25.0, \"FAT\": 30.0, \"CARBOHYDRATE\": 45.0}")
        @ValidMacroStrategy
        Map<MacroType, @PositiveOrZero BigDecimal> macroStrategy,
        @Schema(description = "Insulin Sensitivity Factor (ISF): how much 1 unit of insulin drops blood sugar", example = "35.00")
        @Positive(message = "ISF must be positive")
        @Digits(integer = 3, fraction = 2)
        BigDecimal insulinSensitivityFactor,
        @Schema(description = "Insulin to Fat/Protein Ratio: how many units of insulin are needed for 1 FPU (100kcal from fat/protein)", example = "1.50")
        @Positive(message = "IFP Ratio must be positive")
        @Digits(integer = 3, fraction = 2)
        BigDecimal insulinFatProteinRatio,
        @Schema(description = "Preferred insulin delivery method", example = "PEN")
        InsulinDeliveryMethod insulinDeliveryMethod,
        @Schema(description = "Insulin to Carbohydrate Ratio (ICR) mapped by hour of the day (0-23)", example = "{\"8\": 1.2, \"14\": 1.0, \"20\": 1.5}")
        @ValidHourlyMap
        Map<@Min(0) @Max(23) Integer, @Positive BigDecimal> hourlyCarbRatio
) {}