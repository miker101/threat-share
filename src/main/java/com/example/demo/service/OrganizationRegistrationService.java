
package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.OrganizationRegistrationRequest;
import com.example.demo.entity.Organization;
import com.example.demo.entity.User;
import com.example.demo.repository.OrganizationRepository;
import com.example.demo.repository.UserRepository;

import com.example.demo.service.PlatformSettingsService;

@Service
public class OrganizationRegistrationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final PlatformSettingsService platformSettingsService;
    
    private final PlatformNotificationService notificationService;

    public OrganizationRegistrationService(
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PlatformSettingsService platformSettingsService,
            PlatformNotificationService notificationService) {

        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.platformSettingsService = platformSettingsService;
        this.notificationService = notificationService;
    }
    @Transactional
    public Organization register(OrganizationRegistrationRequest request) {

        String organizationName = request.getOrganizationName().trim();
        String organizationEmail =
                request.getOrganizationEmail().trim().toLowerCase();
        String adminEmail =
                request.getAdminEmail().trim().toLowerCase();
        if (!platformSettingsService.getBooleanSetting(
                "organizationRegistrationEnabled")) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Organization registration is currently disabled by the platform administrator."
            );
        }

        if (organizationRepository.existsByName(organizationName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An organization with this name already exists");
        }

        if (organizationRepository.findByEmail(organizationEmail).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This organization email is already registered");
        }

        if (userRepository.existsByEmail(adminEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This administrator email is already registered");
        }

        Organization organization = new Organization();
        organization.setName(organizationName);
        organization.setEmail(organizationEmail);
        boolean approvalRequired =
                platformSettingsService.getBooleanSetting(
                        "organizationApprovalRequired"
                );

        organization.setStatus(approvalRequired ? "PENDING" : "ACTIVE");

        Organization savedOrganization =
                organizationRepository.save(organization);

        User admin = new User();
        admin.setFullName(request.getAdminFullName().trim());
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setOrganization(savedOrganization);
        admin.setStatus(approvalRequired ? "PENDING" : "ACTIVE");

        userRepository.save(admin);
        
        notificationService.createNotification(
                approvalRequired
                        ? "New Organization Registration"
                        : "New Organization Registered",

                approvalRequired
                        ? organizationName
                                + " has submitted a registration application awaiting your review."
                        : organizationName
                                + " has successfully registered and is now active.",

                "registration",
                "ORGANIZATION_REGISTERED",
                approvalRequired ? "WARNING" : "INFO",
                "ORGANIZATION",
                savedOrganization.getId(),
                "/organizations.html"
        );

        return savedOrganization;
    }
    @Transactional(readOnly = true)
    public String getAdministratorStatus(Long organizationId) {

        return userRepository.findByOrganizationId(organizationId)
                .stream()
                .filter(user -> user.getStatus() != null)
                .map(User::getStatus)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No administrator found for organization: "
                                + organizationId
                ));
    }
}
