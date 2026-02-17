package pl.srozga.gluqalc_api.dto.response;

import java.math.BigDecimal;

public record NutritionTargetsResponse(
        BigDecimal bmr,
        BigDecimal tdee,
        BigDecimal dailyKcalGoal,
        BigDecimal proteinGrams,
        BigDecimal fatGrams,
        BigDecimal carbsGrams
) {
}
