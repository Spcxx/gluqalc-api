package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductPortionRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must be at most 50 characters long")
        String name,
        @NotNull(message = "Weight in grams is required")
        @Positive(message = "Weight in grams must be a positive number")
        @Digits(integer = 5, fraction = 2, message = "Invalid weight format")
        BigDecimal weightInGrams
) {
}
