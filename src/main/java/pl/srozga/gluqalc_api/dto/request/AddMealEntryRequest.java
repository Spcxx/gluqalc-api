package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AddMealEntryRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,
        @NotNull(message = "Meal Category ID is required")
        UUID mealCategoryId,
        @NotNull(message = "Date is required")
        LocalDate date,
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        @Digits(integer = 4, fraction = 2, message = "Quantity must have max 4 digits and 2 decimals")
        BigDecimal quantity,
        UUID portionId
) {
}