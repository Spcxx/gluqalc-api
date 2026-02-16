package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

public record EditProductRequest(
        @Nullable String name,
        @Nullable String brand,
        @Nullable String barcode,
        @Nullable @PositiveOrZero BigDecimal energyKcal,
        @Nullable @PositiveOrZero BigDecimal carbohydrates,
        @Nullable @PositiveOrZero BigDecimal sugars,
        @Nullable @PositiveOrZero BigDecimal fat,
        @Nullable @PositiveOrZero BigDecimal saturatedFat,
        @Nullable @PositiveOrZero BigDecimal protein,
        @Nullable @PositiveOrZero BigDecimal fiber,
        @Nullable @PositiveOrZero BigDecimal salt,
        @Nullable @Min(0) Integer glycemicIndex,
        @Nullable Boolean published
) {
}
