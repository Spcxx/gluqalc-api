package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.UUID;

@Schema(description = "Information about a meal category and its associated entries for a given day")
public record MealCategoryResponse(
        @Schema(description = "Unique identifier of the category", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Schema(description = "Category name", example = "Breakfast")
        String name,
        @Schema(description = "Order in which categories should be displayed", example = "1")
        Integer sortOrder,
        @Schema(description = "Sum of nutritional values for all entries in this category", nullable = true)
        @Nullable
        ProductNutritionResponse totalNutrition,
        @Schema(description = "List of individual food items logged in this category", nullable = true)
        @Nullable List<MealEntryResponse> entries,
        @Schema(description = "Aggregated insulin dose requirement for the entire meal category", nullable = true)
        @Nullable InsulinDoseResponse insulinDose
) {
}