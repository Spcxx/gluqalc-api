package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;
import pl.srozga.gluqalc_api.validation.ValidMacronutrients;
import pl.srozga.gluqalc_api.validation.interfaces.MacronutrientsData;

import java.math.BigDecimal;

@Schema(description = "Request payload for updating an existing product or proposing a modification. Only provided fields will be updated.")
@ValidMacronutrients
public record UpdateProductRequest(
        @Schema(description = "Product name", example = "Milk", nullable = true)
        @Nullable
        @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters long")
        String name,
        @Schema(description = "Brand or manufacturer", example = "Producer", nullable = true)
        @Nullable
        @Size(max = 100, message = "Brand must be at most 100 characters long")
        String brand,
        @Schema(description = "Product barcode", example = "5411188110485", nullable = true)
        @Nullable
        @Pattern(regexp = "^\\d{8,14}$", message = "Barcode must consist of 8 to 14 digits")
        String barcode,
        @Schema(description = "Energy value per 100g/100ml in kcal", example = "44.00", nullable = true)
        @Nullable
        @PositiveOrZero(message = "Energy cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid energy format")
        BigDecimal energyKcal,
        @Schema(description = "Total carbohydrates per 100g/100ml in grams", example = "6.80", nullable = true)
        @Nullable
        @PositiveOrZero(message = "Carbohydrates cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid carbohydrates format")
        BigDecimal carbohydrates,
        @Schema(description = "Sugars per 100g/100ml in grams", example = "3.30", nullable = true)
        @Nullable
        @PositiveOrZero(message = "Of which sugars cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid sugars format")
        BigDecimal sugars,
        @Schema(description = "Total fat per 100g/100ml in grams", example = "1.50", nullable = true)
        @Nullable
        @PositiveOrZero(message = "Fat cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid fat format")
        BigDecimal fat,
        @Schema(description = "Saturated fat per 100g/100ml in grams", example = "0.10", nullable = true)
        @Nullable
        @PositiveOrZero(message = "Of which saturated fat cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid saturated fat format")
        BigDecimal saturatedFat,
        @Schema(description = "Protein per 100g/100ml in grams", example = "0.30", nullable = true)
        @Nullable
        @PositiveOrZero(message = "Protein cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid protein format")
        BigDecimal protein,
        @Schema(description = "Dietary fiber per 100g/100ml in grams", example = "1.40", nullable = true)
        @Nullable
        @PositiveOrZero(message = "Fiber cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid fiber format")
        BigDecimal fiber,
        @Schema(description = "Salt equivalent per 100g/100ml in grams", example = "0.1000", nullable = true)
        @Nullable
        @PositiveOrZero(message = "Salt cannot be negative")
        @Digits(integer = 4, fraction = 4, message = "Invalid salt format")
        BigDecimal salt,
        @Schema(description = "Glycemic Index (0-100)", example = "30", nullable = true)
        @Nullable
        @Min(value = 0, message = "Glycemic index cannot be less than 0")
        @Max(value = 100, message = "Glycemic index cannot be greater than 100")
        Integer glycemicIndex,
        @Schema(description = "Flag indicating if the product is published (Admin only)", example = "true", nullable = true)
        @Nullable
        Boolean published
) implements MacronutrientsData {
}