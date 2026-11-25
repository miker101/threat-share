package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.OrganizationResponse;
import com.example.demo.entity.Organization;
import com.example.demo.repository.OrganizationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminOrganizationService {

    private final OrganizationRepository organizationRepository;

    public AdminOrganizationService(
            OrganizationRepository organizationRepository) {

        this.organizationRepository = organizationRepository;
    }

    // Retrieve all organizations, newest registrations first.
    public List<OrganizationResponse> getAllOrganizations() {

        return organizationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Retrieve organizations by registration status.
    public List<OrganizationResponse> getOrganizationsByStatus(
            String status) {

        return organizationRepository
                .findByStatusOrderByCreatedAtAsc(status.toUpperCase())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Convert the entity into a response DTO.
    private OrganizationResponse mapToResponse(
            Organization organization) {

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getEmail(),
                organization.getStatus(),
                organization.getCreatedAt()
        );
    }
    public OrganizationResponse getOrganizationById(Long id) {

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Organization not found with ID: " + id
                        )
                );

        return mapToResponse(organization);
    }
}