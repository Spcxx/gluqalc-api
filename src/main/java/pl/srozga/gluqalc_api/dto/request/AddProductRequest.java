package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public record AddProductRequest(
        @NotBlank String name,
        @Nullable String brand,
        @Nullable String barcode,
        @NotNull @PositiveOrZero BigDecimal energyKcal,
        @NotNull @PositiveOrZero BigDecimal carbohydrates,
        @Nullable @PositiveOrZero BigDecimal sugars,
        @NotNull @PositiveOrZero BigDecimal fat,
        @Nullable @PositiveOrZero BigDecimal saturatedFat,
        @NotNull @PositiveOrZero BigDecimal protein,
        @Nullable @PositiveOrZero BigDecimal fiber,
        @Nullable @PositiveOrZero BigDecimal salt,
        @Nullable @Min(0) Integer glycemicIndex,
        @Nullable @Valid List<ProductPortionRequest> portions
) {
}
