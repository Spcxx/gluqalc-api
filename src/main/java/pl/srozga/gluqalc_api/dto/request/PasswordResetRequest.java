package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for initiating the password reset process")
public record PasswordResetRequest(
        @Schema(description = "The email address of the account that needs a password reset", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
        @Email(message = "Invalid email format")
        String email
) {
}