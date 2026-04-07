package org.iesalixar.daw2.alvarolopez.lopebnb.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FECHA DE ENTRADA
    @NotNull(message = "La fecha de entrada es obligatoria")
    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    // FECHA DE SALIDA
    @NotNull(message = "La fecha de salida es obligatoria")
    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    // IMPORTE (Double es correcto para dinero en este nivel)
    @Column(name = "importe_total")
    private Double importeTotal;

    // RELACIÓN CON CASA (N:1)
    @NotNull(message = "La reserva debe asociarse a una casa")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_rural_id", nullable = false)
    private CasaRural casaRural;

    // RELACIÓN CON HUESPED (N:1)
    @NotNull(message = "La reserva debe tener un huesped titular")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "huesped_id", nullable = false)
    private Huesped huesped;

    public Reserva(LocalDate fechaEntrada, LocalDate fechaSalida, Double importeTotal, CasaRural casaRural, Huesped huesped) {
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.importeTotal = importeTotal;
        this.casaRural = casaRural;
        this.huesped = huesped;
    }
}