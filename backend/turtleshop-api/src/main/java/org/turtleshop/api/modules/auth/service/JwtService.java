package org.turtleshop.api.modules.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${app.auth.jwt-secret}") String secret) {
        // Ensure your secret in application.properties is at least 32 characters!
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Generate JWT with roles and permissions
    public String generateToken(String email, List<String> roles, List<String> permissions) {
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key)
                .compact();
    }

    // Get all data from the token
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Get email from token
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
}