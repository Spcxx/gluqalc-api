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

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String createVerificationToken(UUID userId) {
        int attempts = 0;
        while (attempts < 10) {
            int code = 100000 + secureRandom.nextInt(900000);
            String token = String.valueOf(code);
            String key = REDIS_PREFIX + token;

            if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().set(key, userId.toString(), Duration.ofMinutes(15));
                return token;
            }

            attempts++;
        }

        throw new RuntimeException("Failed to generate unique verification token");
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
}
