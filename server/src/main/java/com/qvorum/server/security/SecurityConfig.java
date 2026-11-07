package com.qvorum.server.security;

import com.qvorum.server.monitoring.ClientRegistry;
import com.qvorum.server.monitoring.RequestLogService;
import com.qvorum.server.monitoring.RequestLoggingFilter;
import com.qvorum.server.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * security: authentication and authorization (this configuration, JWT handling, password hashing).
 *
 * The API is stateless: no sessions or cookies, every request carries its JWT instead.
 *
 * Access rules:
 * - POST /auth/login is open to everyone;
 * - every other endpoint needs a valid token (401 otherwise);
 * - endpoints for one role are marked in the controller with @PreAuthorize, for example
 *   {@code @PreAuthorize("hasRole('ADMINISTRATOR')")} (403 when the role is missing).
 *   @EnableMethodSecurity turns these annotations on.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            UserRepository userRepository,
            ClientRegistry clientRegistry,
            RequestLogService requestLogService) throws Exception {

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userRepository, clientRegistry);
        RequestLoggingFilter loggingFilter = new RequestLoggingFilter(requestLogService, clientRegistry);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        // Spring forwards errors to /error; without this, every error would become a 401.
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                // Same JSON error format as GlobalExceptionHandler, so the apps can show "message".
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, e) ->
                                writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Please log in to continue."))
                        .accessDeniedHandler((request, response, e) ->
                                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "You are not allowed to do this.")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(loggingFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
