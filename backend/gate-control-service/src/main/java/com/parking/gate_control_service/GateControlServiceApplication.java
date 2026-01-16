package com.parking.gate_control_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.parking.gate_control_service", "com.parking.common.entity"})
@EnableJpaRepositories(basePackages = {"com.parking.gate_control_service.repository"})
public class GateControlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GateControlServiceApplication.class, args);
	}

}
