
package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.PendingOrganizationResponse;
import com.example.demo.entity.Organization;
import com.example.demo.entity.User;
import com.example.demo.repository.OrganizationRepository;
import com.example.demo.repository.UserRepository;

@Service
public class PendingOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public PendingOrganizationService(
            OrganizationRepository organizationRepository,
            UserRepository userRepository) {

        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PendingOrganizationResponse> getPendingOrganizations() {

        List<Organization> organizations =
                organizationRepository
                        .findByStatusOrderByCreatedAtAsc("PENDING");

        return organizations.stream()
                .map(organization -> {

                    List<User> users =
                            userRepository.findByOrganizationId(
                                    organization.getId());

                    // Find the proposed administrator who is still pending.
                    User admin = users.stream()
                            .filter(user ->
                                    "PENDING".equals(user.getStatus()))
                            .findFirst()
                            .orElse(null);

                    // Skip organizations that have no pending administrator.
                    if (admin == null) {
                        return null;
                    }

                    return new PendingOrganizationResponse(
                            organization.getId(),
                            organization.getName(),
                            organization.getEmail(),
                            organization.getStatus(),
                            admin.getFullName(),
                            admin.getEmail(),
                            admin.getStatus(),
                            organization.getCreatedAt()
                    );
                })
                .filter(response -> response != null)
                .toList();
    }
}
