package com.qvorum.server.monitoring;

import com.qvorum.server.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Records every request with the status the server answered (UC25) and refreshes the client's
 * "last seen" time (UC24).
 *
 * A filter is code that runs around every request, before and after the controller. SecurityConfig
 * places this one right after JwtAuthenticationFilter, so it already knows who the user is, and
 * it still sees the final status when a later security step rejects the request (401 / 403).
 *
 * Not a @Component on purpose: Spring Boot would then also register it a second time, outside
 * the security chain, and every request would be logged twice.
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final RequestLogService requestLogService;
    private final ClientRegistry clientRegistry;

    public RequestLoggingFilter(RequestLogService requestLogService, ClientRegistry clientRegistry) {
        this.requestLogService = requestLogService;
        this.clientRegistry = clientRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            AuthenticatedUser user = currentUser();
            String clientId = user == null ? null : user.clientId();
            Long userId = user == null ? null : user.userId();
            if (clientId != null) {
                clientRegistry.touch(clientId);
            }
            requestLogService.recordRequest(clientId, userId, request.getMethod(), request.getRequestURI(),
                    response.getStatus(), System.currentTimeMillis() - start);
        }
    }

    /** CORS preflight requests are sent by the browser itself, not by the app, so they are skipped. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod());
    }

    private static AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }
}
