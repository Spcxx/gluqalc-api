package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Schema(description = "Request payload for logging a new meal entry")
public record AddMealEntryRequest(
        @Schema(description = "ID of the product being logged", example = "a1b2c3d4-e5f6-7g8h-9i0j-1234567890ab")
        @NotNull(message = "Product ID is required")
        UUID productId,
        @Schema(description = "ID of the meal category where this entry belongs", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "Meal Category ID is required")
        UUID mealCategoryId,
        @Schema(description = "Date when the meal was consumed in ISO format", example = "2026-03-15")
        @NotNull(message = "Date is required")
        LocalDate date,
        @Schema(description = "Time when the meal was consumed", example = "10:30:00")
        @NotNull(message = "Time is required")
        LocalTime time,
        @Schema(description = "Quantity of the product consumed. Represents either exact grams (if no portionId is provided) or the multiplier for the selected portion", example = "1.5")
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        @Digits(integer = 4, fraction = 2, message = "Quantity must have max 4 digits and 2 decimals")
        BigDecimal quantity,
        @Schema(description = "Optional ID of a specific product portion. If null, quantity is treated as grams", example = "987fcdeb-51a2-43d7-9012-3456789abcde", nullable = true)
        UUID portionId
) {
}