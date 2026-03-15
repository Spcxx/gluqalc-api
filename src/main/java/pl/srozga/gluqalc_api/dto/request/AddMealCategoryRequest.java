package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a new custom meal category")
public record AddMealCategoryRequest(
        @Schema(description = "The display name of the meal category", example = "Dinner")
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must be at most 50 characters long")
        String name
) {
}