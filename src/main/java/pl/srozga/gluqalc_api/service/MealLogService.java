package pl.srozga.gluqalc_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.component.meal.MealLogMapper;
import pl.srozga.gluqalc_api.dto.internal.ProductDto;
import pl.srozga.gluqalc_api.dto.internal.ProductPortionDto;
import pl.srozga.gluqalc_api.dto.request.AddMealEntryRequest;
import pl.srozga.gluqalc_api.dto.response.MealCategoryResponse;
import pl.srozga.gluqalc_api.dto.response.MealEntryResponse;
import pl.srozga.gluqalc_api.entity.MealCategory;
import pl.srozga.gluqalc_api.entity.MealEntry;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.repository.MealCategoryRepository;
import pl.srozga.gluqalc_api.repository.MealEntryRepository;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MealLogService {
    private final MealEntryRepository mealEntryRepository;
    private final MealCategoryRepository mealCategoryRepository;
    private final ProductService productService;
    private final MealLogMapper mealLogMapper;

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Transactional(readOnly = true)
    public List<MealCategoryResponse> getDailyLog(AuthUser user, LocalDate date) {
        List<MealCategory> categories = mealCategoryRepository.findAllByUserIdOrderBySortOrderAsc(user.id());
        List<MealEntry> entries = mealEntryRepository.findAllByUserIdAndConsumedAt(user.id(), date);

        Map<UUID, List<MealEntry>> entriesByCategory = entries.stream()
                .collect(Collectors.groupingBy(e -> e.getMealCategory().getId()));

        return categories.stream()
                .map(category -> {
                    List<MealEntry> categoryEntries = entriesByCategory.getOrDefault(category.getId(), Collections.emptyList());
                    return mealLogMapper.toDto(category, categoryEntries);
                }).toList();
    }

    @Transactional
    public MealEntryResponse addMealEntry(AuthUser user, AddMealEntryRequest request) {
        MealCategory category = mealCategoryRepository.findByIdAndUserId(request.mealCategoryId(), user.id())
                .orElseThrow(() -> new NotFoundException("Meal category not found"));
        ProductDto product = productService.getProductSmart(request.productId(), user);

        BigDecimal finalWeightInGrams;
        String snapshotPortionName = "g";
        BigDecimal snapshotPortionUnitWeight;
        BigDecimal snapshotPortionQuantity = request.quantity();

        if (request.portionId() != null) {
            ProductPortionDto portion = product.portions().stream()
                    .filter(p -> p.id().equals(request.portionId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Product portion not found"));

            snapshotPortionName = portion.name();
            snapshotPortionUnitWeight = portion.weightInGrams();

            finalWeightInGrams = snapshotPortionUnitWeight.multiply(snapshotPortionQuantity);
        } else {
            finalWeightInGrams = request.quantity();
            snapshotPortionUnitWeight = BigDecimal.ONE;
        }

        MealEntry entry = MealEntry.builder()
                .userId(user.id())
                .mealCategory(category)
                .consumedAt(request.date())
                .productId(product.id())
                .productName(product.name())
                .brand(product.brand())
                .barcode(product.barcode())
                .glycemicIndex(product.nutrition().glycemicIndex())
                .portionId(request.portionId())
                .portionName(snapshotPortionName)
                .weightInGrams(finalWeightInGrams)
                .portionUnitWeight(snapshotPortionUnitWeight)
                .portionQuantity(snapshotPortionQuantity)
                .build();

        calculateAndSetMacros(entry, product, finalWeightInGrams);

        MealEntry savedEntry = mealEntryRepository.save(entry);
        log.info("Added meal entry {} for user {}", savedEntry.getId(), user.id());
        return mealLogMapper.toDto(savedEntry);
    }

    @Transactional
    public void deleteMealEntry(UUID entryId, AuthUser user) {
        MealEntry entry = mealEntryRepository.findByIdAndUserId(entryId, user.id())
                .orElseThrow(() -> new NotFoundException("Meal entry not found or access denied"));
        mealEntryRepository.delete(entry);
        log.info("Deleted meal entry {} for user {}", entryId, user.id());
    }

    private void calculateAndSetMacros(MealEntry entry, ProductDto product, BigDecimal weight) {
        BigDecimal ratio = weight.divide(HUNDRED, MathContext.DECIMAL64);

        if (product.nutrition() != null) {
            entry.setEnergyKcal(safeMultiply(product.nutrition().energyKcal(), ratio));
            entry.setCarbohydrates(safeMultiply(product.nutrition().carbohydrates(), ratio));
            entry.setSugars(safeMultiply(product.nutrition().sugars(), ratio));
            entry.setFat(safeMultiply(product.nutrition().fat(), ratio));
            entry.setSaturatedFat(safeMultiply(product.nutrition().saturatedFat(), ratio));
            entry.setProtein(safeMultiply(product.nutrition().protein(), ratio));
            entry.setFiber(safeMultiply(product.nutrition().fiber(), ratio));
            entry.setSalt(safeMultiply(product.nutrition().salt(), ratio));
        }
    }

    private BigDecimal safeMultiply(BigDecimal value, BigDecimal ratio) {
        if (value == null)
            return null;
        return value.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }
}
