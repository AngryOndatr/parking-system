package com.parking.api_gateway.security.config;

import com.parking.api_gateway.security.filter.SecurityFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for API Gateway
 * Configures custom JWT-based authentication and disables Spring Security defaults
 */
@Configuration
@EnableWebSecurity
@EnableScheduling
@EnableTransactionManagement
@RequiredArgsConstructor
@Slf4j
@Profile("!e2e-test")  // Disable this configuration in E2E tests
public class SecurityConfiguration {
    
    private final SecurityFilter securityFilter;

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://192.168.*,null}")
    private String corsAllowedOrigins;

    /**
     * Configure Security Filter Chain
     * Disables default HTTP Basic authentication and configures custom JWT filter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("🔐 Configuring Security Filter Chain for API Gateway");

        http
            // Disable CSRF for stateless API
            .csrf(csrf -> {
                csrf.disable();
                log.info("✓ CSRF protection disabled (stateless API)");
            })

            // Configure CORS
            .cors(cors -> {
                cors.configurationSource(corsConfigurationSource());
                log.info("✓ CORS configured with custom settings");
            })

            // Stateless session management
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                log.info("✓ Session management: STATELESS");
            })

            // Disable HTTP Basic authentication
            .httpBasic(httpBasic -> {
                httpBasic.disable();
                log.info("✓ HTTP Basic authentication DISABLED");
            })

            // Disable form login
            .formLogin(formLogin -> {
                formLogin.disable();
                log.info("✓ Form login DISABLED");
            })

            // Configure authorization rules
            .authorizeHttpRequests(auth -> {
                auth
                    // Public endpoints - no authentication required
                    .requestMatchers(
                        "/api/auth/**",           // Authentication endpoints
                        "/actuator/health",       // Health check
                        "/actuator/info",         // Info endpoint
                        "/actuator/prometheus",   // Prometheus metrics
                        "/v3/api-docs/**",        // OpenAPI docs
                        "/swagger-ui/**",         // Swagger UI
                        "/swagger-ui.html",       // Swagger UI HTML
                        "/error"                  // Error page
                    ).permitAll()

                    // All other requests are handled by our SecurityFilter
                    // which sets authentication in SecurityContext if JWT is valid
                    .anyRequest().permitAll();

                log.info("✓ Authorization rules configured:");
                log.info("  - Public: /api/auth/**, /actuator/**, /v3/api-docs/**, /swagger-ui/**");
                log.info("  - All other requests: Handled by SecurityFilter (JWT validation)");
            })

            // Add custom JWT security filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("✅ Security Filter Chain configured successfully");
        log.info("🔒 Custom SecurityFilter will handle JWT authentication");

        return http.build();
    }

    /**
     * Configure CORS settings.
     * Mirrors CorsFilter.java — specific origins, no credentials (JWT via Authorization header).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> originPatterns = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());

        // setAllowedOriginPatterns supports wildcards like "http://192.168.*"
        // whereas setAllowedOrigins does not
        configuration.setAllowedOriginPatterns(originPatterns);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        // false: JWT is stored in localStorage and sent via Authorization header — no cookies
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("✓ CORS configuration: originPatterns={}, allowCredentials=false", originPatterns);
        return source;
    }

    /**
     * Scheduled cleanup task for expired tokens and cache entries
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredEntries() {
        try {
            log.debug("Running scheduled cleanup of expired security entries");
            // Cleanup logic will be implemented by individual services
        } catch (Exception e) {
            log.error("Error during scheduled cleanup: {}", e.getMessage());
        }
    }
}
