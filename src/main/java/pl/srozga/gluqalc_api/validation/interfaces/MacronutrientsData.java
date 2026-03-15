package pl.srozga.gluqalc_api.validation.interfaces;

import java.math.BigDecimal;

public interface MacronutrientsData {
    BigDecimal carbohydrates();
    BigDecimal sugars();
    BigDecimal fat();
    BigDecimal saturatedFat();
}
