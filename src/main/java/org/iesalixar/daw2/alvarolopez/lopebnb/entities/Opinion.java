package org.iesalixar.daw2.alvarolopez.lopebnb.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "opinion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Opinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // PUNTUACIÓN (Long/BIGINT)
    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    @Column(name = "puntuacion")
    private Long puntuacion;

    // COMENTARIO
    @Size(max = 500, message = "El comentario es demasiado largo (máx 500 caracteres)")
    @Column(name = "comentario", length = 500)
    private String comentario;

    // RELACIÓN CON CASA
    @NotNull(message = "La opinión debe pertenecer a una casa")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_rural_id", nullable = false)
    private CasaRural casaRural;

    // RELACIÓN CON HUESPED
    @NotNull(message = "La opinión debe ser de un usuario registrado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "huesped_id", nullable = false)
    private Huesped huesped;

    public Opinion(Long puntuacion, String comentario, CasaRural casaRural, Huesped huesped) {
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.casaRural = casaRural;
        this.huesped = huesped;
    }







}
