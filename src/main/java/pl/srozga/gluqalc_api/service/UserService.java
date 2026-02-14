package pl.srozga.gluqalc_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.common.AuthProvider;
import pl.srozga.gluqalc_api.common.UserRole;
import pl.srozga.gluqalc_api.dto.request.RegisterRequest;
import pl.srozga.gluqalc_api.dto.response.UserResponse;
import pl.srozga.gluqalc_api.entity.User;
import pl.srozga.gluqalc_api.exception.ConflictException;
import pl.srozga.gluqalc_api.repository.UserRepository;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email()))
            throw new ConflictException("User with this email already exists");

        User user = User.builder()
                .email(registerRequest.email())
                .passwordHash(passwordEncoder.encode(registerRequest.password()))
                .roles(Set.of(UserRole.USER))
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .locked(false)
                .build();

        log.info("Created new user: {}", user.getEmail());

        User addedUser = userRepository.save(user);
        return mapToResponse(addedUser);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
