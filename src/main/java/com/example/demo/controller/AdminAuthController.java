
package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.example.demo.dto.AdminLoginRequest;
import com.example.demo.security.JwtService;
import com.example.demo.service.AdminAuthService;
import com.example.demo.service.AdministratorSessionService;
import com.example.demo.service.AuthenticationActivityService;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final JwtService jwtService;
    private final AdministratorSessionService sessionService;
    private final AuthenticationActivityService activityService;

    public AdminAuthController(
            AdminAuthService adminAuthService,
            JwtService jwtService,
            AdministratorSessionService sessionService,
            AuthenticationActivityService activityService) {

        this.adminAuthService = adminAuthService;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.activityService = activityService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest httpRequest) {

        Map<String, String> result = adminAuthService.login(
                request.getEmail(),
                request.getPassword(),
                httpRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request) {

        String authorization = request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication token is missing."));
        }

        String token = authorization.substring(7);

        String email;
        String role;
        String sessionId;
        Long adminId;

        try {
        	email = jwtService.extractEmail(token);
        	role = jwtService.extractRole(token);
        	sessionId = jwtService.extractSessionId(token);
        	adminId = jwtService.extractAdminId(token);;
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired authentication token."));
        }

        if (!"PLATFORM_ADMIN".equals(role)
                || adminId == null
                || sessionId == null
                || sessionId.isBlank())  {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid administrator session."));
        }

        boolean revoked = sessionService.revokeSession(
                sessionId,
                "LOGOUT"
        );

        if (!revoked) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "This session is no longer active."));
        }

        activityService.recordEvent(
                "PLATFORM_ADMIN",
                adminId, // Actual platform administrator ID
                null,
                email,
                sessionId,
                "LOGOUT",
                "SUCCESS",
                "INFO",
                "Platform administrator logged out successfully.",
                request
        );

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully.")
        );
    }
}
