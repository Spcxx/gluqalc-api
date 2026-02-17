package pl.srozga.gluqalc_api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.srozga.gluqalc_api.common.UserRole;
import pl.srozga.gluqalc_api.component.product.ProductMapper;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.request.AddProductRequest;
import pl.srozga.gluqalc_api.dto.request.ImportProductRequest;
import pl.srozga.gluqalc_api.dto.request.ProductPortionRequest;
import pl.srozga.gluqalc_api.dto.request.UpdateProductRequest;
import pl.srozga.gluqalc_api.dto.response.PortionChangeResponse;
import pl.srozga.gluqalc_api.dto.response.ProductAdminResponse;
import pl.srozga.gluqalc_api.dto.response.ProductChangeResponse;
import pl.srozga.gluqalc_api.dto.response.ProductResponse;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.ProductService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    // PRODUCT ENDPOINTS

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createProduct(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody AddProductRequest request
    ) {
        Object result;
        if (user.roles().contains(UserRole.ADMIN)) {
            ProductDto productDto = productService.createProduct(user.id(), request);
            result = productMapper.toAdminResponse(productDto);
        } else {
            ProductDto productDto = productService.proposeProduct(user.id(), request);
            result = productMapper.toResponse(productDto);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProduct(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID id
    ) {
        ProductDto productDto = productService.getProductSmart(id, user);

        Object result;
        if (user.roles().contains(UserRole.ADMIN))
            result = productMapper.toAdminResponse(productDto);
        else
            result = productMapper.toResponse(productDto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/raw")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductAdminResponse getProductRaw(
            @PathVariable UUID id
    ) {
        ProductDto productDto = productService.getProductById(id);
        return productMapper.toAdminResponse(productDto);
    }

    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductByBarcode(@AuthenticationPrincipal AuthUser user, @PathVariable String barcode) {
        ProductDto productDto = productService.getProductSmartByBarcode(barcode, user);

        Object result;
        if (user.roles().contains(UserRole.ADMIN))
            result = productMapper.toAdminResponse(productDto);
        else
            result = productMapper.toResponse(productDto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/barcode/{barcode}/raw")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductAdminResponse getProductByBarcodeRaw(
            @PathVariable String barcode
    ) {
        ProductDto productDto = productService.getProductByBarcode(barcode);
        return productMapper.toAdminResponse(productDto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        if (user.roles().contains(UserRole.ADMIN)) {
            ProductDto updatedProduct = productService.updateProduct(id, request);
            return ResponseEntity.ok(productMapper.toAdminResponse(updatedProduct));
        } else {
            productService.proposeProductChange(id, user.id(), request);
            return ResponseEntity.accepted().build();
        }
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void approveProduct(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id) {
        productService.approveWholeProduct(user.id(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable UUID id) {
        productService.softDeleteProduct(id);
    }

    // PORTION ENDPOINTS

    @PostMapping("/{productId}/portions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addPortion(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductPortionRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        if (user.roles().contains(UserRole.ADMIN)) {
            ProductDto productDto = productService.addPortion(productId, request.name(), request.weightInGrams(), user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productMapper.toAdminResponse(productDto));
        } else {
            productService.addPortionUser(productId, request.name(), request.weightInGrams(), user);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
    }

    @PatchMapping("/portions/{portionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePortion(
            @PathVariable UUID portionId,
            @Valid @RequestBody ProductPortionRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        if (user.roles().contains(UserRole.ADMIN)) {
            ProductDto productDto = productService.updatePortion(portionId, request.name(), request.weightInGrams());
            return ResponseEntity.ok(productMapper.toAdminResponse(productDto));
        } else {
            productService.updatePortionUser(portionId, request.name(), request.weightInGrams(), user);
            return ResponseEntity.accepted().build();
        }
    }

    @DeleteMapping("/portions/{portionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void deletePortion(
            @PathVariable UUID portionId,
            @AuthenticationPrincipal AuthUser user
    ) {
        if (user.roles().contains(UserRole.ADMIN))
            productService.deletePortion(portionId);
        else
            productService.deletePortionUser(portionId, user);
    }

    // CHANGES ENDPOINT

    @GetMapping("/changes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductChangeResponse> getPendingProductChanges() {
        return productService.getPendingProductChanges();
    }

    @GetMapping("/portions/changes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PortionChangeResponse> getPendingPortionChanges() {
        return productService.getPendingPortionChanges();
    }

    @PostMapping("/changes/{changeId}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void approveProductChange(@PathVariable UUID changeId) {
        productService.approveProductChange(changeId);
    }

    @PostMapping("/portions/changes/{changeId}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void approvePortionChange(@PathVariable UUID changeId) {
        productService.approvePortionChange(changeId);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<?>> searchUnified(
            @Size(min = 3, max = 64) @RequestParam String q,
            @AuthenticationPrincipal AuthUser user,
            Locale locale,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductDto> result = productService.searchProductsUnified(q, user, locale, pageable);
        if (user.roles().contains(UserRole.ADMIN)) {
            Page<ProductAdminResponse> adminResponses = result.map(productMapper::toAdminResponse);
            return ResponseEntity.ok(adminResponses);
        } else {
            Page<ProductResponse> responses = result.map(productMapper::toResponse);
            return ResponseEntity.ok(responses);
        }
    }

    @PostMapping("/import")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> importProduct(
            @Valid @RequestBody ImportProductRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        ProductDto productDto = productService.importProduct(request.barcode(), user);
        if (user.roles().contains(UserRole.ADMIN))
            return ResponseEntity.ok(productMapper.toAdminResponse(productDto));
        else
            return ResponseEntity.ok(productMapper.toResponse(productDto));
    }
}