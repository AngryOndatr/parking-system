package com.parking.management_service.repository;

import com.parking.common.entity.ParkingSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ParkingSpace entity
 */
@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {

    /**
     * Find all parking spaces with specific status
     */
    List<ParkingSpace> findByStatus(String status);

    /**
     * Find parking spaces by lot and status
     */
    List<ParkingSpace> findByParkingLotIdAndStatus(Long parkingLotId, String status);

    /**
     * Find parking spaces by type
     */
    List<ParkingSpace> findBySpaceType(String spaceType);

    /**
     * Find parking spaces by type and status
     */
    List<ParkingSpace> findBySpaceTypeAndStatus(String spaceType, String status);

    /**
     * Count parking spaces by status
     */
    long countByStatus(String status);
}

