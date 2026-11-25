package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.CryptoKeyRegistrationResponse;
import com.example.demo.service.OrganizationSigningKeyService;

@RestController
@RequestMapping("/api/organization/cryptographic-keys")
public class OrganizationCryptoKeyController {

    private final OrganizationSigningKeyService signingKeyService;

    public OrganizationCryptoKeyController(
            OrganizationSigningKeyService signingKeyService) {
        this.signingKeyService = signingKeyService;
    }

    @PostMapping("/signing/register")
    public ResponseEntity<CryptoKeyRegistrationResponse> registerSigningKey()
            throws Exception {

        CryptoKeyRegistrationResponse response =
                signingKeyService.registerSigningKey();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}