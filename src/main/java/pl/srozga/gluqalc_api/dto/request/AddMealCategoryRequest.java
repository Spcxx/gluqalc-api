package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddMealCategoryRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        String name
) {
}
