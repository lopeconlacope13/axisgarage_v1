package org.iesalixar.daw2.alvarolopez.axisgarage.mappers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.CoverageDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Coverage;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.springframework.stereotype.Component;

@Component
public class CoverageMapper {

    public CoverageDTO toDTO(Coverage entity) {
        if (entity == null) return null;

        CoverageDTO dto = new CoverageDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setTotalPrice(entity.getTotalPrice());

        if (entity.getReservation() != null) {
            dto.setReservationId(entity.getReservation().getId());
        }
        return dto;
    }

    public Coverage toEntity(CoverageDTO dto, Reservation reservation) {
        if (dto == null) return null;

        Coverage entity = new Coverage();
        entity.setId(dto.getId());
        entity.setType(dto.getType());
        entity.setTotalPrice(dto.getTotalPrice());
        entity.setReservation(reservation);
        return entity;
    }
}
