package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ImportProductRequest(
        @NotBlank(message = "Barcode cannot be empty")
        String barcode
) {
}
