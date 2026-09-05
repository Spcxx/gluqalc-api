package pl.srozga.gluqalc_api.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.srozga.gluqalc_api.entity.ProductName;

import java.util.UUID;

@Repository
public interface ProductNameRepository extends JpaRepository<ProductName, UUID> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductName pn WHERE pn.createdBy = :userId AND pn.approved = false")
    void deleteUnapprovedByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE ProductName pn SET pn.createdBy = NULL WHERE pn.createdBy = :userId AND pn.approved = true")
    void anonymizeApprovedByUserId(@Param("userId") UUID userId);
}
