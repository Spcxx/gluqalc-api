package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.srozga.gluqalc_api.entity.MealCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MealCategoryRepository extends JpaRepository<MealCategory, UUID> {
    Optional<MealCategory> findByIdAndUserId(UUID id, UUID userId);
    List<MealCategory> findAllByUserIdOrderBySortOrderAsc(UUID userId);
    @Query("SELECT MAX(c.sortOrder) FROM MealCategory c WHERE c.userId = :userId")
    Integer findMaxSortOrder(@Param("userId") UUID userId);
}
