package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductPortionRequest(
        @NotNull String name,
        @NotNull @Positive BigDecimal weightInGrams
) {
}
