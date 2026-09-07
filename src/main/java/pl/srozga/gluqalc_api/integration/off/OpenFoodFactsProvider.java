package pl.srozga.gluqalc_api.integration.off;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.srozga.gluqalc_api.common.ProductProviderType;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.internal.ProductNutritionDto;
import pl.srozga.gluqalc_api.dto.internal.ProductPortionDto;
import pl.srozga.gluqalc_api.dto.internal.ProductSourceMetadataDto;
import pl.srozga.gluqalc_api.integration.ProductProvider;
import pl.srozga.gluqalc_api.integration.off.dto.OffProduct;
import pl.srozga.gluqalc_api.integration.off.dto.OffResponse;
import pl.srozga.gluqalc_api.integration.off.dto.OffSearchResponse;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenFoodFactsProvider implements ProductProvider {
    private final RestClient restClient;
    public static final ProductProviderType PROVIDER = ProductProviderType.OFF;

    private static final Set<String> TOTAL_CARBS_COUNTRIES = Set.of(
            "en:united-states", "en:canada", "en:japan",
            "en:south-korea", "en:republic-of-korea", "en:taiwan",
            "en:singapore", "en:philippines", "en:hong-kong"
    );

    @Override
    @Cacheable(value = "off_barcode_cache", key = "#barcode", unless = "#result == null")
    public Optional<ProductDto> getProductByBarcode(String barcode) {
        try {
            OffResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v2/product/{barcode}")
                            .queryParam("fields", "code,product_name,brands,nutriments")
                            .build(barcode)
                    ).retrieve()
                    .body(OffResponse.class);
            if (response != null && response.product() != null)
                return Optional.ofNullable(mapInternal(response.product(), response.code()));
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to fetch product from OpenFoodFacts for barcode {}: {}", barcode, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Cacheable(
            value = "off_search_cache",
            key = "#query.toLowerCase() + '_' + #locale.getLanguage() + '_' + #limit"
    )
    public List<ProductDto> searchProducts(String query, Locale locale, int limit) {
        String languageCode = locale.getLanguage();
        if (languageCode.isEmpty())
            languageCode = "en";

        String finalLanguageCode = languageCode.toLowerCase();

        OffSearchResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cgi/search.pl")
                        .queryParam("search_terms", query)
                        .queryParam("search_simple", "1")
                        .queryParam("action", "process")
                        .queryParam("json", "1")
                        .queryParam("sort_by", "unique_scans_n")
                        .queryParam("lc", finalLanguageCode)
                        .queryParam("cc", finalLanguageCode)
                        .queryParam("page_size", limit)
                        .queryParam("fields", "code,product_name,brands,nutriments")
                        .build()
                ).retrieve()
                .body(OffSearchResponse.class);

        if (response == null || response.products() == null)
            return Collections.emptyList();

        return response.products().stream()
                .map(p -> {
                    String effectiveBarcode = p.code() != null ? p.code() : p._id();
                    return mapInternal(p, effectiveBarcode);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private ProductDto mapInternal(OffProduct p, String barcode) {
        String name = p.productName();
        if (name == null || name.isBlank())
            name = "Unknown product (" + barcode + ")";

        if (p.nutriments() == null)
            return null;

        BigDecimal carbs = p.nutriments().carbohydrates();
        BigDecimal fiber = p.nutriments().fiber();
        BigDecimal sugars = p.nutriments().sugars() != null ? p.nutriments().sugars() : BigDecimal.ZERO;
        if (carbs != null && fiber != null && p.countriesTags() != null) {
            boolean isTotalCarbsCountry = p.countriesTags().stream()
                                .filter(Objects::nonNull)
                                .anyMatch(TOTAL_CARBS_COUNTRIES::contains);

            if (isTotalCarbsCountry) {
                BigDecimal carbsAfterSubtraction = carbs.subtract(fiber);
                if (carbsAfterSubtraction.compareTo(sugars) >= 0) {
                    carbs = carbsAfterSubtraction;
                }
            }
        }

        ProductNutritionDto nutrition = new ProductNutritionDto(
                p.nutriments().energyKcal(),
                carbs,
                p.nutriments().sugars(),
                p.nutriments().fat(),
                p.nutriments().saturatedFat(),
                p.nutriments().protein(),
                fiber,
                p.nutriments().salt(),
                null
        );

        ProductPortionDto defaultPortion = new ProductPortionDto(
                null, "100g", new BigDecimal("100"), null, true, null
        );

        return new ProductDto(
                null,
                name,
                p.brands(),
                barcode,
                nutrition,
                List.of(defaultPortion),
                List.of(),
                false,
                null,
                null,
                null,
                new ProductSourceMetadataDto(
                        "OPEN_FOOD_FACTS",
                        "Open Database License (ODbL) v1.0",
                        "https://opendatacommons.org/licenses/odbl/1-0/",
                        "https://world.openfoodfacts.org/product/" + barcode,
                        "Nutritional data sourced from the Open Food Facts collaborative database under the Open Database License (ODbL)."
                ),
                PROVIDER
        );
    }
}
