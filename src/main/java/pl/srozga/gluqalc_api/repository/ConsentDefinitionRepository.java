package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.srozga.gluqalc_api.entity.ConsentDefinition;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsentDefinitionRepository extends JpaRepository<ConsentDefinition, UUID> {
    List<ConsentDefinition> findAllByActiveTrue();
    List<ConsentDefinition> findAllByActiveTrueAndRequiredTrue();
    List<ConsentDefinition> findAllByIdInAndActiveTrue(List<UUID> ids);
}
