package pl.srozga.gluqalc_api.dto.internal;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductDto(
        UUID id,
        String name,
        String brand,
        String barcode,
        ProductNutritionDto nutrition,
        List<ProductPortionDto> portions,
        boolean published,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}
