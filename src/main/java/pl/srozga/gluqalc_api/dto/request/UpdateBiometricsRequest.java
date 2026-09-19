package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "Request payload for partially updating biometric parameters (weight, height, body fat)")
public record UpdateBiometricsRequest(
        @Schema(description = "Current body weight in kilograms", example = "75.50")
        @Positive(message = "Weight must be positive")
        @Digits(integer = 3, fraction = 2, message = "Invalid weight format")
        BigDecimal weightInKg,
        @Schema(description = "Height in centimeters", example = "180.0")
        @Positive(message = "Height must be positive")
        @Digits(integer = 3, fraction = 1, message = "Invalid height format")
        BigDecimal heightInCm,
        @Schema(description = "Body fat percentage (0-100)", example = "15.5")
        @Positive(message = "Body fat percentage must be positive")
        @DecimalMin(value = "0.0", message = "Body fat percentage must be at least 0")
        @DecimalMax(value = "100.0", message = "Body fat percentage cannot exceed 100")
        @Digits(integer = 3, fraction = 1)
        BigDecimal bodyFatPercentage
) {}