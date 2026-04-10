package org.iesalixar.daw2.alvarolopez.axisgarage.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FECHA DE ENTRADA
    @NotNull(message = "La fecha de entrada es obligatoria")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // FECHA DE SALIDA
    @NotNull(message = "La fecha de salida es obligatoria")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // IMPORTE (Double es correcto para dinero en este nivel)
    @Column(name = "total_price")
    private Double totalPrice;

    // ESTADO
    @Column(name = "status")
    private String status;

    // RELACIÓN CON VEHICULO (N:1)
    @NotNull(message = "La reserva debe asociarse a un vehículo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // RELACIÓN CON RENTER (N:1)
    @NotNull(message = "La reserva debe tener un renter titular")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private Renter renter;

    public Reservation(LocalDate startDate, LocalDate endDate, Double totalPrice, String status, Vehicle vehicle,
            Renter renter) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.vehicle = vehicle;
        this.renter = renter;
    }
}
