
package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.OrganizationProfileRequest;
import com.example.demo.dto.OrganizationProfileResponse;
import com.example.demo.entity.Organization;
import com.example.demo.repository.OrganizationRepository;
import com.example.demo.security.CurrentUserService;

@Service
public class OrganizationSettingsService {

    private final OrganizationRepository organizationRepository;
    private final CurrentUserService currentUserService;

    public OrganizationSettingsService(
            OrganizationRepository organizationRepository,
            CurrentUserService currentUserService) {

        this.organizationRepository = organizationRepository;
        this.currentUserService = currentUserService;
    }

    // Retrieve the authenticated organization's profile.
    @Transactional(readOnly = true)
    public OrganizationProfileResponse getOrganizationProfile() {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Organization profile could not be found."
                        )
                );

        return mapToResponse(organization);
    }

    // Update only the permitted organization profile fields.
    @Transactional
    public OrganizationProfileResponse updateOrganizationProfile(
            OrganizationProfileRequest request) {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Organization profile could not be found."
                        )
                );

        String newName = request.name().trim();
        String newEmail = request.email().trim().toLowerCase();

        // Prevent another organization from using the same name.
        boolean duplicateName =
                !organization.getName().equalsIgnoreCase(newName)
                && organizationRepository.existsByName(newName);

        if (duplicateName) {
            throw new IllegalArgumentException(
                    "An organization with this name already exists."
            );
        }

        // Prevent another organization from using the same email.
        boolean duplicateEmail =
                !organization.getEmail().equalsIgnoreCase(newEmail)
                && organizationRepository.findByEmail(newEmail)
                        .filter(existing ->
                                !existing.getId().equals(organizationId))
                        .isPresent();

        if (duplicateEmail) {
            throw new IllegalArgumentException(
                    "An organization with this email already exists."
            );
        }

        organization.setName(newName);
        organization.setEmail(newEmail);

        Organization savedOrganization =
                organizationRepository.save(organization);

        return mapToResponse(savedOrganization);
    }

    private OrganizationProfileResponse mapToResponse(
            Organization organization) {

        return new OrganizationProfileResponse(
                organization.getId(),
                organization.getName(),
                organization.getEmail(),
                organization.getStatus(),
                organization.getCreatedAt()
        );
    }
}
