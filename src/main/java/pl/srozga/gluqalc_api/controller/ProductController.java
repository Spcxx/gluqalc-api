package pl.srozga.gluqalc_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.srozga.gluqalc_api.dto.request.AddProductRequest;
import pl.srozga.gluqalc_api.dto.request.ProductPortionRequest;
import pl.srozga.gluqalc_api.dto.request.UpdateProductRequest;
import pl.srozga.gluqalc_api.dto.response.ProductAdminResponse;
import pl.srozga.gluqalc_api.service.ProductService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductAdminResponse createProduct(@Valid @RequestBody AddProductRequest request) {
        return productService.createProduct(request);
    }

    @GetMapping("/{id}")
    public ProductAdminResponse getProduct(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @GetMapping("/barcode/{barcode}")
    public ProductAdminResponse getProductByBarcode(@PathVariable String barcode) {
        return productService.getProductByBarcode(barcode);
    }

    @PatchMapping("/{id}")
    public ProductAdminResponse updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable UUID id) {
        productService.softDeleteProduct(id);
    }

    @PostMapping("/{productId}/portions")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductAdminResponse addPortion(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductPortionRequest request
    ) {
        return productService.addPortion(productId, request.name(), request.weightInGrams());
    }

    @PatchMapping("/portions/{portionId}")
    public ProductAdminResponse updatePortion(
            @PathVariable UUID portionId,
            @Valid @RequestBody ProductPortionRequest request
    ) {
        return productService.updatePortion(portionId, request.name(), request.weightInGrams());
    }

    @DeleteMapping("/portions/{portionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePortion(@PathVariable UUID portionId) {
        productService.deletePortion(portionId);
    }
}
