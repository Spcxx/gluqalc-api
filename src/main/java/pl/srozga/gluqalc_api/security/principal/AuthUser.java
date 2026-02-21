package pl.srozga.gluqalc_api.security.principal;

import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.srozga.gluqalc_api.common.UserRole;
import pl.srozga.gluqalc_api.entity.User;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@NullMarked
public record AuthUser(
        UUID id,
        String email,
        Set<UserRole> roles,
        @Nullable PasswordHashWrapper passwordHash,
        boolean enabled,
        boolean locked,
        @Nullable String deviceId
) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
    }

    @Override
    public @Nullable String getPassword() {
        return passwordHash != null ? passwordHash.value : null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public record PasswordHashWrapper(@NotNull String value) {
        @Override
        public String toString() {
            return "[PROTECTED]";
        }
    }

    public static AuthUser fromEntity(User user) {
        return new AuthUser(
                user.getId(),
                user.getEmail(),
                new java.util.HashSet<>(user.getRoles()),
                user.getPasswordHash() != null ? new PasswordHashWrapper(user.getPasswordHash()) : null,
                user.isEnabled(),
                user.isLocked(),
                null
        );
    }
}
