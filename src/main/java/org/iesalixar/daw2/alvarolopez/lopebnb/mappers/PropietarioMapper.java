package org.iesalixar.daw2.alvarolopez.lopebnb.mappers;

import jakarta.validation.constraints.NotNull;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.PropietarioDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
import org.springframework.stereotype.Component;

@Component
public class PropietarioMapper {

    // --- MÉTODOS PRIVADOS PARA MAPEAR (De Entity a DTO y viceversa) ---
    public PropietarioDTO toDTO(Propietario propietario) {
        if (propietario == null) return null; // Escudo anti-nulos

        PropietarioDTO dto = new PropietarioDTO();
        dto.setId(propietario.getId());
        dto.setNombre(propietario.getNombre());
        dto.setApellidos(propietario.getApellidos());
        dto.setEmail(propietario.getEmail());
        dto.setTelefono(propietario.getTelefono());

        return dto;
    }

    public Propietario toEntity(PropietarioDTO dto) {
        if (dto == null) return null; // Escudo anti-nulos

        Propietario entity = new Propietario();

        // SÍ mapeamos el ID. Si es un Create, será null y la BBDD lo autogenerará.
        // Si es un Update, necesitamos pasárselo a la BBDD para que no lo duplique.
        entity.setId(dto.getId());

        entity.setNombre(dto.getNombre());
        entity.setApellidos(dto.getApellidos());
        entity.setEmail(dto.getEmail());
        entity.setTelefono(dto.getTelefono());

        return entity;
    }

}
