package pl.srozga.gluqalc_api.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import pl.srozga.gluqalc_api.common.UserRole;
import pl.srozga.gluqalc_api.exception.TokenAuthenticationException;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {
    private static final String ROLES_CLAIM = "roles";
    private static final String EMAIL_CLAIM = "email";
    private static final String REDIS_BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REDIS_ACTIVE_TOKEN_PREFIX = "jwt:active:";

    private final Algorithm signingAlgorithm;
    private final JWTVerifier verifier;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.jwt.expiration-time-ms}")
    private long tokenExpirationTimeMs;

    public JwtService(@Value("${app.jwt.secret}") String signingSecret, StringRedisTemplate stringRedisTemplate) {
        this.signingAlgorithm = Algorithm.HMAC256(signingSecret);
        this.verifier = JWT.require(signingAlgorithm).build();
        this.redisTemplate = stringRedisTemplate;
    }

    public String getActiveJwtToken(String userId) {
        return redisTemplate.opsForValue().get(REDIS_ACTIVE_TOKEN_PREFIX + userId);
    }

    public void saveActiveJwtToken(String userId, String token) {
        redisTemplate.opsForValue().set(REDIS_ACTIVE_TOKEN_PREFIX + userId, token);
    }

    public AuthUser resolveJwtToken(String token) {
        try {
            String redisKey = REDIS_BLACKLIST_PREFIX + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey)))
                throw new TokenAuthenticationException("JWT token has been invalidated");

            DecodedJWT decodedJWT = verifier.verify(token);

            String userId = decodedJWT.getSubject();
            String email = decodedJWT.getClaim(EMAIL_CLAIM).asString();
            Set<UserRole> roles = decodedJWT.getClaim(ROLES_CLAIM).asList(String.class).stream().map(UserRole::valueOf).collect(java.util.stream.Collectors.toSet());

            return new AuthUser(UUID.fromString(userId), email, roles, null, true, false);
        } catch (JWTVerificationException e) {
            throw new TokenAuthenticationException("Invalid JWT token");
        }
    }

    public void invalidateJwtToken(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            String userId = decodedJWT.getSubject();

            String redisKey = REDIS_BLACKLIST_PREFIX + token;
            Instant expirationTime = decodedJWT.getExpiresAtAsInstant();
            long timeToLive = Duration.between(Instant.now(), expirationTime).toMillis();

            if (timeToLive > 0) {
                redisTemplate.opsForValue().set(redisKey, "true", timeToLive, TimeUnit.MILLISECONDS);
                redisTemplate.delete(REDIS_ACTIVE_TOKEN_PREFIX + userId);
            }
        } catch (JWTVerificationException e) {
            throw new TokenAuthenticationException("Invalid JWT token");
        }
    }

    public String createJwtToken(AuthUser user) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusMillis(tokenExpirationTimeMs);
        List<String> roles = user.roles().stream().map(Enum::name).toList();

        return JWT.create()
                .withSubject(user.id().toString())
                .withClaim(EMAIL_CLAIM, user.email())
                .withClaim(ROLES_CLAIM, roles)
                .withIssuedAt(now)
                .withExpiresAt(expirationTime)
                .sign(signingAlgorithm);
    }
}
