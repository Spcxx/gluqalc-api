package pl.srozga.gluqalc_api.dto.internal;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductPortionDto(
        UUID id,
        String name,
        BigDecimal weightInGrams,
        ProductNutritionDto nutrition,
        boolean published,
        UUID createdBy
) {
}
