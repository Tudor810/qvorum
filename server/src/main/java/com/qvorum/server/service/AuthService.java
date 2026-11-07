package com.qvorum.server.service;

import com.qvorum.server.dto.LoginRequest;
import com.qvorum.server.dto.LoginResponse;
import com.qvorum.server.dto.UserDto;
import com.qvorum.server.exception.ApiException;
import com.qvorum.server.model.User;
import com.qvorum.server.monitoring.ClientRegistry;
import com.qvorum.server.repository.UserRepository;
import com.qvorum.server.security.AuthenticatedUser;
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
    private final ClientRegistry clientRegistry;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ClientRegistry clientRegistry) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.clientRegistry = clientRegistry;
    }

    /** @param address the IP address the request came from, shown in the console's client list */
    public LoginResponse login(LoginRequest request, String address) {
        // The same message for an unknown email and a wrong password, so the response does not
        // reveal which emails have accounts.
        User user = userRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        ClientRegistry.ClientInfo client = clientRegistry.connect(user.getId(), user.getEmail(), address);
        return new LoginResponse(jwtService.generateToken(user, client.id()), UserDto.from(user));
    }

    /** Ends the client session, so the token stops working even before it expires. */
    public void logout(AuthenticatedUser currentUser) {
        clientRegistry.disconnect(currentUser.clientId());
    }

    public UserDto me(AuthenticatedUser currentUser) {
        return userRepository.findById(currentUser.userId())
                .map(UserDto::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "The account no longer exists."));
    }
}
