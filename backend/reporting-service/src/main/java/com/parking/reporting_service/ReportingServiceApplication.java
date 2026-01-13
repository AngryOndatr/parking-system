package com.parking.reporting_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.parking.common.entity"})
@EnableJpaRepositories(basePackages = {"com.parking.reporting_service.repository"})
public class ReportingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportingServiceApplication.class, args);
	}

}
