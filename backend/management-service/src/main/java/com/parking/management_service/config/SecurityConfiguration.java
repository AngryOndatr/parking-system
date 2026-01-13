package com.parking.management_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Management Service
 *
 * All endpoints are public - security is handled by API Gateway
 * Management Service provides public information about parking availability
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf.disable())

            // Allow all requests - security is handled by API Gateway
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )

            // Disable form login
            .formLogin(form -> form.disable())

            // Disable HTTP Basic
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}

