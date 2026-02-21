package pl.srozga.gluqalc_api.dto.response;

import pl.srozga.gluqalc_api.common.AuthProvider;
import pl.srozga.gluqalc_api.common.UserRole;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserAdminResponse(
        UUID id,
        String email,
        Set<UserRole> roles,
        Set<AuthProvider> provider,
        boolean enabled,
        boolean locked,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt
) {
}
