package com.qvorum.server.service;

import com.qvorum.server.dto.LoginRequest;
import com.qvorum.server.dto.LoginResponse;
import com.qvorum.server.dto.UtilizatorDto;
import com.qvorum.server.exception.ApiException;
import com.qvorum.server.model.Utilizator;
import com.qvorum.server.repository.UtilizatorRepository;
import com.qvorum.server.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * service: business logic and rules. Uses repositories for data and returns DTOs to controllers.
 */
@Service
public class AuthService {

    private final UtilizatorRepository utilizatorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UtilizatorRepository utilizatorRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.utilizatorRepository = utilizatorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        // The same message for an unknown email and a wrong password, so the response does not
        // reveal which emails have accounts.
        Utilizator utilizator = utilizatorRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.parola(), u.getParolaHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email sau parolă greșită."));

        return new LoginResponse(jwtService.generateToken(utilizator), UtilizatorDto.from(utilizator));
    }
}
