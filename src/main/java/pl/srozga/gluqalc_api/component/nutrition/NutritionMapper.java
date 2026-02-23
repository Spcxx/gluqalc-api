package pl.srozga.gluqalc_api.component.nutrition;

import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.internal.ProductNutritionDto;
import pl.srozga.gluqalc_api.dto.internal.ProductPortionDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class NutritionMapper {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public List<ProductPortionDto> addNutritionToPortions(ProductDto base, List<ProductPortionDto> portions) {
        return portions.stream().map(portion -> {
            if (portion.weightInGrams() == null)
                return portion;

            BigDecimal multiplier = portion.weightInGrams().divide(HUNDRED, 4, RoundingMode.HALF_UP);
            ProductNutritionDto baseNutrition = base.nutrition();
            if (baseNutrition == null)
                return portion;

            ProductNutritionDto calculatedNutrition = new ProductNutritionDto(
                    calculate(baseNutrition.energyKcal(), multiplier, 1),
                    calculate(baseNutrition.carbohydrates(), multiplier, 1),
                    calculate(baseNutrition.sugars(), multiplier, 1),
                    calculate(baseNutrition.fat(), multiplier, 1),
                    calculate(baseNutrition.saturatedFat(), multiplier, 1),
                    calculate(baseNutrition.protein(), multiplier, 1),
                    calculate(baseNutrition.fiber(), multiplier, 1),
                    calculate(baseNutrition.salt(), multiplier, 2),
                    baseNutrition.glycemicIndex()
            );
            return new ProductPortionDto(
                    portion.id(),
                    portion.name(),
                    portion.weightInGrams(),
                    calculatedNutrition,
                    portion.published(),
                    portion.createdBy()
            );
        }).toList();
    }

    private BigDecimal calculate(BigDecimal baseValue, BigDecimal multiplier, int scale) {
        if (baseValue == null) return null;
        return baseValue.multiply(multiplier).setScale(scale, RoundingMode.HALF_UP);
    }
}
