package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.srozga.gluqalc_api.common.ProductProviderType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Extended product data for administrative use, including publishing status and audit timestamps")
public record ProductAdminResponse(
        @Schema(description = "Internal product ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,
        @Schema(description = "Product name", example = "Peanut Butter")
        String name,
        @Schema(description = "Brand name", example = "Producer")
        String brand,
        @Schema(description = "Product barcode", example = "1234567890123")
        String barcode,
        @Schema(description = "Raw nutritional values per 100g/ml")
        ProductNutritionResponse nutrition,
        @Schema(description = "List of all defined portions for this product")
        List<ProductPortionResponse> portions,
        @Schema(description = "Translated and alternative product names")
        List<ProductNameResponse> names,
        @Schema(description = "Publishing status; true if visible to all users", example = "true")
        boolean published,
        @Schema(description = "Timestamp of product creation (UTC)", example = "2026-03-15T10:15:30Z")
        Instant createdAt,
        @Schema(description = "Timestamp of the last update (UTC)", example = "2026-03-15T12:00:00Z")
        Instant updatedAt,
        @Schema(description = "The source of the product data", example = "LOCAL")
        ProductProviderType provider,
        @Schema(description = "Source attribution metadata for external product data")
        @com.fasterxml.jackson.annotation.JsonProperty("_metadata")
        ProductMetadataResponse metadata
) {
}