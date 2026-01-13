package com.parking.management_service.mapper;

import com.parking.common.entity.ParkingSpace;
import com.parking.management_service.domain.ParkingSpaceDomain;
import com.parking.management_service.generated.model.ParkingSpaceResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between ParkingSpaceDomain and ParkingSpaceResponse
 */
@Component
public class ParkingSpaceMapper {

    /**
     * Convert domain model to response DTO
     */
    public ParkingSpaceResponse toResponse(ParkingSpaceDomain domain) {
        ParkingSpace entity = domain.getEntity();

        ParkingSpaceResponse response = new ParkingSpaceResponse();

        // Required fields
        response.spaceId(entity.getId());
        response.lotId(entity.getParkingLotId());
        response.spaceNumber(entity.getSpaceNumber());
        response.level(entity.getFloorLevel());
        response.type(ParkingSpaceResponse.TypeEnum.fromValue(entity.getSpaceType()));
        response.status(ParkingSpaceResponse.StatusEnum.fromValue(entity.getStatus()));
        response.hasCharger(entity.getHasCharger() != null ? entity.getHasCharger() : false);

        // Optional fields - pass values directly, setters will wrap in JsonNullable
        if (entity.getSection() != null) {
            response.section(entity.getSection());
        }

        if (entity.getChargerType() != null) {
            response.chargerType(entity.getChargerType());
        }

        if (entity.getLengthCm() != null) {
            response.lengthCm(entity.getLengthCm());
        }

        if (entity.getWidthCm() != null) {
            response.widthCm(entity.getWidthCm());
        }

        // Convert BigDecimal to Double
        if (entity.getHourlyRateOverride() != null) {
            response.hourlyRateOverride(entity.getHourlyRateOverride().doubleValue());
        }

        if (entity.getDailyRateOverride() != null) {
            response.dailyRateOverride(entity.getDailyRateOverride().doubleValue());
        }

        return response;
    }
}

