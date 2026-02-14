package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotNull;
import pl.srozga.gluqalc_api.common.UserRole;

public record RoleRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {
}
