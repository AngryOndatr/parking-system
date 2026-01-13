package com.parking.management_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.parking.common.entity", "com.parking.management_service.entity"})
@EnableJpaRepositories(basePackages = "com.parking.management_service.repository")
public class ManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManagementServiceApplication.class, args);
	}

}
