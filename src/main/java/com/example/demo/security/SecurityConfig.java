package com.example.demo.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.repository.ThreatShareRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.DispatcherType;

import com.example.demo.service.AdministratorSessionService;

import com.example.demo.service.MaintenanceService;

import com.example.demo.service.PlatformSettingsService;

@Configuration
public class SecurityConfig {

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

@Bean
public JwtAuthenticationFilter jwtAuthenticationFilter(
        JwtService jwtService,
        ThreatShareRepository adminRepository,
        UserRepository userRepository,
        AdministratorSessionService sessionService,
        MaintenanceService maintenanceService,
        PlatformSettingsService platformSettingsService) {

    return new JwtAuthenticationFilter(
            jwtService,
            adminRepository,
            userRepository,
            sessionService,
            maintenanceService,
            platformSettingsService
    );
}



@Bean
public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthenticationFilter jwtAuthenticationFilter)
        throws Exception {

    http
        .csrf(csrf -> csrf.disable())

        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        
        .exceptionHandling(exception -> exception
        	    .authenticationEntryPoint((request, response, authException) -> {
        	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        	        response.setContentType("application/json");

        	        response.getWriter().write("""
        	            {
        	                "status": 401,
        	                "error": "Unauthorized",
        	                "message": "Authentication is required or the token is invalid."
        	            }
        	            """);
        	    })
        	    .accessDeniedHandler((request, response, accessDeniedException) -> {
        	        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        	        response.setContentType("application/json");

        	        response.getWriter().write("""
        	            {
        	                "status": 403,
        	                "error": "Forbidden",
        	                "message": "You do not have permission to access this resource."
        	            }
        	            """);
        	    })
        	)

        .cors(cors -> cors
            .configurationSource(corsConfigurationSource()))

        .authorizeHttpRequests(auth -> auth

        	    // Allow Spring's internal error dispatches.
        	    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

        	    // Organization registration.
        	    .requestMatchers(
        	        HttpMethod.POST,
        	        "/api/auth/register-organization"
        	    ).permitAll()

            // Platform Admin login.
            .requestMatchers(
                HttpMethod.POST,
                "/api/admin/auth/login"
            ).permitAll()

            // Organization Admin login.
            .requestMatchers(
                HttpMethod.POST,
                "/api/organization/auth/login"
            ).permitAll()
         // Public maintenance status for Organization Admin workspaces.
            .requestMatchers(
                HttpMethod.GET,
                "/api/maintenance/status"
            ).permitAll()

            // Organization-specific protected endpoints.
            .requestMatchers("/api/organization/**")
            .hasRole("ORGANIZATION_ADMIN")

            // All remaining endpoints are Platform Admin only.
            .anyRequest()
            .hasRole("PLATFORM_ADMIN")
        )

        .addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class
        );

    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(
    	    List.of(
    	        "http://127.0.0.1:49394",
    	        "http://127.0.0.1:58990",
    	        "http://127.0.0.1:65132",
    	        "http://127.0.0.1:54167"
    	    )
    	);
    configuration.setAllowedMethods(
        List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        )
    );

    configuration.setAllowedHeaders(
        List.of("Authorization", "Content-Type")
    );

    configuration.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", configuration);

    return source;
}


}
