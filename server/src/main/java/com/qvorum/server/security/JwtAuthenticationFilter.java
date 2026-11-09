package com.qvorum.server.security;

import com.qvorum.server.model.User;
import com.qvorum.server.monitoring.ClientRegistry;
import com.qvorum.server.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Runs before every request. If the request carries "Authorization: Bearer <token>" and the token
 * is valid, it tells Spring Security who the user is. Otherwise it does nothing, and the request
 * continues as anonymous; SecurityConfig then answers 401 for any endpoint that needs a login.
 *
 * A token is accepted only if all of these hold:
 * 1. its signature and expiry date are valid (JwtService.parse);
 * 2. its client session is still connected (not logged out or disconnected by the system admin);
 * 3. the user still exists (an account deleted after login loses access at once).
 *
 * Not a @Component, for the same reason as RequestLoggingFilter: SecurityConfig creates it.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    /** Request attribute set when the token's session was closed by the system administrator. */
    public static final String CLOSED_BY_ADMINISTRATOR = "qvorum.closedByAdministrator";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ClientRegistry clientRegistry;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, ClientRegistry clientRegistry) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.clientRegistry = clientRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIX)) {
            authenticate(header.substring(PREFIX.length()), request);
        }
        chain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        JwtService.TokenContent content;
        try {
            content = jwtService.parse(token);
        } catch (JwtException | IllegalArgumentException e) {
            return; // invalid or expired token: stay anonymous
        }
        if (!clientRegistry.isConnected(content.clientId())) {
            if (clientRegistry.wasClosedByAdministrator(content.clientId())) {
                // Read by SecurityConfig to choose the 401 message.
                request.setAttribute(CLOSED_BY_ADMINISTRATOR, Boolean.TRUE);
            }
            return;
        }
        User user = userRepository.findById(content.userId()).orElse(null);
        if (user == null) {
            return;
        }

        AuthenticatedUser principal = new AuthenticatedUser(
                user.getId(), content.clientId(), user.getEmail(), Set.copyOf(user.getRoles()));
        // Spring Security checks roles as "authorities" named ROLE_<name>; hasRole("ADMINISTRATOR")
        // looks for ROLE_ADMINISTRATOR.
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }
}
