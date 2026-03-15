package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Standard macronutrient data for a product, typically calculated per 100g or 100ml")
public record ProductNutritionResponse(
        @Schema(description = "Energy in kilocalories", example = "450.00")
        BigDecimal energyKcal,
        @Schema(description = "Total carbohydrates in grams", example = "55.20")
        BigDecimal carbohydrates,
        @Schema(description = "Sugars in grams", example = "22.10")
        BigDecimal sugars,
        @Schema(description = "Total fat in grams", example = "20.50")
        BigDecimal fat,
        @Schema(description = "Saturated fat in grams", example = "8.40")
        BigDecimal saturatedFat,
        @Schema(description = "Protein in grams", example = "12.30")
        BigDecimal protein,
        @Schema(description = "Dietary fiber in grams", example = "5.00")
        BigDecimal fiber,
        @Schema(description = "Salt equivalent in grams", example = "0.25")
        BigDecimal salt,
        @Schema(description = "Glycemic Index", example = "55", nullable = true)
        Integer glycemicIndex
) {
}