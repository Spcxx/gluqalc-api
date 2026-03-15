package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Schema(description = "Detailed information about a single food item logged in the meal journal")
public record MealEntryResponse(
        @Schema(description = "Unique ID of the log entry", example = "a1b2c3d4-e5f6-7g8h-9i0j-1234567890ab")
        UUID id,
        @Schema(description = "ID of the product associated with this entry", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID productId,
        @Schema(description = "Name of the product at the time of logging", example = "Whole Grain Bread")
        String productName,
        @Schema(description = "Brand name of the product", example = "Bakery")
        String brand,
        @Schema(description = "Product barcode", example = "5901234567890")
        String barcode,
        @Schema(description = "Details about the consumed portion")
        MealEntryPortionResponse portion,
        @Schema(description = "Calculated nutritional data for this entry")
        MealEntryNutritionResponse nutrition,
        @Schema(description = "Estimated insulin requirements for this entry")
        InsulinDoseResponse insulinDose,
        @Schema(description = "Date of consumption", example = "2026-03-15")
        LocalDate consumptionDate,
        @Schema(description = "Time of consumption (UTC)", example = "08:30:00")
        LocalTime consumptionTime
) {
}