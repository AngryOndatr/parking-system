package com.parking.reporting_service.repository;

import com.parking.common.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    
    List<Log> findByLogLevel(String logLevel);
    
    List<Log> findByUserId(Long userId);
    
    @Query("SELECT l FROM Log l WHERE l.timestamp >= :startTime AND l.timestamp <= :endTime")
    List<Log> findByTimestampBetween(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT l FROM Log l WHERE l.logLevel = :level AND l.timestamp >= :startTime")
    List<Log> findByLogLevelAndTimestampAfter(
        @Param("level") String level,
        @Param("startTime") LocalDateTime startTime
    );
}
