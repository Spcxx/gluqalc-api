package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Historical snapshot of a user's biometric data, used to track weight, body fat, and BMI changes over time")
public record UserProfileHistoryResponse(
        @Schema(description = "Unique identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,
        @Schema(description = "User's body weight in kilograms at the time of the snapshot", example = "75.50")
        BigDecimal weightInKg,
        @Schema(description = "User's height in centimeters at the time of the snapshot", example = "180.0")
        BigDecimal heightInCm,
        @Schema(description = "User's estimated or measured body fat percentage at the time of the snapshot", example = "15.5")
        BigDecimal bodyFatPercentage,
        @Schema(description = "Dynamically calculated Body Mass Index (BMI) based on weight and height from this snapshot", example = "23.3")
        BigDecimal bmi,
        @Schema(description = "Timestamp indicating when this historical record was captured", example = "2026-09-19T14:30:00Z")
        Instant createdAt
) {
}