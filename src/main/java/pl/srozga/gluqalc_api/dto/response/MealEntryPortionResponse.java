package pl.srozga.gluqalc_api.dto.response;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

public record MealEntryPortionResponse(
        UUID id,
        String name,
        BigDecimal quantity,
        BigDecimal unitWeight,
        @Nullable BigDecimal totalWeight
) {
}
