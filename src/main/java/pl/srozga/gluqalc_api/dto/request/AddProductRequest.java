package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;
import pl.srozga.gluqalc_api.validation.ValidMacronutrients;
import pl.srozga.gluqalc_api.validation.interfaces.MacronutrientsData;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Request payload for creating a new product or proposing a product addition")
@ValidMacronutrients
public record AddProductRequest(
        @Schema(description = "Product name", example = "Milk")
        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must be at most 200 characters long")
        String name,
        @Schema(description = "Brand or manufacturer of the product", example = "Producer", nullable = true)
        @Nullable
        @Size(max = 100, message = "Brand must be at most 100 characters long")
        String brand,
        @Schema(description = "Product barcode (EAN-8 or EAN-13 typically)", example = "5411188110485", nullable = true)
        @Nullable
        @Pattern(regexp = "^\\d{8,14}$", message = "Barcode must consist of 8 to 14 digits")
        String barcode,
        @Schema(description = "Energy value per 100g/100ml in kcal", example = "44.00")
        @NotNull(message = "Energy is required")
        @PositiveOrZero(message = "Energy cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid energy format")
        BigDecimal energyKcal,
        @Schema(description = "Total carbohydrates per 100g/100ml in grams", example = "6.80")
        @NotNull(message = "Carbohydrates are required")
        @PositiveOrZero(message = "Carbohydrates cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid carbohydrates format")
        BigDecimal carbohydrates,
        @Schema(description = "Sugars (part of carbohydrates) per 100g/100ml in grams", example = "3.30", nullable = true)
        @Nullable
        @PositiveOrZero(message = "Of which sugars cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid sugars format")
        BigDecimal sugars,
        @Schema(description = "Total fat per 100g/100ml in grams", example = "1.50")
        @NotNull(message = "Fat is required")
        @PositiveOrZero(message = "Fat cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid fat format")
        BigDecimal fat,
        @Schema(description = "Saturated fat (part of total fat) per 100g/100ml in grams", example = "0.10", nullable = true)
        @Nullable
        @PositiveOrZero(message = "Of which saturated fat cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid saturated fat format")
        BigDecimal saturatedFat,
        @Schema(description = "Protein per 100g/100ml in grams", example = "0.30")
        @NotNull(message = "Protein is required")
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
        @Schema(description = "Glycemic Index (GI) of the product (0-100)", example = "30", nullable = true)
        @Nullable
        @Min(value = 0, message = "Glycemic index cannot be less than 0")
        @Max(value = 100, message = "Glycemic index cannot be greater than 100")
        Integer glycemicIndex,
        @Schema(description = "Optional list of predefined portion sizes for this product", nullable = true)
        @Nullable
        @Valid
        List<ProductPortionRequest> portions
) implements MacronutrientsData {
}