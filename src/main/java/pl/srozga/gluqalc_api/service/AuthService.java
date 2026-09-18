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
import pl.srozga.gluqalc_api.entity.DeviceSession;
import pl.srozga.gluqalc_api.entity.User;
import pl.srozga.gluqalc_api.exception.ApplicationAuthenticationException;
import pl.srozga.gluqalc_api.exception.ConflictException;
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
    private final ConsentService consentService;

    @Value("${app.google.client-id}")
    private String googleClientId;

    @Transactional
    public TokenResponse login(LoginRequest loginRequest, String ipAddress, String userAgent) {
        User user = userRepository.findByEmailAndDeletedFalse(loginRequest.email())
                .orElseThrow(() -> new ApplicationAuthenticationException("Invalid email or password"));

        if (!user.getProviders().contains(AuthProvider.LOCAL))
            throw new ApplicationAuthenticationException("This user is registered via external provider");
        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash()))
            throw new ApplicationAuthenticationException("Invalid email or password");
        if (!user.isEnabled())
            throw new ApplicationAuthenticationException("User account is not verified");
        if (user.isLocked())
            throw new ApplicationAuthenticationException("User account is locked");

        return generateTokensForUser(user, loginRequest.deviceId(), ipAddress, userAgent);
    }

    @Transactional
    public TokenResponse refreshToken(String refreshTokenRequest, String ipAddress, String userAgent) {
        DeviceSession session = refreshTokenService.verifyAndRotateRefreshToken(refreshTokenRequest, ipAddress, userAgent);
        boolean consentsPending = consentService.hasPendingRequiredConsents(session.getUser().getId());

        AuthUser authUser = AuthUser.fromEntity(session.getUser(), consentsPending);
        String jwt = jwtService.createJwtToken(authUser, session.getDeviceId());
        long expiresIn = jwtService.getTokenExpirationTimeInSeconds();

        return new TokenResponse(
                jwt,
                session.getRefreshToken(),
                expiresIn,
                session.getDeviceId()
        );
    }

    @Transactional
    public TokenResponse loginWithGoogle(String idTokenString, String deviceId, String ipAddress, String userAgent) {
        try {
            GoogleIdToken idToken = verifyGoogleIdToken(idTokenString);
            String email = idToken.getPayload().getEmail();

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                log.info("Registering new user via Google: {}", email);
                User newUser = User.builder()
                        .email(email)
                        .providers(Set.of(AuthProvider.GOOGLE))
                        .roles(Set.of(UserRole.USER))
                        .enabled(true)
                        .locked(false)
                        .deleted(false)
                        .build();
                return userRepository.save(newUser);
            });

            if (!user.getProviders().contains(AuthProvider.GOOGLE))
                throw new ApplicationAuthenticationException("This email is registered via different provider");
            if (user.isLocked())
                throw new TokenAuthenticationException("User account is locked");
            if (!user.isEnabled())
                throw new TokenAuthenticationException("User account is not verified");
            if (user.isDeleted())
                throw new TokenAuthenticationException("User account not found");

            return generateTokensForUser(user, deviceId, ipAddress, userAgent);
        } catch (IOException | GeneralSecurityException e) {
            log.error("Google authentication failed", e);
            throw new TokenAuthenticationException("Google authentication failed");
        }
    }

    @Transactional
    public void logout(String authHeader, String refreshToken) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            jwtService.invalidateJwtToken(jwt);
        }
        if (refreshToken != null && !refreshToken.isEmpty())
            refreshTokenService.deleteRefreshToken(refreshToken);
    }

    @Transactional
    public void verifyEmail(String token) {
        UUID userId = emailVerificationTokenService.validateToken(token);
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ApplicationAuthenticationException("User not found"));

        if (user.isEnabled())
            return;

        user.setEnabled(true);
        userRepository.save(user);
        emailVerificationTokenService.deleteToken(token);
    }

    @Transactional
    public void linkGoogleAccount(String idTokenString, UUID userId) {
        try {
            GoogleIdToken idToken = verifyGoogleIdToken(idTokenString);
            String email = idToken.getPayload().getEmail();
            User user = userRepository.findByIdAndDeletedFalse(userId)
                    .orElseThrow(() -> new ApplicationAuthenticationException("User not found"));

            if (!user.getEmail().equalsIgnoreCase(email))
                throw new ApplicationAuthenticationException("Google email does not match profile email");
            if (user.getProviders().contains(AuthProvider.GOOGLE))
                throw new ConflictException("Google account is already linked");

            user.getProviders().add(AuthProvider.GOOGLE);
            userRepository.save(user);
        } catch (IOException | GeneralSecurityException e) {
            log.error("Google linking failed", e);
            throw new TokenAuthenticationException("Google authentication failed");
        }
    }

    @Transactional
    public void linkLocalAccount(String newPassword, UUID userId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ApplicationAuthenticationException("User not found"));
        if (user.getProviders().contains(AuthProvider.LOCAL))
            throw new ConflictException("Password is already set");

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.getProviders().add(AuthProvider.LOCAL);
        userRepository.save(user);
    }

    private TokenResponse generateTokensForUser(User user, String providedDeviceId, String ipAddress, String userAgent) {
        DeviceSession session = refreshTokenService.createOrUpdateDeviceSession(user, providedDeviceId, ipAddress, userAgent);
        boolean consentsPending = consentService.hasPendingRequiredConsents(user.getId());
        AuthUser authUser = AuthUser.fromEntity(user, consentsPending);
        String jwt = jwtService.createJwtToken(authUser, session.getDeviceId());
        long expiresIn = jwtService.getTokenExpirationTimeInSeconds();

        return new TokenResponse(
                jwt,
                session.getRefreshToken(),
                expiresIn,
                session.getDeviceId()
        );
    }

    private GoogleIdToken verifyGoogleIdToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null)
            throw new TokenAuthenticationException("Invalid Google ID token");
        return idToken;
    }
}
