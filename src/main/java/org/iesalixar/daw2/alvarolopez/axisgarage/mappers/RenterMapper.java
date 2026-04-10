package org.iesalixar.daw2.alvarolopez.axisgarage.mappers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.RenterDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Renter;
import org.springframework.stereotype.Component;

@Component
public class RenterMapper {

    // Convierte de Entidad (BD) a DTO (Postman)
    public RenterDTO toDTO(Renter entity) {
        if (entity == null) {
            return null;
        }

        RenterDTO dto = new RenterDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setDni(entity.getDni());
        dto.setPhone(entity.getPhone());

        return dto;
    }

    // Convierte de DTO (Postman) a Entidad (BD)
    public Renter toEntity(RenterDTO dto) {
        if (dto == null) {
            return null;
        }

        Renter entity = new Renter();
        // El ID no se suele mapear al crear (lo genera la BD)
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setName(dto.getName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setDni(dto.getDni());
        entity.setPhone(dto.getPhone());

        return entity;
    }

}
