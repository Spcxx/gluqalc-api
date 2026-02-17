package pl.srozga.gluqalc_api.dto.response;

import java.time.LocalDate;

public record DaySummaryResponse(
        LocalDate date,
        NutritionalValuesResponse target,
        NutritionalValuesResponse consumed,
        NutritionalValuesResponse remaining
) {
}
