package pl.srozga.gluqalc_api.dto.response;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record MealCategoryResponse(
        UUID id,
        String name,
        Integer sortOrder,
        @Nullable
        ProductNutritionResponse totalNutrition,
        @Nullable List<MealEntryResponse> entries
) {
}
