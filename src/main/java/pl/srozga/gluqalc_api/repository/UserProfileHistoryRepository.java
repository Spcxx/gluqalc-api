package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.srozga.gluqalc_api.entity.UserProfileHistory;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserProfileHistoryRepository extends JpaRepository<UserProfileHistory, UUID> {
    List<UserProfileHistory> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}