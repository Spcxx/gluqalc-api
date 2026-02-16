package pl.srozga.gluqalc_api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductAdminResponse(
        UUID id,
        String name,
        String brand,
        String barcode,
        ProductNutritionResponse nutrition,
        List<ProductPortionAdminResponse> portions,
        boolean published,
        Instant createdAt,
        Instant updatedAt
) {
}
