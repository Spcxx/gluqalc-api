package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.srozga.gluqalc_api.entity.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByIdAndDeletedFalse(UUID id);
    Optional<Product> findByBarcodeAndDeletedFalse(String barcode);
    boolean existsByBarcodeAndDeletedFalse(String barcode);
    boolean existsByBarcodeAndIdNotAndDeletedFalse(String barcode, UUID id);
    @Query("""
        SELECT p FROM Product p
        WHERE p.id = :productId
        AND (p.published = true OR p.createdBy = :userId)
    """)
    Optional<Product> findByIdVisibleToUser(UUID productId, UUID userId);
    @Query("""
        SELECT p FROM Product p
        WHERE p.barcode = :barcode
        AND (p.published = true OR p.createdBy = :userId)
    """)
    Optional<Product> findByBarcodeVisibleToUser(String barcode, UUID userId);
}
