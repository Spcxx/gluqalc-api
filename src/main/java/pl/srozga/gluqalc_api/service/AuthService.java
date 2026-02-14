package pl.srozga.gluqalc_api.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.common.AuthProvider;
import pl.srozga.gluqalc_api.common.UserRole;
import pl.srozga.gluqalc_api.component.email.EmailVerificationTokenService;
import pl.srozga.gluqalc_api.dto.request.LoginRequest;
import pl.srozga.gluqalc_api.dto.response.TokenResponse;
import pl.srozga.gluqalc_api.entity.User;
import pl.srozga.gluqalc_api.exception.ApplicationAuthenticationException;
import pl.srozga.gluqalc_api.exception.TokenAuthenticationException;
import pl.srozga.gluqalc_api.repository.UserRepository;
import pl.srozga.gluqalc_api.security.jwt.JwtService;
import pl.srozga.gluqalc_api.security.jwt.RefreshTokenService;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenService emailVerificationTokenService;

    @Value("${app.google.client-id}")
    private String googleClientId;

    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new ApplicationAuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash()))
            throw new ApplicationAuthenticationException("Invalid email or password");
        if (!user.isEnabled())
            throw new ApplicationAuthenticationException("User account is not verified");
        if (user.isLocked())
            throw new ApplicationAuthenticationException("User account is locked");

        return generateTokensForUser(user);
    }

    @Transactional
    public TokenResponse refreshToken(String refreshTokenRequest) {
        User user = refreshTokenService.verifyAndGetUser(refreshTokenRequest);
        refreshTokenService.deleteRefreshToken(refreshTokenRequest);
        return generateTokensForUser(user);
    }

    @Transactional
    public TokenResponse loginWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null)
                throw new TokenAuthenticationException("Invalid Google ID token");

            String email = idToken.getPayload().getEmail();

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                log.info("Registering new user via Google: {}", email);
                User newUser = User.builder()
                        .email(email)
                        .provider(AuthProvider.GOOGLE)
                        .roles(Set.of(UserRole.USER))
                        .enabled(true)
                        .locked(false)
                        .build();
                return userRepository.save(newUser);
            });

            if (user.isLocked())
                throw new TokenAuthenticationException("User account is locked");
            if (!user.isEnabled())
                throw new TokenAuthenticationException("User account is not verified");

            return generateTokensForUser(user);
        } catch (IOException | GeneralSecurityException e) {
            log.error("Google authentication failed", e);
            throw new TokenAuthenticationException("Google authentication failed");
        }
    }

    @Transactional
    public void logout(String authHeader, UUID userId) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            jwtService.invalidateJwtToken(jwt);
        }
        refreshTokenService.deleteRefreshTokenByUserId(userId);
    }

    @Transactional
    public void verifyEmail(String token) {
        UUID userId = emailVerificationTokenService.validateToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationAuthenticationException("User not found"));

        if (user.isEnabled())
            return;

        user.setEnabled(true);
        userRepository.save(user);
        emailVerificationTokenService.deleteToken(token);
    }

    private TokenResponse generateTokensForUser(User user) {
        String userId = user.getId().toString();

        String jwt = jwtService.getActiveJwtToken(userId);

        if (jwt == null) {
            AuthUser authUser = AuthUser.fromEntity(user);
            jwt = jwtService.createJwtToken(authUser);
            jwtService.saveActiveJwtToken(userId, jwt);
        }

        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new TokenResponse(jwt, newRefreshToken);
    }
}
