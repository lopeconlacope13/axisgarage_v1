package org.iesalixar.daw2.alvarolopez.lopebnb.mappers;

import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.CasaRuralDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.PropietarioDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CasaRuralMapper {

    @Autowired
    private PropietarioMapper propietarioMapper;

    // --- DE ENTIDAD A DTO ---
    public CasaRuralDTO toDTO(CasaRural casaRural) {
        if (casaRural == null) return null;

        CasaRuralDTO dto = new CasaRuralDTO();
        dto.setId(casaRural.getId());
        dto.setNombre(casaRural.getNombre());
        dto.setDireccion(casaRural.getDireccion()); // Añadido
        dto.setPrecioNoche(casaRural.getPrecioNoche());
        dto.setCapacidadPersonas(casaRural.getCapacidadPersonas());
        dto.setImagenes(casaRural.getImagenes()); // Añadido para que el Front sepa la foto

        // AQUÍ USAMOS EL MAPPER INYECTADO PARA METER AL DUEÑO
        if (casaRural.getPropietario() != null) {
            dto.setPropietarioDTO(propietarioMapper.toDTO(casaRural.getPropietario()));
        }

        return dto;
    }

    // --- DE DTO A ENTIDAD ---
    // Le pasamos la Entidad Propietario como segundo parámetro
    public CasaRural toEntity(CasaRuralDTO createDTO, Propietario propietarioReal) {
        if (createDTO == null) return null;

        CasaRural casaRural = new CasaRural();
        casaRural.setId(createDTO.getId());
        casaRural.setNombre(createDTO.getNombre());
        casaRural.setDireccion(createDTO.getDireccion()); // Añadido
        casaRural.setPrecioNoche(createDTO.getPrecioNoche());
        casaRural.setCapacidadPersonas(createDTO.getCapacidadPersonas());

        // Le asignamos el propietario real que nos manda el Servicio
        casaRural.setPropietario(propietarioReal);

        return casaRural;
    }
}
