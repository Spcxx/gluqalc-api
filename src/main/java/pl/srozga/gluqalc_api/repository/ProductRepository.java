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
    @Query(value = """
        SELECT p.*
        FROM products p
        WHERE (
            LOWER(p.name) LIKE CONCAT('%', LOWER(:query), '%')
            OR LOWER(COALESCE(p.brand, '')) LIKE CONCAT('%', LOWER(:query), '%')
            OR EXISTS (
                SELECT 1
                FROM product_names matching_name
                WHERE matching_name.product_id = p.id
                  AND matching_name.approved = true
                  AND LOWER(matching_name.name) LIKE CONCAT('%', LOWER(:query), '%')
            )
            OR p.barcode = :query
            OR EXISTS (
                SELECT 1
                FROM regexp_split_to_table(
                    regexp_replace(LOWER(p.name), '[^[:alnum:]]+', ' ', 'g'),
                    '\\s+'
                ) AS name_token
                WHERE levenshtein_less_equal(name_token, LOWER(:query), :maxDistance) <= :maxDistance
            )
            OR EXISTS (
                SELECT 1
                FROM regexp_split_to_table(
                    regexp_replace(LOWER(COALESCE(p.brand, '')), '[^[:alnum:]]+', ' ', 'g'),
                    '\\s+'
                ) AS brand_token
                WHERE levenshtein_less_equal(brand_token, LOWER(:query), :maxDistance) <= :maxDistance
            )
            OR EXISTS (
                SELECT 1
                FROM product_names alias_name
                WHERE alias_name.product_id = p.id
                  AND alias_name.approved = true
                  AND levenshtein_less_equal(LOWER(alias_name.name), LOWER(:query), :maxDistance) <= :maxDistance
            )
        )
        AND p.deleted = false
        AND (p.published = true OR p.created_by = :userId)
        ORDER BY
            CASE WHEN p.barcode = :query THEN 0 ELSE 1 END,
            CASE WHEN LOWER(p.name) = LOWER(:query) THEN 0 ELSE 1 END,
            LENGTH(p.name) ASC,
            p.name ASC
    """, nativeQuery = true)
    List<Product> searchProducts(
            @Param("query") String query,
            @Param("userId") UUID userId,
            @Param("maxDistance") int maxDistance
    );
}
