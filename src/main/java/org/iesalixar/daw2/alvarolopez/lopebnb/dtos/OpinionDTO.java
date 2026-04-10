package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpinionDTO {

    private Long id;
    private Long puntuacion;
    private String comentario;
    private Long casaRuralId;
    private Long huespedId;

    // --- DATOS PARA MOSTRAR AL CLIENTE (GET) ---
    private String nombreCasaRural;
    private String nombreHuesped;
}
