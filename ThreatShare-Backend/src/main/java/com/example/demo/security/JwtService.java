
package com.example.demo.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

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

    public String generateToken(String adminEmail) {

        Date now = new Date();
        Date expiration = new Date(
            now.getTime() + expirationMillis
        );

        return Jwts.builder()
                .subject(adminEmail)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }
 // Extract the admin's email from the token.
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Validate the token and read its claims.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Check whether the token is valid and belongs to this admin.
    public boolean isTokenValid(String token, String expectedEmail) {
        try {
            String tokenEmail = extractEmail(token);
            return tokenEmail.equalsIgnoreCase(expectedEmail);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
