
package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.OrganizationLoginRequest;
import com.example.demo.dto.OrganizationLoginResponse;
import com.example.demo.entity.User;
import com.example.demo.service.OrganizationAuthService;
import com.example.demo.security.JwtService;
import com.example.demo.service.AdministratorSessionService;
import com.example.demo.service.AuthenticationActivityService;
import com.example.demo.service.MaintenanceService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/organization/auth")
public class OrganizationAuthController {

    private final OrganizationAuthService authService;
    private final JwtService jwtService;
    private final AdministratorSessionService sessionService;
    private final MaintenanceService maintenanceService;
    private final AuthenticationActivityService activityService;

    public OrganizationAuthController(
            OrganizationAuthService authService,
            JwtService jwtService,
            AdministratorSessionService sessionService,
            MaintenanceService maintenanceService,
            AuthenticationActivityService activityService) {

        this.authService = authService;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.maintenanceService = maintenanceService;
        this.activityService = activityService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody OrganizationLoginRequest request,
            HttpServletRequest httpRequest) {

        // Check whether maintenance mode is active.
        Map<String, Object> maintenanceStatus =
                maintenanceService.getMaintenanceStatus();

        boolean maintenanceEnabled =
                Boolean.TRUE.equals(
                        maintenanceStatus.get("maintenanceEnabled")
                );

        // Block new Organization Admin logins during maintenance.
        if (maintenanceEnabled) {

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", 503,
                            "error", "Service Unavailable",
                            "title", "ThreatShare is undergoing maintenance",
                            "message", "We sincerely apologize for the inconvenience. "
                                    + "Our platform is temporarily unavailable while "
                                    + "we carry out maintenance and improvements. "
                                    + "We are working to restore services as soon "
                                    + "as possible. Please check back shortly. "
                                    + "Thank you for your patience."
                    ));
        }

        // Authenticate the organization administrator.
        User user = authService.authenticate(request, httpRequest);

        // Create an administrator session.
        var session = sessionService.createSession(
                "ORGANIZATION_ADMIN",
                user.getId(),
                user.getOrganization().getId(),
                user.getEmail(),
                jwtService.getExpirationMillis()
        );

        // Generate an authentication token.
        String token = jwtService.generateToken(
                user.getEmail(),
                "ORGANIZATION_ADMIN",
                user.getId(),
                user.getOrganization().getId(),
                session.getSessionId()
        );

        // Record successful login with its server-side session ID.
        activityService.recordEvent(
                "ORGANIZATION_ADMIN",
                user.getId(),
                user.getOrganization().getId(),
                user.getEmail(),
                session.getSessionId(),
                "LOGIN",
                "SUCCESS",
                "INFO",
                "Organization administrator logged in successfully",
                httpRequest
        );

        // Prepare the authentication response.
        OrganizationLoginResponse response =
                new OrganizationLoginResponse(
                        "Login successful.",
                        token,
                        "Bearer",
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getOrganization().getId(),
                        user.getOrganization().getName(),
                        "ORGANIZATION_ADMIN"
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request) {

        String authorization =
                request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Authentication token is missing."
                    ));
        }

        String token = authorization.substring(7);

        String email;
        String role;
        Long userId;
        Long organizationId;
        String sessionId;

        try {
            email = jwtService.extractEmail(token);
            role = jwtService.extractRole(token);
            userId = jwtService.extractUserId(token);
            organizationId =
                    jwtService.extractOrganizationId(token);
            sessionId = jwtService.extractSessionId(token);

        } catch (Exception exception) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid or expired authentication token."
                    ));
        }

        if (!"ORGANIZATION_ADMIN".equals(role)
                || userId == null
                || organizationId == null
                || sessionId == null
                || sessionId.isBlank()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid organization administrator session."
                    ));
        }

        boolean revoked = sessionService.revokeSession(
                sessionId,
                "LOGOUT"
        );

        if (!revoked) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "This session is no longer active."
                    ));
        }

        activityService.recordEvent(
                "ORGANIZATION_ADMIN",
                userId,
                organizationId,
                email,
                sessionId,
                "LOGOUT",
                "SUCCESS",
                "INFO",
                "Organization administrator logged out successfully.",
                request
        );

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully.")
        );
    }

}
