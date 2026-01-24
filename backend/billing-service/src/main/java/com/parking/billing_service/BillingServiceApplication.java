package com.parking.billing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {"com.parking.common.entity", "com.parking.billing.entity"})
@EnableJpaRepositories(basePackages = {"com.parking.billing.repository", "com.parking.billing_service.repository"})
@ComponentScan(basePackages = {"com.parking.billing", "com.parking.billing_service"})
@SpringBootApplication
public class BillingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingServiceApplication.class, args);
	}

}
