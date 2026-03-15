package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Detailed information about a legal consent or policy requirement")
public record ConsentResponse(
        @Schema(description = "Unique identifier of the consent definition", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Schema(description = "Unique internal code for the consent", example = "TERMS_AND_CONDITIONS")
        String code,
        @Schema(description = "Human-readable description or content of the consent", example = "I agree to the terms of service and privacy policy...")
        String description,
        @Schema(description = "Version of the consent definition", example = "1.0.2")
        String version,
        @Schema(description = "Indicates if this consent is mandatory to use the application", example = "true")
        boolean required
) {
}