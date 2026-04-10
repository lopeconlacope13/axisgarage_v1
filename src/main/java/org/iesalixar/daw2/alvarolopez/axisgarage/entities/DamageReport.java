package org.iesalixar.daw2.alvarolopez.axisgarage.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "damage_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DamageReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "{msg.damageReport.type.notEmpty}")
    @Size(max = 10, message = "{msg.damageReport.type.size}")
    @Column(nullable = false, length = 10)
    private String type;

    @NotEmpty(message = "{msg.damageReport.description.notEmpty}")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "{msg.damageReport.reportedDate.notNull}")
    @Column(name = "reported_date", nullable = false)
    private LocalDate reportedDate;

    @Size(max = 255)
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @NotNull(message = "{msg.damageReport.reservation.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
}
