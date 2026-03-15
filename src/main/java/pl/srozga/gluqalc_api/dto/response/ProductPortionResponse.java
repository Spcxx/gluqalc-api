package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Portion-specific data, including nutrients and insulin doses adjusted to the portion weight")
public record ProductPortionResponse(
        @Schema(description = "Portion unique identifier", example = "987fcdeb-51a2-43d7-9012-3456789abcde")
        UUID id,
        @Schema(description = "Portion name", example = "Standard slice")
        String name,
        @Schema(description = "Exact weight of this portion in grams", example = "35.00")
        BigDecimal weightInGrams,
        @Schema(description = "Nutritional values specifically for this portion's weight")
        ProductNutritionResponse nutrition,
        @Schema(description = "Insulin dose estimates specifically for this portion's weight")
        InsulinDoseResponse insulinDose
) {
}