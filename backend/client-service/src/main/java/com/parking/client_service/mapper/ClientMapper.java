package com.parking.client_service.mapper;

import com.parking.client_service.generated.model.ClientRequest;
import com.parking.client_service.generated.model.ClientResponse;
import com.parking.client_service.dto.ClientRequestDto;
import com.parking.common.domain.ClientDomain;
import com.parking.common.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    // Generated Request DTO -> Entity
    Client toEntity(ClientRequest request);

    // Local validated DTO -> Entity
    @Mapping(target = "id", ignore = true)
    Client toEntity(ClientRequestDto requestDto);

    // Entity -> Generated Response DTO
    ClientResponse toResponse(Client client);

    // Domain <-> Entity
    ClientDomain toDomain(Client entity);

    Client toEntityFromDomain(ClientDomain domain);

    // Domain -> Generated Response DTO
    ClientResponse domainToResponse(ClientDomain domain);

    // Generated Request DTO -> Domain
    ClientDomain requestToDomain(ClientRequest request);

    // Конвертація LocalDateTime -> OffsetDateTime
    default OffsetDateTime map(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}