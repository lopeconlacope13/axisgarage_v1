package org.iesalixar.daw2.alvarolopez.lopebnb.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "opinion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Opinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    @Column(name = "puntuacion")
    private Long puntuacion;

    /**
     * Comentario textual de la opinión, máximo 500 caracteres.
     * Este campo es opcional, el usuario puede dejar solo la puntuación.
     */
    @Size(max = 500, message = "El comentario es demasiado largo (máx 500 caracteres)")
    @Column(name = "comentario", length = 500)
    private String comentario;

    /**
     * Referencia a la casa rural sobre la cual se deja la opinión.
     * Relación N:1 (muchas opiniones → una casa).
     * LAZY porque se carga bajo demanda.
     */
    @NotNull(message = "La opinión debe pertenecer a una casa")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_rural_id", nullable = false)
    private CasaRural casaRural;

    /**
     * Referencia al huésped que deja la opinión.
     * Relación N:1 (muchas opiniones → un huésped).
     * LAZY porque se carga bajo demanda.
     */
    @NotNull(message = "La opinión debe ser de un usuario registrado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "huesped_id", nullable = false)
    private Huesped huesped;

    /**
     * Constructor de conveniencia para crear opiniones sin necesidad de especificar ID.
     * Útil para inserciones en BD donde el ID se genera automáticamente.
     *
     * @param puntuacion Puntuación de 1 a 5.
     * @param comentario Comentario opcional.
     * @param casaRural Casa sobre la que se opina.
     * @param huesped Autor de la opinión.
     */
    public Opinion(Long puntuacion, String comentario, CasaRural casaRural, Huesped huesped) {
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.casaRural = casaRural;
        this.huesped = huesped;
    }







}
