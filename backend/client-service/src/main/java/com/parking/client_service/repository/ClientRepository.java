package com.parking.client_service.repository;

import com.parking.common.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     */
    Optional<Client> findByPhoneNumber(String phoneNumber);
}