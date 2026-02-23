package pl.srozga.gluqalc_api.integration;

import pl.srozga.gluqalc_api.dto.internal.ProductDto;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public interface ProductProvider {
    Optional<ProductDto> getProductByBarcode(String barcode);
    List<ProductDto> searchProducts(String query, Locale locale, int limit);
}
