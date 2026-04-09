package org.iesalixar.daw2.alvarolopez.lopebnb.mappers;

import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.ReservaDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Huesped;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Reserva;
import org.springframework.stereotype.Component;

@Component

public class ReservaMapper {

    public ReservaDTO toDTO(Reserva entity) {
        if (entity == null) return null;

        ReservaDTO dto = new ReservaDTO();
        dto.setId(entity.getId());
        dto.setFechaEntrada(entity.getFechaEntrada());
        dto.setFechaSalida(entity.getFechaSalida());
        dto.setImporteTotal(entity.getImporteTotal());

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

    public Reserva toEntity(ReservaDTO dto, CasaRural casaRural, Huesped huesped) {
        if (dto == null) return null;

        Reserva entity = new Reserva();
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setFechaEntrada(dto.getFechaEntrada());
        entity.setFechaSalida(dto.getFechaSalida());

        // El importeTotal se ignora aquí porque lo calculará de forma segura el Service

        entity.setCasaRural(casaRural);
        entity.setHuesped(huesped);

        return entity;
    }


}
