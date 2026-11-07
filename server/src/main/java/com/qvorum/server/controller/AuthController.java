package com.qvorum.server.controller;

import com.qvorum.server.dto.LoginRequest;
import com.qvorum.server.dto.LoginResponse;
import com.qvorum.server.dto.UserDto;
import com.qvorum.server.security.AuthenticatedUser;
import com.qvorum.server.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * controller: REST endpoints. Translates HTTP requests into service calls and returns DTOs.
 * No business logic and no database access here.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /auth/login: open to everyone (see SecurityConfig). */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return authService.login(request, http.getRemoteAddr());
    }

    /** POST /auth/logout: answers 204 No Content; the token cannot be used afterwards. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        authService.logout(currentUser);
    }

    /** GET /auth/me: the logged-in user, so an app can restore its session from a saved token. */
    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return authService.me(currentUser);
    }
}
