package pl.srozga.gluqalc_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.component.meal.MealLogMapper;
import pl.srozga.gluqalc_api.dto.request.AddMealCategoryRequest;
import pl.srozga.gluqalc_api.dto.response.MealCategoryResponse;
import pl.srozga.gluqalc_api.entity.MealCategory;
import pl.srozga.gluqalc_api.entity.UserProfile;
import pl.srozga.gluqalc_api.exception.ConflictException;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.repository.MealCategoryRepository;
import pl.srozga.gluqalc_api.repository.MealEntryRepository;
import pl.srozga.gluqalc_api.repository.UserProfileRepository;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MealCategoryService {
    private final MealCategoryRepository mealCategoryRepository;
    private final MealEntryRepository mealEntryRepository;
    private final MealLogMapper mealLogMapper;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public List<MealCategoryResponse> getAllCategories(AuthUser user, LocalTime time) {
        UserProfile profile = userProfileRepository.findByUserId(user.id()).orElse(null);
        return mealCategoryRepository.findAllByUserIdOrderBySortOrderAsc(user.id())
                .stream()
                .map(category -> mealLogMapper.toCategoryDto(category, Collections.emptyList(), profile, time))
                .toList();
    }

    @Transactional(readOnly = true)
    public MealCategoryResponse getCategory(UUID categoryId, AuthUser user, LocalTime time) {
        MealCategory category = mealCategoryRepository.findByIdAndUserId(categoryId, user.id())
                .orElseThrow(() -> new NotFoundException("Meal category not found"));
        UserProfile profile = userProfileRepository.findByUserId(user.id()).orElse(null);

        return mealLogMapper.toCategoryDto(category, Collections.emptyList(), profile, time);
    }

    @Transactional
    public MealCategoryResponse createCategory(AuthUser user, AddMealCategoryRequest request, LocalTime time) {
        UserProfile profile = userProfileRepository.findByUserId(user.id()).orElse(null);
        Integer maxOrder = mealCategoryRepository.findMaxSortOrder(user.id());
        int nextOrder = (maxOrder == null) ? 0 : maxOrder + 1;

        MealCategory category = MealCategory.builder()
                .userId(user.id())
                .name(request.name())
                .sortOrder(nextOrder)
                .build();

        MealCategory savedCategory = mealCategoryRepository.save(category);
        log.info("Created meal category with id {} for user {}", savedCategory.getId(), user.id());

        return mealLogMapper.toCategoryDto(savedCategory, Collections.emptyList(), profile, time);
    }

    @Transactional
    public void deleteCategory(UUID categoryId, AuthUser user, boolean force) {
        MealCategory category = mealCategoryRepository.findByIdAndUserId(categoryId, user.id())
                .orElseThrow(() -> new NotFoundException("Meal category not found"));

        boolean hasEntries = mealEntryRepository.existsByMealCategoryId(categoryId);
        if (hasEntries) {
            if (!force) {
                throw new ConflictException("Cannot delete category with existing meal entries");
            } else {
                log.info("Force deleting all meal entries for category {} (user {})", categoryId, user.id());
                mealEntryRepository.deleteAllByMealCategoryId(categoryId);
            }
        }

        mealCategoryRepository.delete(category);
        log.info("Deleted meal category with id {} for user {}", categoryId, user.id());
    }
}
