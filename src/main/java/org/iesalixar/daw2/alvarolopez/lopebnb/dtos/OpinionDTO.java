package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OpinionDTO {
    private Long id;

    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Long puntuacion;

    @Size(max = 500, message = "El comentario es demasiado largo (máx 500 caracteres)")
    private String comentario;

    @NotNull(message = "La opinión debe pertenecer a una casa")
    private CasaRuralDTO casaRural;

    @NotNull(message = "La opinión debe ser de un usuario registrado")
    private HuespedDTO huesped;
}
