package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservaDTO {

    private Long id;

    @NotNull(message = "La fecha de entrada es obligatoria")
    private LocalDate fechaEntrada;

    @NotNull(message = "La fecha de salida es obligatoria")
    private LocalDate fechaSalida;

    private Double importeTotal;

    //Para extraer la info de las otras entidades, usamos los DTO's no las entidades
    private CasaRuralDTO casaRural;
    private HuespedDTO huesped;

}
