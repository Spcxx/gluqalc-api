package pl.srozga.gluqalc_api.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.srozga.gluqalc_api.entity.ProductPortion;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductPortionRepository extends JpaRepository<ProductPortion, UUID> {
    @Query("""
        SELECT p FROM ProductPortion p
        WHERE p.product.id = :productId
        AND (p.published = true OR p.createdBy = :userId)
    """)
    List<ProductPortion> findAllVisibleForUser(UUID productId, UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductPortion pp WHERE pp.createdBy = :userId AND pp.published = false")
    void deleteUnpublishedByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE ProductPortion pp SET pp.createdBy = NULL WHERE pp.createdBy = :userId AND pp.published = true")
    void anonymizePublishedByUserId(@Param("userId") UUID userId);
}
