package pl.srozga.gluqalc_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.srozga.gluqalc_api.entity.DeviceSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, UUID> {
    Optional<DeviceSession> findByRefreshToken(String refreshToken);
    Optional<DeviceSession> findByUserIdAndDeviceId(UUID userId, String deviceId);
    List<DeviceSession> findAllByUserId(UUID userId);
    long countByUserId(UUID userId);
    Optional<DeviceSession> findFirstByUserIdOrderByLastAccessedAtAsc(UUID userId);
    void deleteByUserIdAndDeviceId(UUID userId, String deviceId);
    void deleteAllByUserId(UUID userId);
}
