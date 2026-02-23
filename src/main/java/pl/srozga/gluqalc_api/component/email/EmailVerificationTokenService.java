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
    private static final String REDIS_USER_MAPPING = "auth:verify:user:";
    private static final String REDIS_EMAIL_CHANGE_PREFIX = "auth:change-email:";
    private static final String REDIS_ACCOUNT_DELETION_PREFIX = "auth:delete-account:";

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String createPasswordResetToken(UUID userId) {
        return generateAndSaveToken(REDIS_PWD_RESET_PREFIX, userId, false);
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
        return generateAndSaveToken(REDIS_PREFIX, userId, true);
    }

    public UUID validateToken(String token) {
        String userIdStr = redisTemplate.opsForValue().get(REDIS_PREFIX + token);
        if (userIdStr == null)
            throw new TokenAuthenticationException("Verification token is invalid or expired");
        return UUID.fromString(userIdStr);
    }

    public void deleteToken(String token) {
        String userIdStr = redisTemplate.opsForValue().get(REDIS_PREFIX + token);
        redisTemplate.delete(REDIS_PREFIX + token);
        if (userIdStr != null)
            redisTemplate.delete(REDIS_USER_MAPPING + userIdStr);
    }

    public void deleteVerificationTokenForUser(UUID userId) {
        String token = redisTemplate.opsForValue().get(REDIS_USER_MAPPING + userId.toString());
        if (token != null) {
            redisTemplate.delete(REDIS_PREFIX + token);
            redisTemplate.delete(REDIS_USER_MAPPING + userId);
        }
    }

    public String createEmailChangeToken(UUID userId, String newEmail) {
        int attempts = 0;
        while (attempts < 10) {
            int code = 100000 + secureRandom.nextInt(900000);
            String token = String.valueOf(code);

            if (Boolean.FALSE.equals(redisTemplate.hasKey(REDIS_EMAIL_CHANGE_PREFIX + token))) {
                String value = userId.toString() + "|" + newEmail;
                redisTemplate.opsForValue().set(REDIS_EMAIL_CHANGE_PREFIX + token, value, Duration.ofMinutes(15));
                return token;
            }
            attempts++;
        }
        throw new RuntimeException("Failed to generate unique code token");
    }

    public String[] validateEmailChangeToken(String token) {
        String value = redisTemplate.opsForValue().get(REDIS_EMAIL_CHANGE_PREFIX + token);
        if (value == null) {
            throw new TokenAuthenticationException("Email change code is invalid or expired");
        }
        return value.split("\\|");
    }

    public void deleteEmailChangeToken(String token) {
        redisTemplate.delete(REDIS_EMAIL_CHANGE_PREFIX + token);
    }

    public String createAccountDeletionToken(UUID userId) {
        return generateAndSaveToken(REDIS_ACCOUNT_DELETION_PREFIX, userId, false);
    }

    public UUID validateAccountDeletionToken(String token) {
        String userIdStr = redisTemplate.opsForValue().get(REDIS_ACCOUNT_DELETION_PREFIX + token);
        if (userIdStr == null)
            throw new TokenAuthenticationException("Deletion code is invalid or expired");
        return UUID.fromString(userIdStr);
    }

    public void deleteAccountDeletionToken(String token) {
        redisTemplate.delete(REDIS_ACCOUNT_DELETION_PREFIX + token);
    }

    private String generateAndSaveToken(String prefix, UUID userId, boolean saveUserMapping) {
        int attempts = 0;
        while (attempts < 10) {
            int code = 100000 + secureRandom.nextInt(900000);
            String token = String.valueOf(code);

            if (Boolean.FALSE.equals(redisTemplate.hasKey(prefix + token))) {
                Duration expiration = Duration.ofMinutes(15);
                redisTemplate.opsForValue().set(prefix + token, userId.toString(), expiration);
                if (saveUserMapping)
                    redisTemplate.opsForValue().set(REDIS_USER_MAPPING + userId, token, expiration);

                return token;
            }

            attempts++;
        }

        throw new RuntimeException("Failed to generate unique code token");
    }
}
