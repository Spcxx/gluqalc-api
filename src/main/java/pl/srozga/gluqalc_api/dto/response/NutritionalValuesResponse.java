package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Set of core macronutrients used for summaries and targets")
public record NutritionalValuesResponse(
        @Schema(description = "Energy in kcal", example = "2000.0")
        BigDecimal energyKcal,
        @Schema(description = "Protein in grams", example = "150.0")
        BigDecimal protein,
        @Schema(description = "Fat in grams", example = "65.0")
        BigDecimal fat,
        @Schema(description = "Total carbohydrates in grams", example = "200.0")
        BigDecimal carbohydrates
) {
    public static NutritionalValuesResponse zero() {
        return new NutritionalValuesResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}