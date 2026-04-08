package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpinionDTO {
    private Long id;
    private Long puntuacion;
    private String comentario;
    private CasaRuralDTO casaRural;
    private HuespedDTO huesped;
}
