package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.srozga.gluqalc_api.entity.ProductPortion;

import java.util.List;
import java.util.UUID;

public interface ProductPortionRepository extends JpaRepository<ProductPortion, UUID> {
    @Query("""
        SELECT p FROM ProductPortion p
        WHERE p.product.id = :productId
        AND (p.published = true OR p.createdBy = :userId)
    """)
    List<ProductPortion> findAllVisibleForUser(UUID productId, UUID userId);
}
