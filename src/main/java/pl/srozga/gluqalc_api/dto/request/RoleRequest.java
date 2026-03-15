package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import pl.srozga.gluqalc_api.common.UserRole;

@Schema(description = "Request payload for assigning a system role to a user")
public record RoleRequest(
        @Schema(description = "The role to assign", example = "USER")
        @NotNull(message = "Role is required")
        UserRole role
) {
}