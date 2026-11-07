package com.qvorum.server.security;

import com.qvorum.server.model.Role;
import java.util.Set;

/**
 * The user who sent the current request, as JwtAuthenticationFilter found it. Controllers get it
 * with a parameter annotated @AuthenticationPrincipal:
 *
 * <pre>
 * public UserDto me(@AuthenticationPrincipal AuthenticatedUser currentUser) { ... }
 * </pre>
 *
 * @param clientId the client session the token belongs to (see ClientRegistry)
 */
public record AuthenticatedUser(Long userId, String clientId, String email, Set<Role> roles) {

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}
