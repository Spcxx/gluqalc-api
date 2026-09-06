package pl.srozga.gluqalc_api.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
        SELECT DISTINCT p FROM Product p
        WHERE p.deleted = false
        AND (
            p.createdBy = :userId
            OR EXISTS (
                SELECT 1 FROM ProductChange pc
                WHERE pc.productId = p.id
                AND pc.userId = :userId
                AND pc.deleted = false
            )
        )
    """)
    List<Product> findAllOwnedByUser(UUID userId);
    @Query(value = """
        SELECT p.*
        FROM products p
        WHERE p.deleted = false
          AND (p.published = true OR p.created_by = :userId)
          AND (
              -- 1. NAJSZYBSZE: Dokładne dopasowanie i zwykłe LIKE
              p.barcode = :query
              OR p.name ILIKE CONCAT('%', :query, '%')
              OR p.brand ILIKE CONCAT('%', :query, '%')
        
              -- 2. SZYBKIE: LIKE w aliasach
              OR EXISTS (
                  SELECT 1
                  FROM product_names alias_name
                  WHERE alias_name.product_id = p.id
                    AND alias_name.approved = true
                    AND alias_name.name ILIKE CONCAT('%', :query, '%')
              )
        
              -- 3. CIĘŻKIE (ale zoptymalizowane): Levenshtein dla nazwy i marki razem
              OR EXISTS (
                  SELECT 1
                  FROM regexp_split_to_table(
                      regexp_replace(LOWER(CONCAT_WS(' ', p.name, p.brand)), '[^[:alnum:]]+', ' ', 'g'),
                      '\\s+'
                  ) AS token
                  WHERE NULLIF(token, '') IS NOT NULL -- eliminuje fałszywe dopasowania pustych znaków
                    AND levenshtein_less_equal(token, LOWER(:query), :maxDistance) <= :maxDistance
              )
        
              -- 4. CIĘŻKIE: Levenshtein w aliasach
              OR EXISTS (
                  SELECT 1
                  FROM product_names alias_name
                  WHERE alias_name.product_id = p.id
                    AND alias_name.approved = true
                    AND levenshtein_less_equal(LOWER(alias_name.name), LOWER(:query), :maxDistance) <= :maxDistance
              )
          )
        ORDER BY
            CASE WHEN p.barcode = :query THEN 0 ELSE 1 END,
            CASE WHEN p.name ILIKE :query THEN 0 ELSE 1 END,
            LENGTH(p.name) ASC,
            p.name ASC;
    """, nativeQuery = true)
    List<Product> searchProducts(
            @Param("query") String query,
            @Param("userId") UUID userId,
            @Param("maxDistance") int maxDistance
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM Product p WHERE p.createdBy = :userId AND p.published = false")
    void deleteUnpublishedByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.createdBy = NULL WHERE p.createdBy = :userId AND p.published = true")
    void anonymizePublishedByUserId(@Param("userId") UUID userId);

    Optional<Product> findByBarcode(String barcode);
}
