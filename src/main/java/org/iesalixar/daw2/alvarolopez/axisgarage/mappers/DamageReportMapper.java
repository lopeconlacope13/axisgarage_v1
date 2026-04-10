package org.iesalixar.daw2.alvarolopez.axisgarage.mappers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.DamageReportDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.DamageReport;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.springframework.stereotype.Component;

@Component
public class DamageReportMapper {

    public DamageReportDTO toDTO(DamageReport entity) {
        if (entity == null) return null;

        DamageReportDTO dto = new DamageReportDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setReportedDate(entity.getReportedDate());
        dto.setImageUrl(entity.getImageUrl());

        if (entity.getReservation() != null) {
            dto.setReservationId(entity.getReservation().getId());
        }
        return dto;
    }

    public DamageReport toEntity(DamageReportDTO dto, Reservation reservation) {
        if (dto == null) return null;

        DamageReport entity = new DamageReport();
        entity.setId(dto.getId());
        entity.setType(dto.getType());
        entity.setDescription(dto.getDescription());
        entity.setReportedDate(dto.getReportedDate());
        entity.setImageUrl(dto.getImageUrl());
        entity.setReservation(reservation);
        return entity;
    }
}
