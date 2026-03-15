package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Calculated daily metabolic and nutritional targets based on user's profile and goals")
public record NutritionTargetsResponse(
        @Schema(description = "Basal Metabolic Rate (BMR) - calories burned at rest", example = "1850.50")
        BigDecimal bmr,
        @Schema(description = "Total Daily Energy Expenditure (TDEE) - calories burned including activity", example = "2400.00")
        BigDecimal tdee,
        @Schema(description = "Final daily calorie goal after applying surplus/deficit adjustment", example = "2100.00")
        BigDecimal dailyKcalGoal,
        @Schema(description = "Target protein intake in grams", example = "120.0")
        BigDecimal proteinGrams,
        @Schema(description = "Target fat intake in grams", example = "70.0")
        BigDecimal fatGrams,
        @Schema(description = "Target carbohydrate intake in grams", example = "245.0")
        BigDecimal carbsGrams
) {
}