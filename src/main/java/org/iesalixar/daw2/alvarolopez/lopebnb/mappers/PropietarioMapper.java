package org.iesalixar.daw2.alvarolopez.lopebnb.mappers;

import jakarta.validation.constraints.NotNull;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.CasaRuralDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.PropietarioDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PropietarioMapper {

    /**
     * Convierte de Entidad a DTO.
     * Implementa lógica de mapeo para la colección de casas usando Streams.
     */
    public PropietarioDTO toDTO(Propietario propietario) {
        if (propietario == null) return null;

        PropietarioDTO dto = new PropietarioDTO();
        dto.setId(propietario.getId());
        dto.setNombre(propietario.getNombre());
        dto.setApellidos(propietario.getApellidos());
        dto.setEmail(propietario.getEmail());
        dto.setTelefono(propietario.getTelefono());

        // Implementación con Java 21: .toList() sustituye a .collect(Collectors.toList())
        if (propietario.getCasas() != null && !propietario.getCasas().isEmpty()) {
            dto.setCasasRuralesDTO(propietario.getCasas().stream().map(casa -> {
                CasaRuralDTO casaDTO = new CasaRuralDTO();
                casaDTO.setId(casa.getId());
                casaDTO.setNombre(casa.getNombre());
                casaDTO.setDireccion(casa.getDireccion());
                casaDTO.setPrecioNoche(casa.getPrecioNoche());
                casaDTO.setCapacidadPersonas(casa.getCapacidadPersonas());
                casaDTO.setImagen(casa.getImagen());
                // Rompemos la referencia circular dejando el propietario en null aquí
                return casaDTO;
            }).toList());
        }

        return dto;
    }

    public Propietario toEntity(PropietarioDTO dto) {
        if (dto == null) return null;
        Propietario entity = new Propietario();
        entity.setId(dto.getId());
        entity.setNombre(dto.getNombre());
        entity.setApellidos(dto.getApellidos());
        entity.setEmail(dto.getEmail());
        entity.setTelefono(dto.getTelefono());
        return entity;
    }

}
