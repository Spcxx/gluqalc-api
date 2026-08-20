package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.srozga.gluqalc_api.entity.ProductName;

import java.util.UUID;

@Repository
public interface ProductNameRepository extends JpaRepository<ProductName, UUID> {
}
