package com.parking.client_service.repository;

import com.parking.common.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByPhoneNumber(String phoneNumber);

    Optional<Client> findByEmail(String email);

    /** Case-insensitive substring search on full name. */
    List<Client> findByFullNameContainingIgnoreCase(String name);
}

