package pl.srozga.gluqalc_api.dto.response;

import pl.srozga.gluqalc_api.common.ProductProviderType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductAdminResponse(
        UUID id,
        String name,
        String brand,
        String barcode,
        ProductNutritionResponse nutrition,
        List<ProductPortionResponse> portions,
        boolean published,
        Instant createdAt,
        Instant updatedAt,
        ProductProviderType provider
) {
}
