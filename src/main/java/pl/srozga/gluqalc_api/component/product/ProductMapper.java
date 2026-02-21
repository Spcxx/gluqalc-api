package pl.srozga.gluqalc_api.component.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.common.ProductProviderType;
import pl.srozga.gluqalc_api.component.diabetes.DiabetesCalculator;
import pl.srozga.gluqalc_api.component.diabetes.DiabetesMapper;
import pl.srozga.gluqalc_api.component.nutrition.NutritionMapper;
import pl.srozga.gluqalc_api.dto.internal.DiabetesCalcDataDto;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.internal.ProductNutritionDto;
import pl.srozga.gluqalc_api.dto.internal.ProductPortionDto;
import pl.srozga.gluqalc_api.dto.response.*;
import pl.srozga.gluqalc_api.entity.Product;
import pl.srozga.gluqalc_api.entity.UserProfile;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final NutritionMapper nutritionMapper;
    private final DiabetesMapper diabetesMapper;
    private final DiabetesCalculator diabetesCalculator;

    public ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getBarcode(),
                mapInternalNutrition(product),
                product.getPortions().stream()
                        .map(portion -> new ProductPortionDto(
                                portion.getId(),
                                portion.getName(),
                                portion.getWeightInGrams(),
                                null,
                                portion.isPublished(),
                                portion.getCreatedBy()
                        ))
                        .toList(),

                product.isPublished(),
                product.getCreatedBy(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                ProductProviderType.LOCAL
        );
    }

    public ProductAdminResponse toAdminResponse(ProductDto product) {
        List<ProductPortionDto> portions = nutritionMapper.addNutritionToPortions(product, product.portions());

        return new ProductAdminResponse(
                product.id(),
                product.name(),
                product.brand(),
                product.barcode(),
                mapResponseNutrition(product.nutrition()),
                mapPortions(portions, null, LocalTime.now()),
                product.published(),
                product.createdAt(),
                product.updatedAt(),
                product.source()
        );
    }

    public ProductResponse toResponse(ProductDto product, UserProfile profile) {
        List<ProductPortionDto> portions = nutritionMapper.addNutritionToPortions(product, product.portions());
        LocalTime now = LocalTime.now();

        InsulinDoseResponse baseInsulinDose = calculateInsulin(product.nutrition(), profile, now);

        return new ProductResponse(
                product.id(),
                product.name(),
                product.brand(),
                product.barcode(),
                mapResponseNutrition(product.nutrition()),
                baseInsulinDose,
                mapPortions(portions, profile, now),
                product.source()
        );
    }

    private ProductNutritionDto mapInternalNutrition(Product product) {
        return new ProductNutritionDto(
                product.getEnergyKcal(),
                product.getCarbohydrates(),
                product.getSugars(),
                product.getFat(),
                product.getSaturatedFat(),
                product.getProtein(),
                product.getFiber(),
                product.getSalt(),
                product.getGlycemicIndex()
        );
    }

    private ProductNutritionResponse mapResponseNutrition(ProductNutritionDto dto) {
        if (dto == null) return null;
        return new ProductNutritionResponse(
                dto.energyKcal(),
                dto.carbohydrates(),
                dto.sugars(),
                dto.fat(),
                dto.saturatedFat(),
                dto.protein(),
                dto.fiber(),
                dto.salt(),
                dto.glycemicIndex()
        );
    }

    private List<ProductPortionResponse> mapPortions(List<ProductPortionDto> portions, UserProfile profile, LocalTime time) {
        return portions.stream()
                .map(portion -> {
                    InsulinDoseResponse insulinDose = calculateInsulin(portion.nutrition(), profile, time);
                    return new ProductPortionResponse(
                            portion.id(),
                            portion.name(),
                            portion.weightInGrams(),
                            mapResponseNutrition(portion.nutrition()),
                            insulinDose
                    );
                })
                .toList();
    }

    private InsulinDoseResponse calculateInsulin(ProductNutritionDto nutrition, UserProfile profile, LocalTime time) {
        if (nutrition == null || profile == null)
            return null;

        DiabetesCalcDataDto calcData = diabetesCalculator.calculate(
                nutrition.carbohydrates(),
                nutrition.protein(),
                nutrition.fat(),
                profile,
                time
        );

        return diabetesMapper.toResponse(calcData);
    }
}
