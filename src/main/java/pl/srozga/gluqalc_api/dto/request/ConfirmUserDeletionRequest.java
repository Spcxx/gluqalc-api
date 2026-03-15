package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload for finalizing account deletion")
public record ConfirmUserDeletionRequest(
        @Schema(description = "The 6-digit verification code sent to the user's email to confirm deletion", example = "654321")
        @NotBlank(message = "Verification code is required")
        @Pattern(regexp = "^\\d{6}$", message = "Code must be exactly 6 digits")
        String code
) {
}