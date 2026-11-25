
package com.example.demo.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.ThreatShare;
import com.example.demo.repository.ThreatShareRepository;
import com.example.demo.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AdminAuthService {

    private final ThreatShareRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AdministratorSessionService sessionService;
    private final AuthenticationActivityService activityService;

    public AdminAuthService(
            ThreatShareRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AdministratorSessionService sessionService,
            AuthenticationActivityService activityService) {

        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.activityService = activityService;
    }

    public Map<String, String> login(
            String email,
            String password,
            HttpServletRequest request) {

        String normalizedEmail = email == null
                ? "UNKNOWN"
                : email.trim().toLowerCase();

        ThreatShare admin = adminRepository
                .findByEmail(normalizedEmail)
                .orElse(null);

        // Unknown administrator account
        if (admin == null) {

            activityService.recordEvent(
                    "PLATFORM_ADMIN",
                    null,
                    null,
                    normalizedEmail,
                    null,
                    "FAILED_LOGIN",
                    "FAILURE",
                    "MEDIUM",
                    "Login failed: administrator account not found",
                    request
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        // Inactive account or incorrect password
        if (!admin.isActive()
                || password == null
                || !passwordEncoder.matches(
                        password, admin.getPassword())) {

            activityService.recordEvent(
                    "PLATFORM_ADMIN",
                    admin.getId(),
                    null,
                    admin.getEmail(),
                    null,
                    "FAILED_LOGIN",
                    "FAILURE",
                    "MEDIUM",
                    !admin.isActive()
                            ? "Login failed: administrator account is inactive"
                            : "Login failed: incorrect password",
                    request
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        // Create the administrator's server-side session
        var session = sessionService.createSession(
                "PLATFORM_ADMIN",
                admin.getId(),
                null,
                admin.getEmail(),
                jwtService.getExpirationMillis()
        );

        String token = jwtService.generateToken(
                admin.getEmail(),
                admin.getId(),
                session.getSessionId()
        );

        // Record successful login
        activityService.recordEvent(
                "PLATFORM_ADMIN",
                admin.getId(),
                null,
                admin.getEmail(),
                session.getSessionId(),
                "LOGIN",
                "SUCCESS",
                "INFO",
                "Platform administrator logged in successfully",
                request
        );

        return Map.of(
                "message", "Login successful",
                "token", token,
                "tokenType", "Bearer",
                "expiresIn", "3600"
        );
    }
}
