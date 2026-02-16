package pl.srozga.gluqalc_api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductChangeResponse(
        UUID id,
        UUID productId,
        UUID userId,
        String name,
        String brand,
        String barcode,
        BigDecimal energyKcal,
        BigDecimal carbohydrates,
        BigDecimal sugars,
        BigDecimal fat,
        BigDecimal saturatedFat,
        BigDecimal protein,
        BigDecimal fiber,
        BigDecimal salt,
        Integer glycemicIndex
) {
}
