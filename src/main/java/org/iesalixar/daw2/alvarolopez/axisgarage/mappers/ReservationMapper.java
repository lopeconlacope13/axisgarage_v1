package org.iesalixar.daw2.alvarolopez.axisgarage.mappers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.ReservationDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Vehicle;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Renter;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationDTO toDTO(Reservation entity) {
        if (entity == null)
            return null;

        ReservationDTO dto = new ReservationDTO();
        dto.setId(entity.getId());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setTotalPrice(entity.getTotalPrice());
        dto.setStatus(entity.getStatus());

        // Extraemos datos de la casa (ahora Vehicle)
        if (entity.getVehicle() != null) {
            dto.setVehicleId(entity.getVehicle().getId());
            dto.setVehicleModel(entity.getVehicle().getModel());
        }

        // Extraemos datos del huésped (ahora Renter)
        if (entity.getRenter() != null) {
            dto.setRenterId(entity.getRenter().getId());
            dto.setRenterName(entity.getRenter().getName() + " " + entity.getRenter().getLastName());
        }

        return dto;
    }

    public Reservation toEntity(ReservationDTO dto, Vehicle vehicle, Renter renter) {
        if (dto == null)
            return null;

        Reservation entity = new Reservation();
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setStatus(dto.getStatus());
        // El frontend calcula el total ya con la cobertura incluida; lo mapeamos directamente
        entity.setTotalPrice(dto.getTotalPrice());

        entity.setVehicle(vehicle);
        entity.setRenter(renter);

        return entity;
    }

}
