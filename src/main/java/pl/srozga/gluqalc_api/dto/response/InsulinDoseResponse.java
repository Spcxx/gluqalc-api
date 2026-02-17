package pl.srozga.gluqalc_api.dto.response;

import java.math.BigDecimal;

public record InsulinDoseResponse(
        BigDecimal carbUnit,        // carbohydrate units (10g per unit)
        BigDecimal fatProteinUnit,  // fat protein units (100kcal from fat/protein per unit)
        BigDecimal carbDose,        // dose for carbohydrates
        BigDecimal fatProteinDose,  // dose for fat and protein
        BigDecimal totalDose,
        Integer bolusDurationMinutes,
        String description
) {
}
