package pl.srozga.gluqalc_api.dto.internal;

import java.math.BigDecimal;

public record DiabetesCalcDataDto(
        BigDecimal carbUnit,        // carbohydrate units (10g per unit)
        BigDecimal fatProteinUnit,  // fat protein units (100kcal from fat/protein per unit)
        BigDecimal carbDose,        // dose for carbohydrates
        BigDecimal fatProteinDose,  // dose for fat and protein
        BigDecimal totalDose,
        Integer bolusDurationMinutes,
        String description
) {
    public static DiabetesCalcDataDto empty() {
        return new DiabetesCalcDataDto(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0, ""
        );
    }
}
