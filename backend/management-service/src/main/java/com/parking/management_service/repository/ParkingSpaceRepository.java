package com.parking.management_service.repository;

import com.parking.common.entity.ParkingSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    
    List<ParkingSpace> findByParkingLotId(Long parkingLotId);
    
    List<ParkingSpace> findByStatus(String status);
    
    @Query("SELECT ps FROM ParkingSpace ps WHERE ps.parkingLot.id = :lotId AND ps.status = :status")
    List<ParkingSpace> findByParkingLotIdAndStatus(
        @Param("lotId") Long parkingLotId, 
        @Param("status") String status
    );
    
    @Query("SELECT COUNT(ps) FROM ParkingSpace ps WHERE ps.parkingLot.id = :lotId AND ps.status = 'AVAILABLE'")
    Long countAvailableSpacesByParkingLotId(@Param("lotId") Long parkingLotId);
}
