package com.skillmirror.security;

import com.skillmirror.config.SkillMirrorProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Creates and validates HS256 JWT tokens. The signing key never leaves the backend. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(SkillMirrorProperties properties) {
        this.key = Keys.hmacShaKeyFor(
                properties.getSecurity().getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = properties.getSecurity().getJwt().getExpirationSeconds();
    }

    public String generateToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Parses a raw token and returns the authenticated principal, or null when the token
     * is missing, expired or tampered with.
     */
    public AuthPrincipal parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long uid = claims.get("uid", Number.class).longValue();
            String role = claims.get("role", String.class);
            return new AuthPrincipal(uid, claims.getSubject(), role == null ? "USER" : role);
        } catch (Exception ex) {
            return null;
        }
    }
}
