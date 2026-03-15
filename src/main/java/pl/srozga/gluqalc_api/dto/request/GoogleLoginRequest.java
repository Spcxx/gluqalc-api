package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record GoogleLoginRequest(
        @NotBlank(message = "ID Token cannot be blank")
        @Size(max = 5000, message = "ID Token is too long")
        String idToken,
        @Nullable
        @Size(max = 255, message = "Device ID must be at most 255 characters")
        String deviceId
) {
}
