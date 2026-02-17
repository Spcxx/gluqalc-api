package pl.srozga.gluqalc_api.dto.response;

import java.math.BigDecimal;

public record NutritionalValuesResponse(
        BigDecimal energyKcal,
        BigDecimal protein,
        BigDecimal fat,
        BigDecimal carbohydrates
) {
    public static NutritionalValuesResponse zero() {
        return new NutritionalValuesResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}