package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.srozga.gluqalc_api.entity.ProductPortion;

import java.util.UUID;

public interface ProductPortionRepository extends JpaRepository<ProductPortion, UUID> {
}
