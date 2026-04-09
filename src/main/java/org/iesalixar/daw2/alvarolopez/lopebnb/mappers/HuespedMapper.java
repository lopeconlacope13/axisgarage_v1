package org.iesalixar.daw2.alvarolopez.lopebnb.mappers;

import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.HuespedDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.OpinionDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.ReservaDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Huesped;
import org.springframework.stereotype.Component;

@Component

public class HuespedMapper {

    // Convierte de Entidad (BD) a DTO (Postman)
    public HuespedDTO toDTO(Huesped entity) {
        if (entity == null) {
            return null;
        }

        HuespedDTO dto = new HuespedDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setApellidos(entity.getApellidos());
        dto.setEmail(entity.getEmail());
        dto.setDni(entity.getDni());
        dto.setTelefono(entity.getTelefono());

        return dto;
    }

    // Convierte de DTO (Postman) a Entidad (BD)
    public Huesped toEntity(HuespedDTO dto) {
        if (dto == null) {
            return null;
        }

        Huesped entity = new Huesped();
        // El ID no se suele mapear al crear (lo genera la BD)
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setNombre(dto.getNombre());
        entity.setApellidos(dto.getApellidos());
        entity.setEmail(dto.getEmail());
        entity.setDni(dto.getDni());
        entity.setTelefono(dto.getTelefono());

        return entity;
    }

}