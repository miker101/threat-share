
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

@Service
public class OrganizationRegistrationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OrganizationRegistrationService(
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Organization register(OrganizationRegistrationRequest request) {

        String organizationName = request.getOrganizationName().trim();
        String organizationEmail =
                request.getOrganizationEmail().trim().toLowerCase();
        String adminEmail =
                request.getAdminEmail().trim().toLowerCase();

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
        organization.setStatus("PENDING");

        Organization savedOrganization =
                organizationRepository.save(organization);

        User admin = new User();
        admin.setFullName(request.getAdminFullName().trim());
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setOrganization(savedOrganization);
        admin.setStatus("PENDING");

        userRepository.save(admin);

        return savedOrganization;
    }
}
