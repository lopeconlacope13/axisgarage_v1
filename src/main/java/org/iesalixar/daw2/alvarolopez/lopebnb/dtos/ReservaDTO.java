package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReservaDTO {

    private Long id;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private Double importeTotal;

    //Para extraer la info de las otras entidades, usamos los DTO's no las entidades
    private CasaRuralDTO casaRural;
    private HuespedDTO huesped;

}
