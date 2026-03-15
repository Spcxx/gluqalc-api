package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload for importing a product from an external provider")
public record ImportProductRequest(
        @Schema(description = "The barcode of the product to import", example = "5411188110485")
        @NotBlank(message = "Barcode cannot be empty")
        @Pattern(regexp = "^\\d{8,14}$", message = "Barcode must consist of 8 to 14 digits")
        String barcode
) {
}