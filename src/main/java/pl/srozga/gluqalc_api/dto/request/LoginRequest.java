package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

@Schema(description = "Request payload for standard email and password authentication")
public record LoginRequest(
        @Schema(description = "User's registered email address", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
        @Email
        String email,
        @Schema(description = "User's password", example = "MySecretPass123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        String password,
        @Schema(description = "Optional unique identifier for the user's device, used for session management", example = "android-xyz-123", nullable = true)
        @Nullable
        @Size(max = 255, message = "Device ID must be at most 255 characters")
        String deviceId
) {
}