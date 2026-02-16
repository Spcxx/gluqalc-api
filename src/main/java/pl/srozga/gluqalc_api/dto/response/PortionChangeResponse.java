package pl.srozga.gluqalc_api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PortionChangeResponse(
        UUID id,
        UUID portionId,
        UUID userId,
        String name,
        BigDecimal weightInGrams
) {
}
