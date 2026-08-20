package pl.srozga.gluqalc_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import pl.srozga.gluqalc_api.component.rateLimit.RateLimit;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.request.AddProductRequest;
import pl.srozga.gluqalc_api.dto.request.AddProductNameRequest;
import pl.srozga.gluqalc_api.dto.request.ImportProductRequest;
import pl.srozga.gluqalc_api.dto.request.ProductPortionRequest;
import pl.srozga.gluqalc_api.dto.request.UpdateProductRequest;
import pl.srozga.gluqalc_api.dto.response.PortionChangeResponse;
import pl.srozga.gluqalc_api.dto.response.ProductAdminResponse;
import pl.srozga.gluqalc_api.dto.response.ProductChangeResponse;
import pl.srozga.gluqalc_api.dto.response.ProductNameResponse;
import pl.srozga.gluqalc_api.dto.response.ProductResponse;
import pl.srozga.gluqalc_api.entity.UserProfile;
import pl.srozga.gluqalc_api.repository.UserProfileRepository;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.ProductService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Tag(name = "07. Products", description = "Endpoints for managing the global product database, user proposals and barcode scanning")
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final UserProfileRepository userProfileRepository;

    @Operation(summary = "Create a product", description = "Creates a new product. Admins create published products directly. Users create proposed products visible only to them until approved.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Conflict: Barcode already exists or invalid macro relations")
    })
    @PostMapping("/products")
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
            UserProfile profile = userProfileRepository.findByUserId(user.id()).orElse(null);
            ProductDto productDto = productService.proposeProduct(user.id(), request);
            result = productMapper.toResponse(productDto, profile);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result);
    }

    @Operation(summary = "Get a product", description = "Retrieves a product by its ID. Users will also see their own pending changes merged into the response.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the product"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: Product does not exist or is not visible to the user")
    })
    @GetMapping("/products/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProduct(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID id
    ) {
        ProductDto productDto = productService.getProductSmart(id, user);
        UserProfile profile = userProfileRepository.findByUserId(user.id()).orElse(null);
        Object result = (user.roles().contains(UserRole.ADMIN)) ? productMapper.toAdminResponse(productDto) : productMapper.toResponse(productDto, profile);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get raw product (Admin)", description = "Retrieves the raw database state of a product without user-specific overrides or calculated insulin data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the raw product"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/admin/products/{id}/raw")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductAdminResponse getProductRaw(
            @PathVariable UUID id
    ) {
        ProductDto productDto = productService.getProductById(id);
        return productMapper.toAdminResponse(productDto);
    }

    @Operation(summary = "Get product by barcode", description = "Retrieves a product by its barcode.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the product"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: No product with this barcode exists or is visible")
    })
    @GetMapping("/products/barcode/{barcode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductByBarcode(@AuthenticationPrincipal AuthUser user, @PathVariable String barcode) {
        ProductDto productDto = productService.getProductSmartByBarcode(barcode, user);
        UserProfile profile = userProfileRepository.findByUserId(user.id()).orElse(null);
        Object result = (user.roles().contains(UserRole.ADMIN)) ? productMapper.toAdminResponse(productDto) : productMapper.toResponse(productDto, profile);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get raw product by barcode (Admin)", description = "Retrieves the raw database state of a product by barcode without user-specific overrides.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the raw product"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/admin/products/barcode/{barcode}/raw")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductAdminResponse getProductByBarcodeRaw(
            @PathVariable String barcode
    ) {
        ProductDto productDto = productService.getProductByBarcode(barcode);
        return productMapper.toAdminResponse(productDto);
    }

    @Operation(summary = "Update a product", description = "Updates product details. Admins apply changes directly. Users create a change proposal for moderation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product successfully updated (Admin only)"),
            @ApiResponse(responseCode = "202", description = "Change proposal successfully submitted (User only)"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: Another product uses this barcode or invalid macro relations")
    })
    @PatchMapping("/products/{id}")
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

    @Operation(summary = "Add a product name", description = "Adds a translated or alternative name. User submissions require administrator approval.")
    @PostMapping("/products/{id}/names")
    @PreAuthorize("isAuthenticated()")
    public ProductNameResponse addProductName(
            @PathVariable UUID id,
            @Valid @RequestBody AddProductNameRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return productService.addProductName(id, request, user);
    }

    @Operation(summary = "Approve a product name (Admin)", description = "Approves a user-submitted product name.")
    @PostMapping("/admin/products/names/{nameId}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void approveProductName(@PathVariable UUID nameId) {
        productService.approveProductName(nameId);
    }

    @Operation(summary = "Delete a product name", description = "Deletes an administrator name or the current user's pending submission.")
    @DeleteMapping("/products/{id}/names/{nameId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProductName(
            @PathVariable UUID id,
            @PathVariable UUID nameId,
            @AuthenticationPrincipal AuthUser user
    ) {
        productService.deleteProductName(id, nameId, user);
    }

    @Operation(summary = "Approve a whole product (Admin)", description = "Publishes a previously proposed product, making it visible to all users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product successfully approved"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., product is already published)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PostMapping("/admin/products/{id}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void approveProduct(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id) {
        productService.approveWholeProduct(user.id(), id);
    }

    @Operation(summary = "Delete a product (Admin)", description = "Soft-deletes a product from the database.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/admin/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable UUID id) {
        productService.softDeleteProduct(id);
    }

    @Operation(summary = "Add a portion to a product", description = "Adds a new portion size. Admins add directly, Users create private/proposed portions.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Portion successfully created (Admin only)"),
            @ApiResponse(responseCode = "202", description = "Private portion successfully added (User only)"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: Product does not exist")
    })
    @PostMapping("/products/{productId}/portions")
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

    @Operation(summary = "Update a portion", description = "Updates an existing portion. Admins apply changes directly. Users update private portions directly or propose changes to public ones.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Portion successfully updated (Admin only)"),
            @ApiResponse(responseCode = "202", description = "Portion successfully updated or change proposed (User only)"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: Portion or Product does not exist")
    })
    @PatchMapping("/products/portions/{portionId}")
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

    @Operation(summary = "Delete a portion", description = "Deletes a portion. Admins can delete any portion. Users can only delete their own private portions.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Portion successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: User attempted to delete a public portion or one they do not own")
    })
    @DeleteMapping("/products/portions/{portionId}")
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

    @Operation(summary = "Get pending product changes (Admin)", description = "Retrieves all product modification proposals submitted by users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pending changes"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required")
    })
    @GetMapping("/admin/products/changes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductChangeResponse> getPendingProductChanges() {
        return productService.getPendingProductChanges();
    }

    @Operation(summary = "Get pending portion changes (Admin)", description = "Retrieves all portion modification proposals submitted by users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pending portion changes"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required")
    })
    @GetMapping("/admin/products/portions/changes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PortionChangeResponse> getPendingPortionChanges() {
        return productService.getPendingPortionChanges();
    }

    @Operation(summary = "Approve product change (Admin)", description = "Applies a user's proposed changes to the actual product database.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Change successfully approved and applied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found: Change or associated product does not exist")
    })
    @PostMapping("/admin/products/changes/{changeId}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void approveProductChange(@PathVariable UUID changeId) {
        productService.approveProductChange(changeId);
    }

    @Operation(summary = "Approve portion change (Admin)", description = "Applies a user's proposed changes to the actual portion database.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Portion change successfully approved and applied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found: Change or associated portion does not exist")
    })
    @PostMapping("/admin/products/portions/changes/{changeId}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void approvePortionChange(@PathVariable UUID changeId) {
        productService.approvePortionChange(changeId);
    }

    @Operation(summary = "Search products", description = "Unified search that queries the local database and optionally fetches missing products from an external provider.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., query too short)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "429", description = "Too many requests (Rate limit exceeded)")
    })
    @RateLimit(maxRequests = 60, timeWindowSeconds = 60)
    @GetMapping("/products/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<?>> searchUnified(
            @Size(min = 3, max = 64) @RequestParam String q,
            @RequestParam(defaultValue = "false") boolean quick,
            @AuthenticationPrincipal AuthUser user,
            Locale locale,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductDto> result = productService.searchProductsUnified(q, quick, user, locale, pageable);
        if (user.roles().contains(UserRole.ADMIN)) {
            Page<ProductAdminResponse> adminResponses = result.map(productMapper::toAdminResponse);
            return ResponseEntity.ok(adminResponses);
        } else {
            UserProfile profile = userProfileRepository.findByUserId(user.id()).orElse(null);
            Page<ProductResponse> responses = result.map(product -> productMapper.toResponse(product, profile));
            return ResponseEntity.ok(responses);
        }
    }

    @Operation(summary = "Import external product", description = "Imports a product from an external provider using its barcode. Admins import directly, Users propose the import.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product successfully imported (or found locally)"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: Product does not exist in the external provider")
    })
    @PostMapping("/products/import")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> importProduct(
            @Valid @RequestBody ImportProductRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        ProductDto productDto = productService.importProduct(request.barcode(), user);
        if (user.roles().contains(UserRole.ADMIN))
            return ResponseEntity.ok(productMapper.toAdminResponse(productDto));
        else {
            UserProfile profile = userProfileRepository.findByUserId(user.id()).orElse(null);
            return ResponseEntity.ok(productMapper.toResponse(productDto, profile));
        }
    }
}