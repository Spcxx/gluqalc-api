package pl.srozga.gluqalc_api.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record MealEntryResponse(
        UUID id,
        UUID productId,
        String productName,
        String brand,
        String barcode,
        MealEntryPortionResponse portion,
        MealEntryNutritionResponse nutrition,
        InsulinDoseResponse insulinDose,
        LocalDate consumptionDate,
        LocalTime consumptionTime
) {
}
