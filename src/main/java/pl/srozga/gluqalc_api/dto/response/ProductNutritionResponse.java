package pl.srozga.gluqalc_api.dto.response;

import java.math.BigDecimal;

public record ProductNutritionResponse(
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
