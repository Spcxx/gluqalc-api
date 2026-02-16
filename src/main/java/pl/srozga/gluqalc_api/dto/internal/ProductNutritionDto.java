package pl.srozga.gluqalc_api.dto.internal;

import java.math.BigDecimal;

public record ProductNutritionDto(
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
