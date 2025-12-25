package com.parking.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * API Gateway Application - Main Entry Point
 *
 * Excludes default Spring Security auto-configuration to use custom security setup:
 * - SecurityAutoConfiguration: Disables default security setup
 * - UserDetailsServiceAutoConfiguration: Disables auto-generated password
 *
 * Custom security is configured in SecurityConfiguration.java
 */
@SpringBootApplication(exclude = {
	SecurityAutoConfiguration.class,
	UserDetailsServiceAutoConfiguration.class
})
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.parking.api_gateway")
@EnableJpaRepositories(basePackages = "com.parking.api_gateway.security.repository")
@EntityScan(basePackages = "com.parking.api_gateway.security.entity")
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}
