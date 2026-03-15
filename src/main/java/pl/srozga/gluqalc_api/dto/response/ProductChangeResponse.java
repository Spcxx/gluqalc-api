package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Represents a user-proposed modification to a product's identity or nutritional values")
public record ProductChangeResponse(
        @Schema(description = "Unique identifier of the change proposal", example = "c9876543-b21a-43d7-9012-3456789abcde")
        UUID id,
        @Schema(description = "ID of the product that this change refers to", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID productId,
        @Schema(description = "ID of the user who proposed the change", example = "d1e2f3a4-b5c6-47d8-9e0f-1a2b3c4d5e6f")
        UUID userId,
        @Schema(description = "Proposed new name", example = "Peanut Butter", nullable = true)
        String name,
        @Schema(description = "Proposed new brand", example = "Producer", nullable = true)
        String brand,
        @Schema(description = "Proposed new barcode", example = "9876543210987", nullable = true)
        String barcode,
        @Schema(description = "Proposed energy value (kcal)", example = "590.00", nullable = true)
        BigDecimal energyKcal,
        @Schema(description = "Proposed carbohydrates (g)", example = "12.50", nullable = true)
        BigDecimal carbohydrates,
        @Schema(description = "Proposed sugars (g)", example = "6.20", nullable = true)
        BigDecimal sugars,
        @Schema(description = "Proposed fat (g)", example = "50.00", nullable = true)
        BigDecimal fat,
        @Schema(description = "Proposed saturated fat (g)", example = "10.00", nullable = true)
        BigDecimal saturatedFat,
        @Schema(description = "Proposed protein (g)", example = "25.00", nullable = true)
        BigDecimal protein,
        @Schema(description = "Proposed fiber (g)", example = "8.00", nullable = true)
        BigDecimal fiber,
        @Schema(description = "Proposed salt (g)", example = "1.1000", nullable = true)
        BigDecimal salt,
        @Schema(description = "Proposed Glycemic Index", example = "15", nullable = true)
        Integer glycemicIndex
) {
}