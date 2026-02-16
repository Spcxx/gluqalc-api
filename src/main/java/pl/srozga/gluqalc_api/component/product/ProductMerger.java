package pl.srozga.gluqalc_api.component.product;

import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.internal.ProductNutritionDto;
import pl.srozga.gluqalc_api.dto.internal.ProductPortionDto;
import pl.srozga.gluqalc_api.entity.PortionChange;
import pl.srozga.gluqalc_api.entity.Product;
import pl.srozga.gluqalc_api.entity.ProductChange;
import pl.srozga.gluqalc_api.entity.ProductPortion;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductMerger {
    public ProductDto merge(Product base, ProductChange change, List<ProductPortion> allVisiblePortions, List<PortionChange> portionChanges) {
        boolean hasChange = (change != null);

        String effectiveName = (hasChange && change.getName() != null) ? change.getName() : base.getName();
        String effectiveBrand = (hasChange && change.getBrand() != null) ? change.getBrand() : base.getBrand();
        String effectiveBarcode = (hasChange && change.getBarcode() != null) ? change.getBarcode() : base.getBarcode();
        ProductNutritionDto nutrition = new ProductNutritionDto(
                (hasChange && change.getEnergyKcal() != null) ? change.getEnergyKcal() : base.getEnergyKcal(),
                (hasChange && change.getCarbohydrates() != null) ? change.getCarbohydrates() : base.getCarbohydrates(),
                (hasChange && change.getSugars() != null) ? change.getSugars() : base.getSugars(),
                (hasChange && change.getFat() != null) ? change.getFat() : base.getFat(),
                (hasChange && change.getSaturatedFat() != null) ? change.getSaturatedFat() : base.getSaturatedFat(),
                (hasChange && change.getProtein() != null) ? change.getProtein() : base.getProtein(),
                (hasChange && change.getFiber() != null) ? change.getFiber() : base.getFiber(),
                (hasChange && change.getSalt() != null) ? change.getSalt() : base.getSalt(),
                (hasChange && change.getGlycemicIndex() != null) ? change.getGlycemicIndex() : base.getGlycemicIndex()
        );

        List<PortionChange> safePortionChanges = portionChanges != null ? portionChanges : Collections.emptyList();

        Map<UUID, PortionChange> portionChangeMap = safePortionChanges.stream()
                .collect(Collectors.toMap(PortionChange::getPortionId, pc -> pc));

        List<ProductPortionDto> effectivePortions = allVisiblePortions.stream()
                .map(portion -> {
                    PortionChange pc = portionChangeMap.get(portion.getId());

                    if (pc != null) {
                        return new ProductPortionDto(
                                portion.getId(),
                                (pc.getName() != null) ? pc.getName() : portion.getName(),
                                (pc.getWeightInGrams() != null) ? pc.getWeightInGrams() : portion.getWeightInGrams(),
                                null,
                                portion.isPublished(),
                                portion.getCreatedBy()

                        );
                    } else {
                        return new ProductPortionDto(
                                portion.getId(),
                                portion.getName(),
                                portion.getWeightInGrams(),
                                null,
                                portion.isPublished(),
                                portion.getCreatedBy()
                        );
                    }
                })
                .sorted(Comparator.comparing(ProductPortionDto::weightInGrams))
                .toList();

        return new ProductDto(
                base.getId(),
                effectiveName,
                effectiveBrand,
                effectiveBarcode,
                nutrition,
                effectivePortions,
                base.isPublished(),
                base.getCreatedBy(),
                base.getCreatedAt(),
                base.getUpdatedAt()
        );
    }
}
