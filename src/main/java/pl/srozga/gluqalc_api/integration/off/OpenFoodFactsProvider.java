package pl.srozga.gluqalc_api.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.internal.ProductNutritionDto;
import pl.srozga.gluqalc_api.dto.internal.ProductPortionDto;
import pl.srozga.gluqalc_api.integration.off.dto.OffProduct;
import pl.srozga.gluqalc_api.integration.off.dto.OffResponse;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenFoodFactsProvider implements ProductProvider {
    private final RestClient restClient;
    public static final pl.srozga.gluqalc_api.common.ProductProvider PROVIDER = pl.srozga.gluqalc_api.common.ProductProvider.OFF_API;

    @Override
    public pl.srozga.gluqalc_api.common.ProductProvider getProductProvider() {
        return PROVIDER;
    }

    @Override
    public Optional<ProductDto> getProductByBarcode(String barcode) {
        try {
            OffResponse resonse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/product/{barcode}")
                            .queryParam("fields", "code,product_name,brands,nutriments")
                            .build(barcode)
                    ).retrieve()
                    .body(OffResponse.class);
            return Optional.ofNullable(mapToProductDto(resonse));
        } catch (Exception e) {
            log.warn("Failed to fetch product from OpenFoodFacts for barcode {}: {}", barcode, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<ProductDto> searchProducts(String query) {
        return Collections.emptyList();
    }

    private ProductDto mapToProductDto(OffResponse response) {
        if (response == null || response.status() != 1 || response.product() == null)
            return null;

        OffProduct p = response.product();
        ProductNutritionDto nutrition = new ProductNutritionDto(
                p.nutriments().energyKcal(),
                p.nutriments().carbohydrates(),
                p.nutriments().sugars(),
                p.nutriments().fat(),
                p.nutriments().saturatedFat(),
                p.nutriments().protein(),
                p.nutriments().fiber(),
                p.nutriments().salt(),
                null
        );

        ProductPortionDto defaultPortion = new ProductPortionDto(
                null,
                "100g",
                new BigDecimal("100"),
                null,
                true,
                null
        );

        return new ProductDto(
                null,
                p.productName(),
                p.brands(),
                response.code(),
                nutrition,
                List.of(defaultPortion),
                false,
                null,
                null,
                null
        );
    }
}
