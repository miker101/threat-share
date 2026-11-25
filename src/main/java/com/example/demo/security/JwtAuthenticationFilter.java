
package com.example.demo.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.jsonwebtoken.JwtException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.entity.ThreatShare;
import com.example.demo.entity.User;
import com.example.demo.repository.ThreatShareRepository;
import com.example.demo.repository.UserRepository;

import com.example.demo.service.AdministratorSessionService;

import com.example.demo.service.MaintenanceService;
import com.example.demo.service.PlatformSettingsService;

import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ThreatShareRepository adminRepository;
    private final UserRepository userRepository;
    private final AdministratorSessionService sessionService;
    private final MaintenanceService maintenanceService;
    private final PlatformSettingsService platformSettingsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            ThreatShareRepository adminRepository,
            UserRepository userRepository,
            AdministratorSessionService sessionService,
            MaintenanceService maintenanceService,
            PlatformSettingsService platformSettingsService) {

        this.jwtService = jwtService;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.maintenanceService = maintenanceService;
        this.platformSettingsService = platformSettingsService;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {

            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);
            String sessionId = jwtService.extractSessionId(token);

         // Every authenticated token must have an active server-side session.
         // Validate the JWT before trusting its role.
            if (email == null || role == null
                    || SecurityContextHolder.getContext()
                            .getAuthentication() != null
                    || !jwtService.isTokenValid(token, email)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Block Organization Admin access during maintenance.
            // Platform Admin access remains unaffected.
            if ("ORGANIZATION_ADMIN".equals(role)) {

                Map<String, Object> maintenanceStatus =
                        maintenanceService.getMaintenanceStatus();

                boolean maintenanceEnabled =
                        Boolean.TRUE.equals(
                                maintenanceStatus.get("maintenanceEnabled")
                        );

                if (maintenanceEnabled) {

                    SecurityContextHolder.clearContext();

                    response.setStatus(
                            HttpServletResponse.SC_SERVICE_UNAVAILABLE
                    );

                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    response.getWriter().write("""
                        {
                            "status": 503,
                            "error": "Service Unavailable",
                            "title": "ThreatShare is undergoing maintenance",
                            "message": "We sincerely apologize for the inconvenience. Our platform is temporarily unavailable while we carry out maintenance and improvements. We are working to restore services as soon as possible. Please check back shortly. Thank you for your patience."
                        }
                        """);

                    return;
                }
            }

            // Every authenticated token must have an active server-side session.
            if (sessionId == null
                    || !sessionService.isSessionActive(sessionId)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            if ("PLATFORM_ADMIN".equals(role)) {

                ThreatShare admin = adminRepository
                        .findByEmail(email)
                        .filter(ThreatShare::isActive)
                        .orElse(null);

                if (admin != null) {

                    PlatformAdminPrincipal principal =
                            new PlatformAdminPrincipal(
                                    admin.getId(),
                                    admin.getEmail()
                            );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    List.of(new SimpleGrantedAuthority(
                                            "ROLE_PLATFORM_ADMIN"))
                            );
                    SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

                    
                }

            } else if ("ORGANIZATION_ADMIN".equals(role)) {

                Long tokenUserId =
                        jwtService.extractUserId(token);

                Long tokenOrganizationId =
                        jwtService.extractOrganizationId(token);

                User user = userRepository
                        .findByEmailWithOrganization(email)
                        .orElse(null);

                if (user != null
                        && tokenUserId != null
                        && tokenOrganizationId != null
                        && user.getOrganization() != null
                        && user.getId().equals(tokenUserId)
                        && user.getOrganization().getId()
                                .equals(tokenOrganizationId)
                        && "ACTIVE".equalsIgnoreCase(user.getStatus())
                        && "ACTIVE".equalsIgnoreCase(
                                user.getOrganization().getStatus())) {

                    OrganizationAdminPrincipal principal =
                            new OrganizationAdminPrincipal(
                                    user.getId(),
                                    user.getEmail(),
                                    user.getOrganization().getId()
                            );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    List.of(new SimpleGrantedAuthority(
                                            "ROLE_ORGANIZATION_ADMIN"))
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
            // Update activity only after successful administrator authentication.
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                sessionService.updateLastActivity(sessionId);
            }

        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT AUTHENTICATION ERROR: "
                    + e.getClass().getSimpleName()
                    + " - " + e.getMessage());

            e.printStackTrace();

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
