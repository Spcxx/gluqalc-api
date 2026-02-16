package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.srozga.gluqalc_api.entity.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByIdAndDeletedFalse(UUID id);
    Optional<Product> findByBarcodeAndDeletedFalse(String barcode);
    boolean existsByBarcodeAndDeletedFalse(String barcode);
    boolean existsByBarcodeAndIdNotAndDeletedFalse(String barcode, UUID id);
}
