package pl.srozga.gluqalc_api.dto.response;

import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String brand,
        String barcode,
        ProductNutritionResponse nutrition,
        List<ProductPortionResponse> portions
) {
}
