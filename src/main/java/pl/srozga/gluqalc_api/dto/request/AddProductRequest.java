package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;
import pl.srozga.gluqalc_api.validation.ValidMacronutrients;
import pl.srozga.gluqalc_api.validation.interfaces.MacronutrientsData;

import java.math.BigDecimal;
import java.util.List;

@ValidMacronutrients
public record AddProductRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must be at most 200 characters long")
        String name,
        @Nullable
        @Size(max = 100, message = "Brand must be at most 100 characters long")
        String brand,
        @Nullable
        @Pattern(regexp = "^\\d{8,14}$", message = "Barcode must consist of 8 to 14 digits")
        String barcode,
        @NotNull(message = "Energy is required")
        @PositiveOrZero(message = "Energy cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid energy format")
        BigDecimal energyKcal,
        @NotNull(message = "Carbohydrates are required")
        @PositiveOrZero(message = "Carbohydrates cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid carbohydrates format")
        BigDecimal carbohydrates,
        @Nullable
        @PositiveOrZero(message = "Of which sugars cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid sugars format")
        BigDecimal sugars,
        @NotNull(message = "Fat is required")
        @PositiveOrZero(message = "Fat cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid fat format")
        BigDecimal fat,
        @Nullable
        @PositiveOrZero(message = "Of which saturated fat cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid saturated fat format")
        BigDecimal saturatedFat,
        @NotNull(message = "Protein is required")
        @PositiveOrZero(message = "Protein cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid protein format")
        BigDecimal protein,
        @Nullable
        @PositiveOrZero(message = "Fiber cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Invalid fiber format")
        BigDecimal fiber,
        @Nullable
        @PositiveOrZero(message = "Salt cannot be negative")
        @Digits(integer = 4, fraction = 4, message = "Invalid salt format")
        BigDecimal salt,
        @Nullable
        @Min(value = 0, message = "Glycemic index cannot be less than 0")
        @Max(value = 100, message = "Glycemic index cannot be greater than 100")
        Integer glycemicIndex,
        @Nullable
        @Valid
        List<ProductPortionRequest> portions
) implements MacronutrientsData {
}
