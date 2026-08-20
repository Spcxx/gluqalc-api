package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.srozga.gluqalc_api.common.BmrCalculationMethod;
import pl.srozga.gluqalc_api.common.CombinedInsulinCalculationMethod;
import pl.srozga.gluqalc_api.common.InsulinDeliveryMethod;
import pl.srozga.gluqalc_api.common.MacroType;
import pl.srozga.gluqalc_api.common.UserGender;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "User's health profile including biometric data, caloric distribution, and diabetes management parameters")
public record UserProfileResponse(
        @Schema(description = "Biological gender used for metabolic calculations", example = "FEMALE")
        UserGender gender,
        @Schema(description = "Current weight in kg", example = "68.50")
        BigDecimal weightInKg,
        @Schema(description = "Height in cm", example = "170.0")
        BigDecimal heightInCm,
        @Schema(description = "Date of birth", example = "1995-08-20")
        LocalDate birthDate,
        @Schema(description = "Automatically calculated age based on birth date", example = "30")
        Integer age,
        @Schema(description = "Physical Activity Level (PAL) multiplier", example = "1.4")
        BigDecimal physicalActivityLevel,
        @Schema(description = "Daily caloric adjustment (surplus/deficit)", example = "-200")
        Integer kcalGoalDifference,
        @Schema(description = "Variance of caloric goal per day of the week", example = "{\"SATURDAY\": 200, \"SUNDAY\": -200}")
        Map<DayOfWeek, Integer> weeklyKcalDistribution,
        @Schema(description = "Measured or estimated body fat percentage", example = "22.5")
        BigDecimal bodyFatPercentage,
        @Schema(description = "Method used for Basal Metabolic Rate calculation", example = "KATCH_MCARDLE")
        BmrCalculationMethod bmrMethod,
        @Schema(description = "Distribution of macronutrients in the daily diet (percentages)", example = "{\"PROTEIN\": 30.0, \"FAT\": 25.0, \"CARBOHYDRATE\": 45.0}")
        Map<MacroType, BigDecimal> macroStrategy,
        @Schema(description = "Insulin Sensitivity Factor (ISF): glucose drop per 1 unit of insulin", example = "45.00")
        BigDecimal insulinSensitivityFactor,
        @Schema(description = "Insulin to Fat/Protein Ratio: units needed per 1 WBT", example = "1.20")
        BigDecimal insulinFatProteinRatio,
        @Schema(description = "Insulin delivery method selected by the user", example = "PUMP")
        InsulinDeliveryMethod insulinDeliveryMethod,
        @Schema(description = "Method used to calculate the combined insulin dose for protein and fat", example = "PANKOWSKA")
        CombinedInsulinCalculationMethod combinedInsulinCalculationMethod,
        @Schema(description = "Insulin to Carbohydrate Ratio (ICR) by hour of the day", example = "{\"8\": 1.1, \"14\": 0.9}")
        Map<Integer, BigDecimal> hourlyCarbRatio,
        @Schema(description = "Aggregated metabolic and nutritional targets calculated based on the profile data")
        NutritionTargetsResponse targets
) {
}