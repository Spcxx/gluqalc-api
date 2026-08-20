package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.srozga.gluqalc_api.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByIdAndDeletedFalse(UUID id);
    Optional<Product> findByBarcodeAndDeletedFalse(String barcode);
    boolean existsByBarcodeAndDeletedFalse(String barcode);
    boolean existsByBarcodeAndIdNotAndDeletedFalse(String barcode, UUID id);
    @Query("""
        SELECT p FROM Product p
        WHERE p.id = :productId
        AND p.deleted = false
        AND (p.published = true OR p.createdBy = :userId)
    """)
    Optional<Product> findByIdVisibleToUser(UUID productId, UUID userId);
    @Query("""
        SELECT p FROM Product p
        WHERE p.barcode = :barcode
        AND p.deleted = false
        AND (p.published = true OR p.createdBy = :userId)
    """)
    Optional<Product> findByBarcodeVisibleToUser(String barcode, UUID userId);
    @Query("""
        SELECT p FROM Product p
        WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :#{escape(#query)}, '%')) ESCAPE '\\'
           OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :#{escape(#query)}, '%')) ESCAPE '\\'
              OR EXISTS (SELECT n.id FROM ProductName n
                             WHERE n.product = p
                             AND n.approved = true
                             AND LOWER(n.name) LIKE LOWER(CONCAT('%', :#{escape(#query)}, '%')) ESCAPE '\\')
           OR p.barcode = :query)
        AND p.deleted = false
        AND (p.published = true OR p.createdBy = :userId)
        ORDER BY
           CASE WHEN p.barcode = :query THEN 0 ELSE 1 END,
           LENGTH(p.name) ASC,
           p.name ASC
    """)
    List<Product> searchProducts(@Param("query") String query, @Param("userId") UUID userId);
}
