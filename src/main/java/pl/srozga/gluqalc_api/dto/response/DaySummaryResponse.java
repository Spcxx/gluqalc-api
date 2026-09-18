package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Summary of nutritional progress for a specific day, comparing targets vs actual consumption")
public record DaySummaryResponse(
        @Schema(description = "The summarized date", example = "2026-03-15")
        LocalDate date,
        @Schema(description = "Calculated daily nutritional targets based on user profile and strategy")
        NutritionalValuesResponse target,
        @Schema(description = "Total nutrients consumed by the user on this day")
        NutritionalValuesResponse consumed,
        @Schema(description = "Remaining nutrients to reach the daily target (can be negative if exceeded)")
        NutritionalValuesResponse remaining,
        @Schema(description = "Summary of calculated insulin doses for the day")
        DailyInsulinSummaryResponse insulinSummary
) {
}