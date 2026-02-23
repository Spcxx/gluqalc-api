package pl.srozga.gluqalc_api.component.rateLimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public RateLimitResponse checkRateLimit(String ip, String actionKey, int maxRequests, int timeWindowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + ip + ":" + actionKey;
        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);
            long count = currentCount != null ? currentCount : 0;

            if (count == 1L)
                redisTemplate.expire(redisKey, timeWindowSeconds, TimeUnit.SECONDS);

            long remainingTokens = Math.max(0, maxRequests - count);
            boolean allowed = count <= maxRequests;

            long retryAfterSeconds = 0;
            if (!allowed) {
                Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
                if (ttl == null || ttl == -1) {
                    redisTemplate.expire(redisKey, timeWindowSeconds, TimeUnit.SECONDS);
                    retryAfterSeconds = timeWindowSeconds;
                } else
                    retryAfterSeconds = ttl;
            }

            return new RateLimitResponse(allowed, remainingTokens, retryAfterSeconds);
        } catch (Exception e) {
            log.error("Redis rate limit error for IP: {}", ip, e);
            return new RateLimitResponse(true, maxRequests, 0);
        }
    }

    public record RateLimitResponse(
            boolean allowed,
            long remainingTokens,
            long retryAfterSeconds
    ) {}
}
