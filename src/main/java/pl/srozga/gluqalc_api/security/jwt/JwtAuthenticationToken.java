package pl.srozga.gluqalc_api.security.jwt;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.util.Collection;
import java.util.Collections;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final @Nullable AuthUser authUser;
    private @Nullable String token;

    private JwtAuthenticationToken(Collection<? extends GrantedAuthority> authorities, @Nullable AuthUser authUser, boolean authenticated, @Nullable String jwtToken) {
        super(authorities);
        super.setAuthenticated(authenticated);
        this.authUser = authUser;
        this.token = jwtToken;
    }

    public static JwtAuthenticationToken unauthenticated(String jwtToken) {
        return new JwtAuthenticationToken(Collections.emptyList(), null, false, jwtToken);
    }

    public static JwtAuthenticationToken authenticated(AuthUser authUser) {
        return new JwtAuthenticationToken(authUser.getAuthorities(), authUser, true, null);
    }

    @Override
    public @Nullable String getCredentials() {
        return token;
    }

    @Override
    public @Nullable AuthUser getPrincipal() {
        return authUser;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.token = null;
    }
}
