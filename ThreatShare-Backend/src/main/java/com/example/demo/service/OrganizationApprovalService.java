
package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Organization;
import com.example.demo.entity.User;
import com.example.demo.repository.OrganizationRepository;
import com.example.demo.repository.UserRepository;

@Service
public class OrganizationApprovalService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public OrganizationApprovalService(
            OrganizationRepository organizationRepository,
            UserRepository userRepository) {

        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, Object> approveOrganization(Long organizationId) {

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Organization not found"));

        if (!"PENDING".equals(organization.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending organizations can be approved");
        }

        List<User> users =
                userRepository.findByOrganizationId(organizationId);

        List<User> pendingAdmins = users.stream()
                .filter(user -> "PENDING".equals(user.getStatus()))
                .toList();

        if (pendingAdmins.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No pending administrator found for this organization");
        }

        // Activate the organization.
        organization.setStatus("ACTIVE");
        organizationRepository.save(organization);

        // Activate its pending administrator account(s).
        pendingAdmins.forEach(user -> user.setStatus("ACTIVE"));
        userRepository.saveAll(pendingAdmins);

        return Map.of(
                "message", "Organization approved successfully",
                "organizationId", organization.getId(),
                "organizationStatus", organization.getStatus(),
                "activatedAdministrators", pendingAdmins.size()
        );
    }

    @Transactional
    public Map<String, Object> rejectOrganization(Long organizationId) {

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Organization not found"));

        if (!"PENDING".equals(organization.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending organizations can be rejected");
        }

        List<User> users =
                userRepository.findByOrganizationId(organizationId);

        List<User> pendingAdmins = users.stream()
                .filter(user -> "PENDING".equals(user.getStatus()))
                .toList();

        if (pendingAdmins.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No pending administrator found for this organization");
        }

        // Reject the organization and its pending administrator accounts.
        organization.setStatus("REJECTED");
        organizationRepository.save(organization);

        pendingAdmins.forEach(user -> user.setStatus("REJECTED"));
        userRepository.saveAll(pendingAdmins);

        return Map.of(
                "message", "Organization rejected successfully",
                "organizationId", organization.getId(),
                "organizationStatus", organization.getStatus(),
                "rejectedAdministrators", pendingAdmins.size()
        );
    }
}
