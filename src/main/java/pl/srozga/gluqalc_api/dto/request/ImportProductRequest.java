package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ImportProductRequest(
        @NotBlank(message = "Barcode cannot be empty")
        @Pattern(regexp = "^\\d{8,14}$", message = "Barcode must consist of 8 to 14 digits")
        String barcode
) {
}
