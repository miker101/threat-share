
package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.OrganizationRegistrationRequest;
import com.example.demo.dto.OrganizationRegistrationResponse;
import com.example.demo.entity.Organization;
import com.example.demo.service.OrganizationRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class OrganizationRegistrationController {

    private final OrganizationRegistrationService registrationService;

    public OrganizationRegistrationController(
            OrganizationRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register-organization")
    public ResponseEntity<OrganizationRegistrationResponse> register(
            @Valid @RequestBody OrganizationRegistrationRequest request) {

        Organization organization =
                registrationService.register(request);

        OrganizationRegistrationResponse response =
                new OrganizationRegistrationResponse(
                        "Organization registration submitted successfully. Awaiting approval.",
                        organization.getId(),
                        organization.getStatus(),
                        "PENDING"
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
