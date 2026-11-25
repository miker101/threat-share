
package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.OrganizationLoginRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class OrganizationAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationActivityService activityService;

    public OrganizationAuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationActivityService activityService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityService = activityService;
    }

    @Transactional
    public User authenticate(
            OrganizationLoginRequest request,
            HttpServletRequest httpRequest) {

        String email = request.email() == null
                ? "UNKNOWN"
                : request.email().trim().toLowerCase();

        // Find the organization administrator.
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {

            activityService.recordEvent(
                    "ORGANIZATION_ADMIN",
                    null,
                    null,
                    email,
                    null,
                    "FAILED_LOGIN",
                    "FAILURE",
                    "MEDIUM",
                    "Login failed: administrator account not found",
                    httpRequest
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password."
            );
        }

        // Verify the submitted password.
        if (request.password() == null
                || !passwordEncoder.matches(
                        request.password(),
                        user.getPassword())) {

            activityService.recordEvent(
                    "ORGANIZATION_ADMIN",
                    user.getId(),
                    user.getOrganization() == null
                            ? null
                            : user.getOrganization().getId(),
                    user.getEmail(),
                    null,
                    "FAILED_LOGIN",
                    "FAILURE",
                    "MEDIUM",
                    "Login failed: incorrect password",
                    httpRequest
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password."
            );
        }

        // Verify administrator account status.
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {

            activityService.recordEvent(
                    "ORGANIZATION_ADMIN",
                    user.getId(),
                    user.getOrganization() == null
                            ? null
                            : user.getOrganization().getId(),
                    user.getEmail(),
                    null,
                    "FAILED_LOGIN",
                    "FAILURE",
                    "HIGH",
                    "Login failed: administrator account is inactive",
                    httpRequest
            );

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your administrator account is not active."
            );
        }

        // Verify organization status.
        if (user.getOrganization() == null
                || !"ACTIVE".equalsIgnoreCase(
                        user.getOrganization().getStatus())) {

            activityService.recordEvent(
                    "ORGANIZATION_ADMIN",
                    user.getId(),
                    user.getOrganization() == null
                            ? null
                            : user.getOrganization().getId(),
                    user.getEmail(),
                    null,
                    "FAILED_LOGIN",
                    "FAILURE",
                    "HIGH",
                    "Login failed: organization is not active",
                    httpRequest
            );

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your organization is not active."
            );
        }

        return user;
    }
}
