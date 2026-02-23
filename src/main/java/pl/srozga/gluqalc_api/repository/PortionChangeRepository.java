package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.srozga.gluqalc_api.entity.PortionChange;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortionChangeRepository extends JpaRepository<PortionChange, UUID> {
    Optional<PortionChange> findByPortionIdAndUserIdAndDeletedFalse(UUID portionId, UUID userId);
    List<PortionChange> findAllByDeletedFalse();

    @Query("""
        SELECT pc FROM PortionChange pc
        JOIN ProductPortion pp ON pc.portionId = pp.id
        WHERE pc.userId = :userId
          AND pp.product.id = :productId
          AND pc.deleted = false
    """)
    List<PortionChange> findAllActiveByUserIdAndProduct(UUID userId, UUID productId);
}
