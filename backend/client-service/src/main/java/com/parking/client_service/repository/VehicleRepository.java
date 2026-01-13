package com.parking.client_service.repository;

import com.parking.common.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Find vehicle by license plate (unique constraint).
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    /**
     * Find all vehicles belonging to a specific client.
     */
    List<Vehicle> findByClientId(Long clientId);
}

