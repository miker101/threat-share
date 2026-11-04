package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ThreatShare;

public interface ThreatShareRepository
        extends JpaRepository<ThreatShare, Long> {

    Optional<ThreatShare> findByEmail(String email);

    boolean existsByEmail(String email);
}