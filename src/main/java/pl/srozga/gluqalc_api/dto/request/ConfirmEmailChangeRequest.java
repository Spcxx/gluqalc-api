package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for confirming an email change using a verification code")
public record ConfirmEmailChangeRequest(
        @Schema(description = "The new email address that the verification code was sent to", example = "new.email@example.com")
        @NotBlank(message = "New email is required")
        @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
        @Email(message = "Invalid new email format")
        String newEmail,
        @Schema(description = "The 6-digit verification code received at the new email address", example = "123456")
        @NotBlank(message = "Verification code is required")
        @Pattern(regexp = "^\\d{6}$", message = "Code must be exactly 6 digits")
        String code
) {
}