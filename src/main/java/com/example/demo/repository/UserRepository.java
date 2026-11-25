package com.example.demo.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

public interface UserRepository
        extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
	Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByOrganizationId(Long organizationId);
    
    List<User> findByOrganizationId(Long organizationId);
    
    @Query("""
    	    SELECT u
    	    FROM User u
    	    JOIN FETCH u.organization
    	    WHERE u.email = :email
    	""")
    	Optional<User> findByEmailWithOrganization(
    	        @Param("email") String email
    	);
    
    long countByStatus(String status);
}