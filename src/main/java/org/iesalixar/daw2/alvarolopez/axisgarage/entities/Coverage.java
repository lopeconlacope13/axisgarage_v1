package org.iesalixar.daw2.alvarolopez.axisgarage.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coverages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "{msg.coverage.type.notEmpty}")
    @Column(nullable = false, length = 20)
    private String type;

    @NotNull(message = "{msg.coverage.totalPrice.notNull}")
    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @NotNull(message = "{msg.coverage.reservation.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
}
