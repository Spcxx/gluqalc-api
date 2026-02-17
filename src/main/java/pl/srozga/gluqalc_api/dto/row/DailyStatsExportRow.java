package pl.srozga.gluqalc_api.dto.row;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyStatsExportRow(
        LocalDate date,
        String dayOfWeek,
        int mealsCount,
        String eatingWindow,      // NOWE
        BigDecimal kcalConsumed,
        BigDecimal kcalGoal,
        BigDecimal balance,
        BigDecimal proteinGrams,
        BigDecimal fatGrams,
        BigDecimal carbsGrams,
        BigDecimal sugarsGrams,   // NOWE
        BigDecimal fiberGrams,    // NOWE
        BigDecimal glycemicLoad,  // NOWE
        String macroRatio,
        BigDecimal cu,
        BigDecimal fpu,
        BigDecimal totalInsulin,
        BigDecimal insulinCarbs,
        BigDecimal insulinFatProtein,
        BigDecimal avgInsulinPerMeal
) {
    public static DailyStatsExportRow empty(LocalDate date, String dayName, BigDecimal goal) {
        return new DailyStatsExportRow(
                date, dayName, 0, "-",
                BigDecimal.ZERO, goal, BigDecimal.ZERO.subtract(goal),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, // Sugars, Fiber, GL
                "0% / 0% / 0%",
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
    }
}
