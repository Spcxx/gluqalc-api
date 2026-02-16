package pl.srozga.gluqalc_api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductPortionResponse(
        UUID id,
        String name,
        BigDecimal weightInGrams,
        ProductNutritionResponse nutrition
) {
}
