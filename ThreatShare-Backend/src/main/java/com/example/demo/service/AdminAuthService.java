
package com.example.demo.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.ThreatShare;
import com.example.demo.repository.ThreatShareRepository;
import com.example.demo.security.JwtService;

@Service
public class AdminAuthService {

    private final ThreatShareRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(
            ThreatShareRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Map<String, String> login(String email, String password) {

        ThreatShare admin = adminRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        if (!admin.isActive()
                || !passwordEncoder.matches(
                        password, admin.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        String token = jwtService.generateToken(admin.getEmail());

        return Map.of(
                "message", "Login successful",
                "token", token,
                "tokenType", "Bearer",
                "expiresIn", "3600"
        );
    }
}
