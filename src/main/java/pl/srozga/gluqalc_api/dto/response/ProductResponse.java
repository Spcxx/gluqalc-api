package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.srozga.gluqalc_api.common.ProductProviderType;
import java.util.List;
import java.util.UUID;

@Schema(description = "Standard product view for users, including insulin estimations and available portion sizes")
public record ProductResponse(
        @Schema(description = "Unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,
        @Schema(description = "Product name", example = "Whole Grain Bread")
        String name,
        @Schema(description = "Brand name", example = "Bakery")
        String brand,
        @Schema(description = "Product barcode", example = "5901234567890")
        String barcode,
        @Schema(description = "Nutritional values per 100g base")
        ProductNutritionResponse nutrition,
        @Schema(description = "Insulin dose estimates for 100g of the product")
        InsulinDoseResponse insulinDose,
        @Schema(description = "List of predefined portions for easier logging")
        List<ProductPortionResponse> portions,
        @Schema(description = "Approved translated and alternative product names")
        List<ProductNameResponse> names,
        @Schema(description = "Indicates if the product is from the local database or an external provider", example = "LOCAL")
        ProductProviderType provider
) {
}