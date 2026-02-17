package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.srozga.gluqalc_api.entity.MealEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MealEntryRepository extends JpaRepository<MealEntry, UUID> {
    List<MealEntry> findAllByUserIdAndConsumedAt(UUID userId, LocalDate date);
    Optional<MealEntry> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByMealCategoryId(UUID mealCategoryId);
}
