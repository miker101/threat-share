
package com.example.demo.service;

import com.example.demo.dto.ThreatShareAdminSetupRequest;
import com.example.demo.entity.ThreatShare;
import com.example.demo.repository.ThreatShareRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ThreatShareAdminSetupService {

    private final ThreatShareRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ThreatShareAdminSetupService(
            ThreatShareRepository repository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void setup(ThreatShareAdminSetupRequest request) {

        if (repository.count() > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Platform administrator setup has already been completed");
        }

        ThreatShare admin = new ThreatShare();
        admin.setFullName(request.getFullName().trim());
        admin.setEmail(request.getEmail().trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setActive(true);

        repository.save(admin);
    }
}
