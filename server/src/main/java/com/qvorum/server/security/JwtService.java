package com.qvorum.server.security;

import com.qvorum.server.model.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Creates and validates JSON Web Tokens. The token stores the user id and an expiry date and is
 * signed with jwt.secret, so the server can trust it without keeping sessions in memory.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration validity;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.validity-hours}") long validityHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validity = Duration.ofHours(validityHours);
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(validity)))
                .signWith(key)
                .compact();
    }

    /** Returns the user id stored in the token, or throws JwtException if it is invalid or expired. */
    public Long extractUserId(String token) throws JwtException {
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(subject);
    }
}
