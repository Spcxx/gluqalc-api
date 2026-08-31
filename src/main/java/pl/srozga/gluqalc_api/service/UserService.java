package pl.srozga.gluqalc_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.common.AuthProvider;
import pl.srozga.gluqalc_api.common.UserRole;
import pl.srozga.gluqalc_api.component.email.EmailService;
import pl.srozga.gluqalc_api.component.email.EmailVerificationTokenService;
import pl.srozga.gluqalc_api.dto.request.*;
import pl.srozga.gluqalc_api.dto.response.DeviceSessionResponse;
import pl.srozga.gluqalc_api.dto.response.UserAdminResponse;
import pl.srozga.gluqalc_api.dto.response.UserResponse;
import pl.srozga.gluqalc_api.entity.User;
import pl.srozga.gluqalc_api.exception.ApplicationAuthenticationException;
import pl.srozga.gluqalc_api.exception.ConflictException;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.exception.TokenAuthenticationException;
import pl.srozga.gluqalc_api.repository.DeviceSessionRepository;
import pl.srozga.gluqalc_api.repository.UserRepository;
import pl.srozga.gluqalc_api.security.jwt.JwtService;
import pl.srozga.gluqalc_api.security.jwt.RefreshTokenService;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final DeviceSessionRepository deviceSessionRepository;
    private final JwtService jwtService;

    @Transactional
    public UserResponse createUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email()))
            throw new ConflictException("User with this email already exists");

        User user = User.builder()
                .email(registerRequest.email())
                .passwordHash(passwordEncoder.encode(registerRequest.password()))
                .roles(Set.of(UserRole.USER))
                .providers(Set.of(AuthProvider.LOCAL))
                .enabled(false)
                .locked(false)
                .build();

        log.info("Created new user: {}", user.getEmail());

        User addedUser = userRepository.save(user);

        String verificationToken = verificationTokenService.createVerificationToken(addedUser.getId());
        emailService.sendVerificationEmail(addedUser.getEmail(), verificationToken);

        return mapToResponse(addedUser);
    }

    @Transactional(readOnly = true)
    public List<UserAdminResponse> getAllActiveUsers() {
        return userRepository.findAllByDeletedFalse().stream()
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserAdminResponse getUserById(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return mapToAdminResponse(user);
    }

    @Transactional
    public void softDeleteUser(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setDeleted(true);
        userRepository.save(user);
        refreshTokenService.deleteAllUserSessions(user.getId());
        log.info("Soft deleted user: {}", id);
    }

    @Transactional(readOnly = true)
    public Set<UserRole> getUserRoles(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return user.getRoles();
    }

    @Transactional
    public void addRoleToUser(UUID id, UserRole role) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRoles().contains(role))
            throw new ConflictException("User already has this role");
        user.getRoles().add(role);
        userRepository.save(user);
        log.info("Added role {} to user {}", role, id);
    }

    @Transactional
    public void removeRoleFromUser(UUID id, UserRole role) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!user.getRoles().contains(role))
            throw new ConflictException("User does not have this role");
        user.getRoles().remove(role);
        userRepository.save(user);
        log.info("Removed role {} from user {}", role, id);
    }

    @Transactional(readOnly = true)
    public List<DeviceSessionResponse> getUserSessions(UUID userId) {
        userRepository.findByIdAndDeletedFalse(userId).orElseThrow(() -> new NotFoundException("User not found"));

        return deviceSessionRepository.findAllByUserId(userId).stream()
                .map(session -> new DeviceSessionResponse(
                        session.getDeviceId(),
                        session.getIpAddress(),
                        session.getUserAgent(),
                        session.getLastAccessedAt(),
                        session.getExpiresAt()
                ))
                .toList();
    }

    @Transactional
    public void revokeDeviceSession(UUID targetUserId, String targetDeviceId, AuthUser currentUser, String authHeader) {
        deviceSessionRepository.deleteByUserIdAndDeviceId(targetUserId, targetDeviceId);

        boolean isDeletingOwnCurrentSession = targetUserId.equals(currentUser.id())
                && targetDeviceId.equals(currentUser.deviceId());

        if (isDeletingOwnCurrentSession && authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            jwtService.invalidateJwtToken(jwt);
        }

        log.info("Revoked session for device {} of user {} by user {}", targetDeviceId, targetUserId, currentUser.id());
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        Optional<User> optionalUser = userRepository.findByEmailAndDeletedFalse(email);
        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();
        if (!user.getProviders().contains(AuthProvider.LOCAL))
            return;

        String token = verificationTokenService.createPasswordResetToken(user.getId());
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Transactional
    public void completePasswordReset(PasswordResetConfirmRequest request) {
        UUID userId = verificationTokenService.validatePasswordResetToken(request.code());
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getEmail().equals(request.email())) {
            throw new TokenAuthenticationException("Invalid email for this reset code");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenService.deleteAllUserSessions(user.getId());
        verificationTokenService.deletePasswordResetToken(request.code());

        emailService.sendSecurityAlertEmail(
                user.getEmail(),
                "The password for your GluQalc account has been changed."
        );

        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    @Transactional
    public void changeUnverifiedUserEmail(ChangeUnverifiedEmailRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.oldEmail())
                .orElseThrow(() -> new ApplicationAuthenticationException("Invalid email or password"));

        if (user.isEnabled())
            throw new ConflictException("Account is already verified.");
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new ApplicationAuthenticationException("Invalid email or password");

        String newEmail = request.newEmail().toLowerCase();

        if (user.getEmail().equalsIgnoreCase(newEmail))
            throw new ConflictException("New email must be different from the current one");
        if (userRepository.existsByEmail(newEmail))
            throw new ConflictException("Email is already taken");

        user.setEmail(newEmail);
        userRepository.save(user);

        verificationTokenService.deleteVerificationTokenForUser(user.getId());
        String newToken = verificationTokenService.createVerificationToken(user.getId());

        emailService.sendVerificationEmail(newEmail, newToken);
        log.info("Changed email for unverified user {}. Sent new verification code to {}", user.getId(), newEmail);
    }

    public void requestVerifiedUserEmailChange(UUID userId, ChangeEmailRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new ApplicationAuthenticationException("Invalid email or password");

        if (!user.getProviders().contains(AuthProvider.LOCAL))
            throw new ConflictException("Email change is not available for users registered via external providers");
        if (user.getProviders().size() > 1)
            throw new ConflictException("Email change is not available for users with multiple authentication providers");

        if (!user.isEnabled())
            throw new ConflictException("Account is not verified");

        String newEmail = request.newEmail().toLowerCase();

        if (user.getEmail().equalsIgnoreCase(newEmail))
            throw new ConflictException("New email must be different from the current one");
        if (userRepository.existsByEmail(newEmail))
            throw new ConflictException("Email is already taken");

        String token = verificationTokenService.createEmailChangeToken(user.getId(), newEmail);
        emailService.sendEmailChangeConfirmationEmail(newEmail, token);

        log.info("User {} requested email change. Sent confirmation code to {}", user.getId(), newEmail);
    }

    @Transactional
    public void confirmVerifiedUserEmailChange(UUID userId, ConfirmEmailChangeRequest request) {
        String[] tokenData = verificationTokenService.validateEmailChangeToken(request.code());
        UUID tokenUserId = UUID.fromString(tokenData[0]);
        String tokenNewEmail = tokenData[1];

        if (!tokenUserId.equals(userId))
            throw new ApplicationAuthenticationException("Invalid token for this user");

        if (!tokenNewEmail.equalsIgnoreCase(request.newEmail()))
            throw new ConflictException("Email does not match the requested change");

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getProviders().contains(AuthProvider.LOCAL))
            throw new ConflictException("Email change is not available for users registered via external providers");
        if (user.getProviders().size() > 1)
            throw new ConflictException("Email change is not available for users with multiple authentication providers");
        if (userRepository.existsByEmail(tokenNewEmail))
            throw new ConflictException("Email is already taken");

        String oldEmail = user.getEmail();

        user.setEmail(tokenNewEmail);
        userRepository.save(user);

        verificationTokenService.deleteEmailChangeToken(request.code());

        emailService.sendSecurityAlertEmail(
                oldEmail,
                "The email address associated with your Gluqalc account has been changed to: " + tokenNewEmail + "."
        );

        log.info("Confirmed email change for verified user {} from {} to {}", user.getId(), oldEmail, tokenNewEmail);
    }

    public void requestUserDeletion(UUID userId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String token = verificationTokenService.createAccountDeletionToken(user.getId());
        emailService.sendAccountDeletionEmail(user.getEmail(), token);

        log.info("User {} requested account deletion", user.getId());
    }

    @Transactional
    public void confirmUserDeletion(UUID userId, String code, String authHeader) {
        UUID tokenUserId = verificationTokenService.validateAccountDeletionToken(code);

        if (!tokenUserId.equals(userId))
            throw new ApplicationAuthenticationException("Invalid token for this user");

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String userEmail = user.getEmail();

        user.setDeleted(true);
        userRepository.save(user);

        verificationTokenService.deleteAccountDeletionToken(code);
        refreshTokenService.deleteAllUserSessions(user.getId());

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            jwtService.invalidateJwtToken(jwt);
        }

        emailService.sendAccountDeletedConfirmationEmail(userEmail);

        log.info("User {} successfully deleted their own account", user.getId());
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    private UserAdminResponse mapToAdminResponse(User user) {
        return new UserAdminResponse(
                user.getId(),
                user.getEmail(),
                user.getRoles(),
                user.getProviders(),
                user.isEnabled(),
                user.isLocked(),
                user.isDeleted(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
