package com.parking.gate_control_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.parking.common.entity", "com.parking.client.service.entity"})
@EnableJpaRepositories(basePackages = {"com.parking.client.service.repository"})
public class GateControlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GateControlServiceApplication.class, args);
	}

}
