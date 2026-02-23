package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import pl.srozga.gluqalc_api.entity.DeviceSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceSessionRepository extends JpaRepository<DeviceSession, UUID> {
    Optional<DeviceSession> findByRefreshToken(String refreshToken);
    Optional<DeviceSession> findByUserIdAndDeviceId(UUID userId, String deviceId);
    List<DeviceSession> findAllByUserId(UUID userId);
    long countByUserId(UUID userId);
    Optional<DeviceSession> findFirstByUserIdOrderByLastAccessedAtAsc(UUID userId);
    @Modifying
    void deleteByUserIdAndDeviceId(UUID userId, String deviceId);
    @Modifying
    void deleteAllByUserId(UUID userId);
}
