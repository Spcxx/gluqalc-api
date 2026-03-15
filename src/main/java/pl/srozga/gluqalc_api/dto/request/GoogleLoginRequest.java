package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

@Schema(description = "Request payload for authenticating via Google")
public record GoogleLoginRequest(
        @Schema(description = "The Google OAuth2 ID Token obtained from the Google Sign-In client", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1Njc4OTAifQ...")
        @NotBlank(message = "ID Token cannot be blank")
        @Size(max = 5000, message = "ID Token is too long")
        String idToken,
        @Schema(description = "Optional unique identifier for the user's device, used for session management", example = "android-xyz-123", nullable = true)
        @Nullable
        @Size(max = 255, message = "Device ID must be at most 255 characters")
        String deviceId
) {
}