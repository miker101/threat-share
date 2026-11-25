package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.SharedIntelligence;

@Repository
public interface SharedIntelligenceRepository
        extends JpaRepository<SharedIntelligence, Long> {
}