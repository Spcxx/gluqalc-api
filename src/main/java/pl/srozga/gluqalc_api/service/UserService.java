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
import pl.srozga.gluqalc_api.dto.request.RegisterRequest;
import pl.srozga.gluqalc_api.dto.response.UserAdminResponse;
import pl.srozga.gluqalc_api.dto.response.UserResponse;
import pl.srozga.gluqalc_api.entity.User;
import pl.srozga.gluqalc_api.exception.ConflictException;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.repository.UserRepository;
import pl.srozga.gluqalc_api.security.jwt.RefreshTokenService;

import java.util.List;
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

    @Transactional
    public UserResponse createUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email()))
            throw new ConflictException("User with this email already exists");

        User user = User.builder()
                .email(registerRequest.email())
                .passwordHash(passwordEncoder.encode(registerRequest.password()))
                .roles(Set.of(UserRole.USER))
                .provider(AuthProvider.LOCAL)
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
        refreshTokenService.deleteRefreshTokenByUserId(user.getId());
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
    }

    @Transactional
    public void removeRoleFromUser(UUID id, UserRole role) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!user.getRoles().contains(role))
            throw new ConflictException("User does not have this role");
        user.getRoles().remove(role);
        userRepository.save(user);
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
                user.getProvider(),
                user.isEnabled(),
                user.isLocked(),
                user.isDeleted(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
