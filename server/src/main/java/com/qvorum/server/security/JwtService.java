package com.qvorum.server.security;

import com.qvorum.server.model.User;
import io.jsonwebtoken.Claims;
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
 * Creates and validates JSON Web Tokens. The token stores the user id, the client session id and
 * an expiry date, and is signed with jwt.secret, so nobody can change it without the server
 * noticing.
 */
@Service
public class JwtService {

    /** Name of the claim that holds the client session id. */
    private static final String CLIENT_ID_CLAIM = "sid";

    /** What a valid token contains. */
    public record TokenContent(Long userId, String clientId) {
    }

    private final SecretKey key;
    private final Duration validity;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.validity-hours}") long validityHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validity = Duration.ofHours(validityHours);
    }

    public String generateToken(User user, String clientId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLIENT_ID_CLAIM, clientId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(validity)))
                .signWith(key)
                .compact();
    }

    /** Reads a token, or throws JwtException if it is invalid, tampered with or expired. */
    public TokenContent parse(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new TokenContent(Long.valueOf(claims.getSubject()), claims.get(CLIENT_ID_CLAIM, String.class));
    }
}
