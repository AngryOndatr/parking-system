package com.parking.client_service.mapper;

import com.parking.client_service.generated.model.ClientRequest;
import com.parking.client_service.generated.model.ClientResponse;
import com.parking.common.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    // Request DTO -> Entity
    Client toEntity(ClientRequest request);

    // Entity -> Response DTO
    ClientResponse toResponse(Client client);

    // Конвертація LocalDateTime -> OffsetDateTime
    default OffsetDateTime map(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}