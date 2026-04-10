package org.iesalixar.daw2.alvarolopez.lopebnb.mappers;

import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.OpinionDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Huesped;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Opinion;
import org.springframework.stereotype.Component;


@Component
public class OpinionMapper {

    public OpinionDTO toDTO(Opinion entity) {
        if (entity == null) return null;

        OpinionDTO dto = new OpinionDTO();
        dto.setId(entity.getId());
        dto.setPuntuacion(entity.getPuntuacion());
        dto.setComentario(entity.getComentario());

        // Extraemos datos de la casa
        if (entity.getCasaRural() != null) {
            dto.setCasaRuralId(entity.getCasaRural().getId());
            dto.setNombreCasaRural(entity.getCasaRural().getNombre());
        }

        // Extraemos datos del huésped
        if (entity.getHuesped() != null) {
            dto.setHuespedId(entity.getHuesped().getId());
            dto.setNombreHuesped(entity.getHuesped().getNombre() + " " + entity.getHuesped().getApellidos());
        }

        return dto;
    }

    public Opinion toEntity(OpinionDTO dto, CasaRural casaRural, Huesped huesped) {
        if (dto == null) return null;

        Opinion entity = new Opinion();
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setPuntuacion(dto.getPuntuacion());
        entity.setComentario(dto.getComentario());
        entity.setCasaRural(casaRural);
        entity.setHuesped(huesped);

        return entity;
    }
}
