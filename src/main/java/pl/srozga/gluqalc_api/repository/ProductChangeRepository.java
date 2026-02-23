package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.srozga.gluqalc_api.entity.ProductChange;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductChangeRepository extends JpaRepository<ProductChange, UUID> {
    Optional<ProductChange> findByProductIdAndUserIdAndDeletedFalse(UUID productId, UUID userId);
    List<ProductChange> findAllByDeletedFalse();
}
