package pl.srozga.gluqalc_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.common.UserRole;
import pl.srozga.gluqalc_api.common.ProductNameSource;
import pl.srozga.gluqalc_api.common.ProductNameType;
import pl.srozga.gluqalc_api.component.product.ProductMapper;
import pl.srozga.gluqalc_api.component.product.ProductMerger;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.request.AddProductRequest;
import pl.srozga.gluqalc_api.dto.request.AddProductNameRequest;
import pl.srozga.gluqalc_api.dto.request.UpdateProductRequest;
import pl.srozga.gluqalc_api.dto.response.*;
import pl.srozga.gluqalc_api.entity.*;
import pl.srozga.gluqalc_api.exception.ConflictException;
import pl.srozga.gluqalc_api.exception.DomainValidationException;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.integration.ProductProvider;
import pl.srozga.gluqalc_api.repository.*;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductNameRepository productNameRepository;
    private final ProductPortionRepository productPortionRepository;
    private final ProductChangeRepository productChangeRepository;
    private final PortionChangeRepository portionChangeRepository;
    private final ProductMerger productMerger;
    private final ProductMapper productMapper;
    private final ProductProvider productProvider;
    private final ObjectProvider<ProductService> selfProvider;

    @Transactional
    @CacheEvict(value = "local_search_cache", allEntries = true)
    public ProductDto createProduct(UUID adminId, AddProductRequest productRequest) {
        Product savedProduct = createProductInternal(productRequest, true, adminId);
        log.info("Created new published product: {} by admin {}", savedProduct.getId(), adminId);
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    @CacheEvict(value = "local_search_cache", allEntries = true)
    public ProductDto proposeProduct(UUID authorId, AddProductRequest productRequest) {
        Product savedProduct = createProductInternal(productRequest, false, authorId);
        log.info("Created new proposed product: {} by user {}", savedProduct.getId(), authorId);
        return productMapper.toDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductSmart(UUID productId, AuthUser user) {
        Product product;
        if (user.roles().contains(UserRole.ADMIN))
            product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        else
            product = productRepository.findByIdVisibleToUser(productId, user.id())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        return assembleSmartProduct(product, user.id(), user.roles().contains(UserRole.ADMIN));
    }

    @Transactional(readOnly = true)
    public ProductDto getProductSmartByBarcode(String barcode, AuthUser user) {
        Product product;
        if (user.roles().contains(UserRole.ADMIN))
            product = productRepository.findByBarcodeAndDeletedFalse(barcode)
                    .orElseThrow(() -> new NotFoundException("Product not found"));
        else
            product = productRepository.findByBarcodeVisibleToUser(barcode, user.id())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

        return assembleSmartProduct(product, user.id(), user.roles().contains(UserRole.ADMIN));
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getMyProducts(UUID userId) {
        return productRepository.findAllOwnedByUser(userId).stream()
                .map(product -> assembleSmartProduct(product, userId, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return productMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcodeAndDeletedFalse(barcode)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductNameResponse addProductName(UUID productId, AddProductNameRequest request, AuthUser user) {
        Product product = user.roles().contains(UserRole.ADMIN)
            ? productRepository.findByIdAndDeletedFalse(productId)
            .orElseThrow(() -> new NotFoundException("Product not found"))
            : productRepository.findByIdVisibleToUser(productId, user.id())
            .orElseThrow(() -> new NotFoundException("Product not found"));
        String normalizedName = request.name().trim();
        String normalizedLanguage = request.languageCode().trim().toLowerCase(Locale.ROOT);

        boolean duplicate = product.getNames().stream().anyMatch(name ->
                name.getName().equalsIgnoreCase(normalizedName)
                        && name.getLanguageCode().equalsIgnoreCase(normalizedLanguage));
        if (product.getName().equalsIgnoreCase(normalizedName))
            duplicate = true;
        if (duplicate)
            throw new ConflictException("This product name already exists");

        boolean approved = user.roles().contains(UserRole.ADMIN);
        ProductName productName = ProductName.builder()
                .product(product)
                .name(normalizedName)
                .languageCode(normalizedLanguage)
                .type(request.type() != null ? request.type() : ProductNameType.ALIAS)
                .source(approved ? ProductNameSource.ADMIN : ProductNameSource.USER)
                .approved(approved)
                .createdBy(user.id())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        product.getNames().add(productName);
        productRepository.save(product);
        return toProductNameResponse(productName);
    }

    @Transactional
    public void approveProductName(UUID nameId) {
        ProductName productName = productNameForId(nameId);
        productName.setApproved(true);
        productName.setUpdatedAt(Instant.now());
    }

    @Transactional
    public void deleteProductName(UUID productId, UUID nameId, AuthUser user) {
        Product product = user.roles().contains(UserRole.ADMIN)
                ? productRepository.findByIdAndDeletedFalse(productId).orElseThrow(() -> new NotFoundException("Product not found"))
                : productRepository.findByIdVisibleToUser(productId, user.id()).orElseThrow(() -> new NotFoundException("Product not found"));
        ProductName productName = product.getNames().stream()
                .filter(name -> name.getId().equals(nameId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Product name not found"));
        if (!user.roles().contains(UserRole.ADMIN)
                && (!productName.getCreatedBy().equals(user.id()) || productName.isApproved()))
            throw new ConflictException("Only your pending product names can be deleted");
        product.getNames().remove(productName);
        productRepository.save(product);
    }

    private ProductName productNameForId(UUID nameId) {
        return productNameRepository.findById(nameId)
                .orElseThrow(() -> new NotFoundException("Product name not found"));
    }

    private ProductNameResponse toProductNameResponse(ProductName name) {
        return new ProductNameResponse(name.getId(), name.getName(), name.getLanguageCode(), name.getType(), name.getSource(), name.isApproved());
    }

    @Transactional
    @CacheEvict(value = "local_search_cache", allEntries = true)
    public ProductDto updateProduct(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (request.barcode() != null && productRepository.existsByBarcodeAndIdNotAndDeletedFalse(request.barcode(), id))
            throw new ConflictException("Another product with this barcode already exists");

        BigDecimal effectiveCarbs = request.carbohydrates() != null ? request.carbohydrates() : product.getCarbohydrates();
        BigDecimal effectiveSugars = request.sugars() != null ? request.sugars() : product.getSugars();
        BigDecimal effectiveFat = request.fat() != null ? request.fat() : product.getFat();
        BigDecimal effectiveSatFat = request.saturatedFat() != null ? request.saturatedFat() : product.getSaturatedFat();

        validateMacroRelations(effectiveCarbs, effectiveSugars, effectiveFat, effectiveSatFat);

        updateIfPresent(request.name(), product::setName);
        updateIfPresent(request.brand(), product::setBrand);
        updateIfPresent(request.barcode(), product::setBarcode);
        updateIfPresent(request.energyKcal(), product::setEnergyKcal);
        updateIfPresent(request.carbohydrates(), product::setCarbohydrates);
        updateIfPresent(request.sugars(), product::setSugars);
        updateIfPresent(request.fat(), product::setFat);
        updateIfPresent(request.saturatedFat(), product::setSaturatedFat);
        updateIfPresent(request.protein(), product::setProtein);
        updateIfPresent(request.fiber(), product::setFiber);
        updateIfPresent(request.salt(), product::setSalt);
        updateIfPresent(request.glycemicIndex(), product::setGlycemicIndex);
        updateIfPresent(request.published(), product::setPublished);

        Product updatedProduct = productRepository.save(product);
        log.info("Updated product: {}", updatedProduct.getId());
        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    @CacheEvict(value = "local_search_cache", allEntries = true)
    public void proposeProductChange(UUID productId, UUID userId, UpdateProductRequest request) {
        Product product = productRepository.findByIdVisibleToUser(productId, userId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

         BigDecimal effectiveCarbs = request.carbohydrates() != null ? request.carbohydrates() : product.getCarbohydrates();
         BigDecimal effectiveSugars = request.sugars() != null ? request.sugars() : product.getSugars();
         BigDecimal effectiveFat = request.fat() != null ? request.fat() : product.getFat();
         BigDecimal effectiveSatFat = request.saturatedFat() != null ? request.saturatedFat() : product.getSaturatedFat();

        validateMacroRelations(effectiveCarbs, effectiveSugars, effectiveFat, effectiveSatFat);

        ProductChange change = productChangeRepository.findByProductIdAndUserIdAndDeletedFalse(productId, userId)
                .orElse(ProductChange.builder()
                        .productId(productId)
                        .userId(userId)
                        .deleted(false)
                        .createdAt(Instant.now())
                        .build());

        change.setUpdatedAt(Instant.now());

        updateIfPresent(request.name(), change::setName);
        updateIfPresent(request.brand(), change::setBrand);
        updateIfPresent(request.barcode(), change::setBarcode);
        updateIfPresent(request.energyKcal(), change::setEnergyKcal);
        updateIfPresent(request.carbohydrates(), change::setCarbohydrates);
        updateIfPresent(request.sugars(), change::setSugars);
        updateIfPresent(request.fat(), change::setFat);
        updateIfPresent(request.saturatedFat(), change::setSaturatedFat);
        updateIfPresent(request.protein(), change::setProtein);
        updateIfPresent(request.fiber(), change::setFiber);
        updateIfPresent(request.salt(), change::setSalt);
        updateIfPresent(request.glycemicIndex(), change::setGlycemicIndex);

        log.info("User {} proposed change for product {}. Change: {}", userId, productId, change.getId());
        productChangeRepository.save(change);
    }

    @Transactional
    public void approveWholeProduct(UUID adminId, UUID id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (product.isPublished())
            throw new DomainValidationException("Product is already approved");

        product.setPublished(true);
        product.getPortions().forEach(p -> p.setPublished(true));
        Product approvedProduct = productRepository.save(product);
        log.info("Product {} approved by admin {}", approvedProduct.getId(), adminId);
    }

    @Transactional
    @CacheEvict(value = "local_search_cache", allEntries = true)
    public void softDeleteProduct(UUID id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        product.setDeleted(true);
        productRepository.save(product);
        log.info("Soft deleted product: {}", id);
    }

    @Transactional
    public ProductDto addPortion(UUID productId, String name, BigDecimal weight, AuthUser user) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        addPortionToProduct(product, name, weight, product.isPublished(), user.id());
        Product savedProduct = productRepository.save(product);
        log.info("Product {} added by admin {}", savedProduct.getId(), user.id());
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public void addPortionUser(UUID productId, String name, BigDecimal weight, AuthUser user) {
        Product product = productRepository.findByIdVisibleToUser(productId, user.id())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        addPortionToProduct(product, name, weight, false, user.id());

        productRepository.save(product);
        log.info("Private portion added to product {} by user {}", productId, user.id());
    }

    @Transactional
    public ProductDto updatePortion(UUID portionId, String name, BigDecimal weight) {
        ProductPortion portion = productPortionRepository.findById(portionId)
                .orElseThrow(() -> new NotFoundException("Portion not found"));
        if (portion.getProduct().isDeleted())
            throw new NotFoundException("Product for this portion not found");

        if (name != null)
            portion.setName(name);
        if (weight != null)
            portion.setWeightInGrams(weight);

        productPortionRepository.save(portion);
        log.info("Updated portion: {}", portionId);

        return productMapper.toDto(portion.getProduct());
    }

    @Transactional
    public void updatePortionUser(UUID portionId, String name, BigDecimal weight, AuthUser user) {
        ProductPortion portion = productPortionRepository.findById(portionId)
                .orElseThrow(() -> new NotFoundException("Portion not found"));

        boolean canEditDirectly = portion.getCreatedBy().equals(user.id()) && !portion.isPublished();

        if (canEditDirectly) {
            updateIfPresent(name, portion::setName);
            updateIfPresent(weight, portion::setWeightInGrams);
            productPortionRepository.save(portion);
            log.info("User {} updated their private portion {}", user.id(), portionId);
        } else {
            PortionChange change = portionChangeRepository.findByPortionIdAndUserIdAndDeletedFalse(portionId, user.id())
                    .orElse(PortionChange.builder()
                            .portionId(portionId)
                            .userId(user.id())
                            .deleted(false)
                            .createdAt(Instant.now())
                            .build());

            change.setUpdatedAt(Instant.now());

            updateIfPresent(name, change::setName);
            updateIfPresent(weight, change::setWeightInGrams);

            portionChangeRepository.save(change);
            log.info("User {} proposed change for verified/public portion {}", user.id(), portionId);
        }
    }

    @Transactional
    public void deletePortion(UUID portionId) {
        if (!productPortionRepository.existsById(portionId))
            throw new NotFoundException("Portion not found");

        productPortionRepository.deleteById(portionId);
        log.info("Deleted portion: {}", portionId);
    }

    @Transactional
    public void deletePortionUser(UUID portionId, AuthUser user) {
        ProductPortion portion = productPortionRepository.findById(portionId)
                .orElseThrow(() -> new NotFoundException("Portion not found"));

        boolean isOwner = portion.getCreatedBy().equals(user.id());
        boolean isPrivate = !portion.isPublished();

        if (!isOwner)
            throw new ConflictException("You can only delete portions created by you.");
        if (!isPrivate)
            throw new ConflictException("This portion is already verified and public. You cannot delete it.");

        productPortionRepository.delete(portion);
        log.info("User {} deleted their private portion {}", user.id(), portionId);
    }

    @Transactional(readOnly = true)
    public List<ProductChangeResponse> getPendingProductChanges() {
        return productChangeRepository.findAllByDeletedFalse().stream().map(c -> new ProductChangeResponse(
                c.getId(),
                c.getProductId(),
                c.getUserId(),
                c.getName(),
                c.getBrand(),
                c.getBarcode(),
                c.getEnergyKcal(),
                c.getCarbohydrates(),
                c.getSugars(),
                c.getFat(),
                c.getSaturatedFat(),
                c.getProtein(),
                c.getFiber(),
                c.getSalt(),
                c.getGlycemicIndex()
        )).toList();
    }

    @Transactional(readOnly = true)
    public List<PortionChangeResponse> getPendingPortionChanges() {
        return portionChangeRepository.findAllByDeletedFalse().stream().map(c -> new PortionChangeResponse(
                c.getId(),
                c.getPortionId(),
                c.getUserId(),
                c.getName(),
                c.getWeightInGrams()
        )).toList();
    }

    @Transactional
    public void approveProductChange(UUID changeId) {
        ProductChange change = productChangeRepository.findById(changeId)
                .orElseThrow(() -> new NotFoundException("Change not found"));
        Product product = productRepository.findByIdAndDeletedFalse(change.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        updateIfPresent(change.getName(), product::setName);
        updateIfPresent(change.getBrand(), product::setBrand);
        updateIfPresent(change.getBarcode(), product::setBarcode);
        updateIfPresent(change.getEnergyKcal(), product::setEnergyKcal);
        updateIfPresent(change.getCarbohydrates(), product::setCarbohydrates);
        updateIfPresent(change.getSugars(), product::setSugars);
        updateIfPresent(change.getFat(), product::setFat);
        updateIfPresent(change.getSaturatedFat(), product::setSaturatedFat);
        updateIfPresent(change.getProtein(), product::setProtein);
        updateIfPresent(change.getFiber(), product::setFiber);
        updateIfPresent(change.getSalt(), product::setSalt);
        updateIfPresent(change.getGlycemicIndex(), product::setGlycemicIndex);

        change.setDeleted(true);
        productRepository.save(product);
        productChangeRepository.save(change);
        log.info("Product change {} approved and applied to product {}", changeId, product.getId());
    }

    @Transactional
    public void approvePortionChange(UUID changeId) {
        PortionChange change = portionChangeRepository.findById(changeId)
                .orElseThrow(() -> new NotFoundException("Change not found"));
        ProductPortion portion = productPortionRepository.findById(change.getPortionId())
                .orElseThrow(() -> new NotFoundException("Portion not found"));

        updateIfPresent(change.getName(), portion::setName);
        updateIfPresent(change.getWeightInGrams(), portion::setWeightInGrams);

        change.setDeleted(true);
        productPortionRepository.save(portion);
        portionChangeRepository.save(change);
        log.info("Portion change {} approved and applied to portion {}", changeId, portion.getId());
    }

    public Page<ProductDto> searchProductsUnified(String query, boolean quick, AuthUser user, Locale locale, Pageable pageable) {
        ProductService self = selfProvider.getObject();

        CompletableFuture<List<ProductDto>> localFuture = CompletableFuture.supplyAsync(
                () -> self.findLocalProductsDto(query, user.id())
        );

        CompletableFuture<List<ProductDto>> offFuture = CompletableFuture.supplyAsync(() -> {
            if (quick)
                return Collections.emptyList();

            int requiredFromOff = Math.max(50, (int) pageable.getOffset() + pageable.getPageSize());
            try {
                return productProvider.searchProducts(query, locale, requiredFromOff);
            } catch (Exception e) {
                log.warn("Failed to search products from external provider", e);
                return Collections.emptyList();
            }
        });

        List<ProductDto> results = new ArrayList<>(localFuture.join());
        List<ProductDto> offResults = offFuture.join();

        if (!offResults.isEmpty()) {
            List<String> existingBarcodes = results.stream()
                    .map(ProductDto::barcode)
                    .filter(Objects::nonNull)
                    .toList();

            offResults.stream()
                    .filter(off -> off.barcode() != null && !existingBarcodes.contains(off.barcode()))
                    .forEach(results::add);
        }

        int start = (int) pageable.getOffset();
        if (start >= results.size())
            return new PageImpl<>(Collections.emptyList(), pageable, results.size());

        int end = Math.min(start + pageable.getPageSize(), results.size());
        List<ProductDto> pageContent = results.subList(start, end);

        return new PageImpl<>(pageContent, pageable, results.size());
    }

    @Transactional
    @CacheEvict(value = "local_search_cache", allEntries = true)
    public ProductDto importProduct(String barcode, AuthUser user) {
        ProductService self = selfProvider.getObject();

        Optional<Product> existingAny = productRepository.findByBarcode(barcode);
        if (existingAny.isPresent()) {
            Product product = existingAny.get();
            boolean changed = false;

            if (product.isDeleted()) {
                product.setDeleted(false);
                changed = true;
            }

            if (!product.isPublished()) {
                product.setPublished(true);
                changed = true;
            }

            if (changed) {
                productRepository.save(product);
            }
            return self.getProductSmart(product.getId(), user);
        }

        ProductDto offDto = productProvider.getProductByBarcode(barcode)
                .orElseThrow(() -> new NotFoundException("Product not found in external provider"));

        AddProductRequest request = new AddProductRequest(
                offDto.name(),
                offDto.brand(),
                offDto.barcode(),
                offDto.nutrition().energyKcal(),
                offDto.nutrition().carbohydrates(),
                offDto.nutrition().sugars(),
                offDto.nutrition().fat(),
                offDto.nutrition().saturatedFat(),
                offDto.nutrition().protein(),
                offDto.nutrition().fiber(),
                offDto.nutrition().salt(),
                offDto.nutrition().glycemicIndex(),
                null
        );

        Product savedProduct = createProductInternal(request, true, user.id());

        log.info("User {} imported and published product with barcode {} from external provider (OFF)", user.id(), barcode);

        return productMapper.toDto(savedProduct);
    }

    @Cacheable(value = "local_search_cache", key = "'v2_' + #query.trim().toLowerCase() + '_' + #userId")
    @Transactional(readOnly = true)
    public List<ProductDto> findLocalProductsDto(String query, UUID userId) {
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        return productRepository.searchProducts(normalizedQuery, userId, maxSearchDistance(normalizedQuery))
                .stream()
                .map(product -> assembleSmartProduct(product, userId, false))
                .collect(Collectors.toList());
    }

    private int maxSearchDistance(String query) {
        if (query.length() <= 4)
            return 1;
        if (query.length() <= 8)
            return 3;
        return 3;
    }

    private Product createProductInternal(AddProductRequest productRequest, boolean isPublished, UUID creatorId) {
        if (productRequest.barcode() != null && productRepository.existsByBarcodeAndDeletedFalse(productRequest.barcode()))
            throw new ConflictException("Product with this barcode already exists");

        validateMacroRelations(
                productRequest.carbohydrates(), productRequest.sugars(),
                productRequest.fat(), productRequest.saturatedFat()
        );

        Product product = Product.builder()
                .name(productRequest.name())
                .brand(productRequest.brand())
                .barcode(productRequest.barcode())
                .energyKcal(productRequest.energyKcal())
                .carbohydrates(productRequest.carbohydrates())
                .fat(productRequest.fat())
                .protein(productRequest.protein())
                .sugars(productRequest.sugars())
                .saturatedFat(productRequest.saturatedFat())
                .fiber(productRequest.fiber())
                .salt(productRequest.salt())
                .glycemicIndex(productRequest.glycemicIndex())
                .published(isPublished)
                .deleted(false)
                .createdBy(creatorId)
                .build();

        addPortionToProduct(product, "100g", new BigDecimal("100.0"), isPublished, creatorId);
        if (productRequest.portions() != null && !productRequest.portions().isEmpty())
            productRequest.portions().forEach(p -> addPortionToProduct(product, p.name(), p.weightInGrams(), isPublished, creatorId));

        Product savedProduct = productRepository.save(product);
        log.info("Created new product: {}", savedProduct.getName());
        return savedProduct;

    }

    private void addPortionToProduct(Product product, String name, BigDecimal weight, boolean isPublished, UUID creatorId) {
        ProductPortion portion = ProductPortion.builder()
                .name(name)
                .weightInGrams(weight)
                .published(isPublished)
                .createdBy(creatorId)
                .build();
        product.getPortions().add(portion);
        portion.setProduct(product);
    }

    private void validateMacroRelations(BigDecimal carbs, BigDecimal sugars, BigDecimal fat, BigDecimal satFat) {
        if (sugars != null && carbs != null && sugars.compareTo(carbs) > 0)
            throw new ConflictException("Sugars cannot be greater than carbohydrates");
        if (satFat != null && fat != null && satFat.compareTo(fat) > 0)
            throw new ConflictException("Saturated fat cannot be greater than total fat");
    }

    private <T> void updateIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private ProductDto assembleSmartProduct(Product product, UUID userId, boolean includePendingNames) {
        ProductChange change = productChangeRepository.findByProductIdAndUserIdAndDeletedFalse(product.getId(), userId)
                .orElse(null);
        List<ProductPortion> allVisiblePortions = productPortionRepository.findAllVisibleForUser(product.getId(), userId);
        List<PortionChange> portionChanges = portionChangeRepository.findAllActiveByUserIdAndProduct(userId, product.getId());
        return productMerger.merge(product, change, allVisiblePortions, portionChanges, includePendingNames);
    }
}
