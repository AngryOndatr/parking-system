package com.parking.api_gateway.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

/**
 * Security Configuration for E2E Tests
 * Completely disables security for testing purposes
 */
@Configuration
@EnableWebSecurity
@Profile("e2e-test")
@Slf4j
public class E2ESecurityConfiguration {

    /**
     * Configure Security Filter Chain for E2E tests
     * Disables all security features
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.warn("⚠️  E2E Test Mode: Security is COMPLETELY DISABLED");
        log.warn("⚠️  This configuration should NEVER be used in production!");

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout -> logout.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        log.info("✓ E2E Test Security Configuration: All requests permitted");

        return http.build();
    }
}

