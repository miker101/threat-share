
package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.OrganizationProfileRequest;
import com.example.demo.dto.OrganizationProfileResponse;
import com.example.demo.service.OrganizationSettingsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/organization/settings")
public class OrganizationSettingsController {

    private final OrganizationSettingsService settingsService;

    public OrganizationSettingsController(
            OrganizationSettingsService settingsService) {

        this.settingsService = settingsService;
    }

    // GET: Retrieve the authenticated organization's profile.
    @GetMapping("/profile")
    public ResponseEntity<OrganizationProfileResponse> getProfile() {

        OrganizationProfileResponse response =
                settingsService.getOrganizationProfile();

        return ResponseEntity.ok(response);
    }

    // PUT: Update the authenticated organization's profile.
    @PutMapping("/profile")
    public ResponseEntity<OrganizationProfileResponse> updateProfile(
            @Valid @RequestBody OrganizationProfileRequest request) {

        OrganizationProfileResponse response =
                settingsService.updateOrganizationProfile(request);

        return ResponseEntity.ok(response);
    }
}
