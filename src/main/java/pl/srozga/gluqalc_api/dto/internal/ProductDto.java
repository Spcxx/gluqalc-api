package pl.srozga.gluqalc_api.dto.internal;

import pl.srozga.gluqalc_api.common.ProductProviderType;

import java.io.Serializable;
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
        Instant updatedAt,
        ProductProviderType source
) implements Serializable {
}
