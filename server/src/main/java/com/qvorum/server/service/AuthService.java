package com.qvorum.server.service;

import com.qvorum.server.dto.LoginRequest;
import com.qvorum.server.dto.LoginResponse;
import com.qvorum.server.dto.UserDto;
import com.qvorum.server.exception.ApiException;
import com.qvorum.server.model.User;
import com.qvorum.server.repository.UserRepository;
import com.qvorum.server.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * service: business logic and rules. Uses repositories for data and returns DTOs to controllers.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        // The same message for an unknown email and a wrong password, so the response does not
        // reveal which emails have accounts.
        User user = userRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        return new LoginResponse(jwtService.generateToken(user), UserDto.from(user));
    }
}
