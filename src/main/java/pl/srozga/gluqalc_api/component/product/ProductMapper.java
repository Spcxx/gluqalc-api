package pl.srozga.gluqalc_api.component.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.component.nutrition.NutritionMapper;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.internal.ProductNutritionDto;
import pl.srozga.gluqalc_api.dto.internal.ProductPortionDto;
import pl.srozga.gluqalc_api.dto.response.ProductAdminResponse;
import pl.srozga.gluqalc_api.dto.response.ProductNutritionResponse;
import pl.srozga.gluqalc_api.dto.response.ProductPortionResponse;
import pl.srozga.gluqalc_api.dto.response.ProductResponse;
import pl.srozga.gluqalc_api.entity.Product;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final NutritionMapper nutritionMapper;

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
                product.getUpdatedAt()
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
                mapPortions(portions),
                product.published(),
                product.createdAt(),
                product.updatedAt()
        );
    }

    public ProductResponse toResponse(ProductDto product) {
        List<ProductPortionDto> portions = nutritionMapper.addNutritionToPortions(product, product.portions());

        return new ProductResponse(
                product.id(),
                product.name(),
                product.brand(),
                product.barcode(),
                mapResponseNutrition(product.nutrition()),
                mapPortions(portions)
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

    private List<ProductPortionResponse> mapPortions(List<ProductPortionDto> portions) {
        return portions.stream()
                .map(portion -> new ProductPortionResponse(
                        portion.id(),
                        portion.name(),
                        portion.weightInGrams(),
                        mapResponseNutrition(portion.nutrition())
                ))
                .toList();
    }
}
