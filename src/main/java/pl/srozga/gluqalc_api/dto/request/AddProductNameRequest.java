package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.srozga.gluqalc_api.common.ProductNameType;

@Schema(description = "Request for adding a translated or alternative product name")
public record AddProductNameRequest(
        @Schema(description = "Product name or alias", example = "Ziemniaki")
        @NotBlank
        @Size(max = 255)
        String name,
        @Schema(description = "ISO language code", example = "pl")
        @NotBlank
        @Size(max = 10)
        @Pattern(regexp = "^[a-zA-Z]{2,3}([_-][a-zA-Z]{2,4})?$", message = "Invalid language code")
        String languageCode,
        @Schema(description = "Whether this is a translation or an alternative name", example = "TRANSLATION")
        ProductNameType type
) {
}
