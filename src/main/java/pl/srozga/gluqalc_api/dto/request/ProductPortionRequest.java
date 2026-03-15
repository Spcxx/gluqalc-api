package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request payload for defining a specific portion size for a product")
public record ProductPortionRequest(
        @Schema(description = "The display name of the portion", example = "1 slice")
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must be at most 50 characters long")
        String name,
        @Schema(description = "The exact weight of this portion in grams", example = "25.50")
        @NotNull(message = "Weight in grams is required")
        @Positive(message = "Weight in grams must be a positive number")
        @Digits(integer = 5, fraction = 2, message = "Invalid weight format")
        BigDecimal weightInGrams
) {
}