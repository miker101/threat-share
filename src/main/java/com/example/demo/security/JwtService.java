
package com.example.demo.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${threatshare.jwt.secret}") String secret,
            @Value("${threatshare.jwt.expiration-ms:3600000}")
            long expirationMillis) {

        byte[] keyBytes = Decoders.BASE64.decode(secret);

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expirationMillis;
    }

    // PLATFORM ADMIN TOKEN - EXISTING METHOD
 // PLATFORM ADMIN TOKEN WITH ADMIN ID AND SESSION ID
    public String generateToken(
            String adminEmail,
            Long adminId,
            String sessionId) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + expirationMillis
        );

        return Jwts.builder()
                .subject(adminEmail)
                .claim("role", "PLATFORM_ADMIN")
                .claim("adminId", adminId)
                .claim("sessionId", sessionId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    // PLATFORM ADMIN TOKEN WITH SESSION ID
    public String generateToken(
            String adminEmail,
            String sessionId) {

        return generateToken(
                adminEmail,
                "PLATFORM_ADMIN",
                null,
                null,
                sessionId
        );
    }

    // EXISTING FOUR-ARGUMENT TOKEN METHOD
    public String generateToken(
            String email,
            String role,
            Long userId,
            Long organizationId) {

        return generateToken(
                email,
                role,
                userId,
                organizationId,
                null
        );
    }

    // TOKEN GENERATION WITH SERVER-SIDE SESSION ID
    public String generateToken(
            String email,
            String role,
            Long userId,
            Long organizationId,
            String sessionId) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + expirationMillis
        );

        var builder = Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration);

        if (userId != null) {
            builder.claim("userId", userId);
        }

        if (organizationId != null) {
            builder.claim("organizationId", organizationId);
        }

        if (sessionId != null) {
            builder.claim("sessionId", sessionId);
        }

        return builder
                .signWith(signingKey)
                .compact();
    }

    // EXTRACT EMAIL
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // EXTRACT ROLE
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // EXTRACT USER ID
    public Long extractUserId(String token) {

        Object value = extractAllClaims(token).get("userId");

        return value instanceof Number number
                ? number.longValue()
                : null;
    }
 // EXTRACT PLATFORM ADMIN ID
    public Long extractAdminId(String token) {

        Object value = extractAllClaims(token).get("adminId");

        return value instanceof Number number
                ? number.longValue()
                : null;
    }

    // EXTRACT ORGANIZATION ID
    public Long extractOrganizationId(String token) {

        Object value = extractAllClaims(token).get("organizationId");

        return value instanceof Number number
                ? number.longValue()
                : null;
    }

    // EXTRACT SESSION ID
    public String extractSessionId(String token) {
        return extractAllClaims(token).get("sessionId", String.class);
    }

    // GET CONFIGURED TOKEN EXPIRATION
    public long getExpirationMillis() {
        return expirationMillis;
    }

    // EXTRACT ALL CLAIMS AND VERIFY SIGNATURE/EXPIRATION
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // VALIDATE TOKEN
    public boolean isTokenValid(
            String token,
            String expectedEmail) {

        try {

            String tokenEmail = extractEmail(token);

            return tokenEmail != null
                    && tokenEmail.equalsIgnoreCase(expectedEmail);

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }
}
