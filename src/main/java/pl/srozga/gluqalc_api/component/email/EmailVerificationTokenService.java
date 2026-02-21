package pl.srozga.gluqalc_api.component.email;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import pl.srozga.gluqalc_api.exception.TokenAuthenticationException;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenService {
    private static final String REDIS_PREFIX = "auth:verify:";
    private static final String REDIS_PWD_RESET_PREFIX = "auth:password-reset:";

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String createPasswordResetToken(UUID userId) {
        return generateAndSaveToken(REDIS_PWD_RESET_PREFIX, userId);
    }

    public UUID validatePasswordResetToken(String token) {
        String userIdStr = redisTemplate.opsForValue().get(REDIS_PWD_RESET_PREFIX + token);
        if (userIdStr == null)
            throw new TokenAuthenticationException("Reset code is invalid or expired");
        return UUID.fromString(userIdStr);
    }

    public void deletePasswordResetToken(String token) {
        redisTemplate.delete(REDIS_PWD_RESET_PREFIX + token);
    }

    public String createVerificationToken(UUID userId) {
        return generateAndSaveToken(REDIS_PREFIX, userId);
    }

    public UUID validateToken(String token) {
        String userIdStr = redisTemplate.opsForValue().get(REDIS_PREFIX + token);
        if (userIdStr == null)
            throw new TokenAuthenticationException("Verification token is invalid or expired");
        return UUID.fromString(userIdStr);
    }

    public void deleteToken(String token) {
        redisTemplate.delete(REDIS_PREFIX + token);
    }

    private String generateAndSaveToken(String prefix, UUID userId) {
        int attempts = 0;
        while (attempts < 10) {
            int code = 100000 + secureRandom.nextInt(900000);
            String token = String.valueOf(code);

            if (Boolean.FALSE.equals(redisTemplate.hasKey(prefix + token))) {
                redisTemplate.opsForValue().set(prefix + token, userId.toString(), Duration.ofMinutes(15));
                return token;
            }

            attempts++;
        }

        throw new RuntimeException("Failed to generate unique code token");
    }
}
