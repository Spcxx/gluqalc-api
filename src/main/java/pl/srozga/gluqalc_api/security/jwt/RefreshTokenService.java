package pl.srozga.gluqalc_api.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.entity.DeviceSession;
import pl.srozga.gluqalc_api.entity.User;
import pl.srozga.gluqalc_api.exception.TokenAuthenticationException;
import pl.srozga.gluqalc_api.repository.DeviceSessionRepository;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final DeviceSessionRepository deviceSessionRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;
    @Value("${app.session.max-devices}")
    private int maxDevicesPerUser;

    @Transactional
    public DeviceSession createOrUpdateDeviceSession(User user, String providedDeviceId, String ipAddress, String userAgent) {
        String deviceId = (providedDeviceId == null || providedDeviceId.isBlank()) ? UUID.randomUUID().toString() : providedDeviceId;
        String newRefreshToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusMillis(refreshExpirationMs);

        DeviceSession session = deviceSessionRepository.findByUserIdAndDeviceId(user.getId(), deviceId).orElse(null);

        if (session != null) {
            session.setRefreshToken(newRefreshToken);
            session.setExpiresAt(expiresAt);
            session.setIpAddress(ipAddress);
            session.setUserAgent(userAgent);
            session.setLastAccessedAt(Instant.now());
        } else {
            long currentDevices = deviceSessionRepository.countByUserId(user.getId());
            if (currentDevices >= maxDevicesPerUser) {
                log.info("User {} has reached max device sessions. Deleting oldest session.", user.getEmail());
                deviceSessionRepository.findFirstByUserIdOrderByLastAccessedAtAsc(user.getId()).ifPresent(deviceSessionRepository::delete);
            }

            session = DeviceSession.builder()
                    .user(user)
                    .deviceId(deviceId)
                    .refreshToken(newRefreshToken)
                    .expiresAt(expiresAt)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .lastAccessedAt(Instant.now())
                    .build();
        }

        return deviceSessionRepository.save(session);
    }

    @Transactional
    public DeviceSession verifyAndRotateRefreshToken(String token, String ipAddress, String userAgent) {
        DeviceSession session = deviceSessionRepository.findByRefreshToken(token)
                .orElseThrow(() -> new TokenAuthenticationException("Refresh token is invalid or expired"));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            deleteDeviceSession(session);
            throw new TokenAuthenticationException("Refresh token expired");
        }

        User user = session.getUser();
        if (user.isDeleted() || !user.isEnabled() || user.isLocked()) {
            deleteDeviceSession(session);
            throw new TokenAuthenticationException("User account not valid");
        }

        session.setRefreshToken(UUID.randomUUID().toString());
        session.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        session.setLastAccessedAt(Instant.now());
        if (ipAddress != null)
            session.setIpAddress(ipAddress);
        if (userAgent != null)
            session.setUserAgent(userAgent);

        return deviceSessionRepository.save(session);
    }

    @Transactional
    public void deleteDeviceSession(DeviceSession session) {
        deviceSessionRepository.delete(session);
    }

    @Transactional
    public void deleteRefreshToken(String token) {
        deviceSessionRepository.findByRefreshToken(token).ifPresent(this::deleteDeviceSession);
    }

    @Transactional
    public void deleteAllUserSessions(UUID userId) {
        deviceSessionRepository.deleteAllByUserId(userId);
    }
}
