package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.srozga.gluqalc_api.entity.UserConsent;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, UUID> {
    List<UserConsent> findAllByUserId(UUID userId);
    @Query("SELECT uc.consentDefinition.id FROM UserConsent uc WHERE uc.user.id = :userId")
    Set<UUID> findAcceptedDefinitionIdsByUserId(@Param("userId") UUID userId);
}
