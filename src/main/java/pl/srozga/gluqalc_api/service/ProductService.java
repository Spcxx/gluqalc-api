package pl.srozga.gluqalc_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.dto.request.AddProductRequest;
import pl.srozga.gluqalc_api.dto.request.UpdateProductRequest;
import pl.srozga.gluqalc_api.dto.response.ProductAdminResponse;
import pl.srozga.gluqalc_api.dto.response.ProductNutritionResponse;
import pl.srozga.gluqalc_api.dto.response.ProductPortionAdminResponse;
import pl.srozga.gluqalc_api.entity.Product;
import pl.srozga.gluqalc_api.entity.ProductPortion;
import pl.srozga.gluqalc_api.exception.ConflictException;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.repository.ProductPortionRepository;
import pl.srozga.gluqalc_api.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductPortionRepository productPortionRepository;

    @Transactional
    public ProductAdminResponse createProduct(AddProductRequest productRequest) {
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
                .published(true)
                .deleted(false)
                .build();

        addPortionToProduct(product, "100g", new BigDecimal("100.0"));
        if (productRequest.portions() != null && !productRequest.portions().isEmpty())
            productRequest.portions().forEach(p -> addPortionToProduct(product, p.name(), p.weightInGrams()));

        Product savedProduct = productRepository.save(product);
        log.info("Created new product: {}", savedProduct.getName());
        return mapToResponse(savedProduct);

    }

    @Transactional(readOnly = true)
    public ProductAdminResponse getProductById(UUID id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductAdminResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcodeAndDeletedFalse(barcode)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return mapToResponse(product);
    }


    @Transactional
    public ProductAdminResponse updateProduct(UUID id, UpdateProductRequest request) {
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
        return mapToResponse(updatedProduct);
    }

    @Transactional
    public void softDeleteProduct(UUID id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        product.setDeleted(true);
        productRepository.save(product);
        log.info("Soft deleted product: {}", id);
    }

    @Transactional
    public ProductAdminResponse addPortion(UUID productId, String name, BigDecimal weight) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        addPortionToProduct(product, name, weight);
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Transactional
    public ProductAdminResponse updatePortion(UUID portionId, String name, BigDecimal weight) {
        ProductPortion portion = productPortionRepository.findById(portionId)
                .orElseThrow(() -> new NotFoundException("Portion not found"));
        if (portion.getProduct().isDeleted())
            throw new NotFoundException("Product for this portion not found");

        if (name != null)
            portion.setName(name);
        if (weight != null)
            portion.setWeightInGrams(weight);

        productPortionRepository.save(portion);

        return mapToResponse(portion.getProduct());
    }

    @Transactional
    public void deletePortion(UUID portionId) {
        if (!productPortionRepository.existsById(portionId))
            throw new NotFoundException("Portion not found");

        productPortionRepository.deleteById(portionId);
        log.info("Deleted portion: {}", portionId);
    }

    private void addPortionToProduct(Product product, String name, BigDecimal weight) {
        ProductPortion portion = ProductPortion.builder()
                .name(name)
                .weightInGrams(weight)
                .published(true)
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

    private ProductAdminResponse mapToResponse(Product product) {
        List<ProductPortionAdminResponse> portionResponses = product.getPortions().stream()
                .sorted(Comparator.comparing(ProductPortion::getWeightInGrams))
                .map(p -> new ProductPortionAdminResponse(
                        p.getId(),
                        p.getName(),
                        p.getWeightInGrams()
                )).toList();

        ProductNutritionResponse nutrition = new ProductNutritionResponse(
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

        return new ProductAdminResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getBarcode(),
                nutrition,
                portionResponses,
                product.isPublished(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private <T> void updateIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
