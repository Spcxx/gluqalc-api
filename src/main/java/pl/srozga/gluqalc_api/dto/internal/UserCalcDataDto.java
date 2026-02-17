package pl.srozga.gluqalc_api.dto.internal;

import java.math.BigDecimal;

public record UserCalcDataDto(
        BigDecimal bmr, // basal metabolic rate
        BigDecimal tdee, // total daily energy expenditure - bmr with activity
        BigDecimal dailyGoalKcal, // daily goal in kcal with user goal
        BigDecimal dailyGoalCarbs, // daily goal in grams of carbs
        BigDecimal dailyGoalProtein, // daily goal in grams of protein
        BigDecimal dailyGoalFat // daily goal in grams of fat
) {

}
