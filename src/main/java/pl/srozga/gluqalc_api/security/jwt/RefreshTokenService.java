package pl.srozga.gluqalc_api.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import pl.srozga.gluqalc_api.entity.User;
import pl.srozga.gluqalc_api.exception.TokenAuthenticationException;
import pl.srozga.gluqalc_api.repository.UserRepository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private static final String REDIS_REFRESH_TOKEN_PREFIX = "jwt:refresh:";
    private static final String REDIS_USER_REFRESH_MAP_PREFIX = "jwt:user_refresh:";

    public String createRefreshToken(UUID userId) {
        deleteRefreshTokenByUserId(userId);

        String token = UUID.randomUUID().toString();
        String tokenKey = REDIS_REFRESH_TOKEN_PREFIX + token;

        redisTemplate.opsForValue().set(
                tokenKey,
                userId.toString(),
                refreshExpirationMs,
                TimeUnit.MILLISECONDS
        );

        redisTemplate.opsForValue().set(
                REDIS_USER_REFRESH_MAP_PREFIX + userId,
                token,
                refreshExpirationMs,
                TimeUnit.MILLISECONDS
        );

        return token;
    }

    public User verifyAndGetUser(String token) {
        String tokenKey = REDIS_REFRESH_TOKEN_PREFIX + token;
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);

        if (userIdStr == null)
            throw new TokenAuthenticationException("Refresh token is invalid or expired");

        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new TokenAuthenticationException("User associated with this token does not exist"));

        if (!user.isEnabled()) {
            deleteRefreshToken(tokenKey);
            throw new TokenAuthenticationException("User account is disabled");
        }
        if (user.isLocked()) {
            deleteRefreshToken(tokenKey);
            throw new TokenAuthenticationException("User account is locked");
        }

        return user;
    }

    public void deleteRefreshToken(String token) {
        String tokenKey = REDIS_REFRESH_TOKEN_PREFIX + token;
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);

        if (userIdStr != null) {
            UUID userId = UUID.fromString(userIdStr);
            redisTemplate.delete(REDIS_USER_REFRESH_MAP_PREFIX + userId);
        }

        redisTemplate.delete(tokenKey);
    }

    public void deleteRefreshTokenByUserId(UUID userId) {
        String userKey = REDIS_USER_REFRESH_MAP_PREFIX + userId;
        String token = redisTemplate.opsForValue().get(userKey);

        if (token != null) {
            redisTemplate.delete(REDIS_REFRESH_TOKEN_PREFIX + token);
        }
        redisTemplate.delete(userKey);
    }
}
