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
    private Long casaRuralId;
    private Long huespedId;

    // --- DATOS PARA MOSTRAR AL CLIENTE (GET) ---
    private String nombreCasaRural;
    private String nombreHuesped;

}
