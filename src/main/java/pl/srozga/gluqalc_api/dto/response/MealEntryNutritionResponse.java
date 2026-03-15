package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Calculated nutritional values for a specific meal entry based on consumed weight")
public record MealEntryNutritionResponse(
        @Schema(description = "Energy in kilocalories", example = "250.50")
        BigDecimal energyKcal,
        @Schema(description = "Total carbohydrates in grams", example = "30.00")
        BigDecimal carbohydrates,
        @Schema(description = "Sugars in grams", example = "5.20")
        BigDecimal sugars,
        @Schema(description = "Total fat in grams", example = "12.00")
        BigDecimal fat,
        @Schema(description = "Saturated fat in grams", example = "2.10")
        BigDecimal saturatedFat,
        @Schema(description = "Protein in grams", example = "8.50")
        BigDecimal protein,
        @Schema(description = "Dietary fiber in grams", example = "4.00")
        BigDecimal fiber,
        @Schema(description = "Salt in grams", example = "0.50")
        BigDecimal salt,
        @Schema(description = "Glycemic Index of the entry", example = "45")
        Integer glycemicIndex
) {
}