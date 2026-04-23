package org.iesalixar.daw2.alvarolopez.axisgarage.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "{msg.review.rating.notNull}")
    @Min(value = 1, message = "{msg.review.rating.min}")
    @Max(value = 5, message = "{msg.review.rating.max}")
    @Column(name = "rating")
    private Long rating;

    @Size(max = 500, message = "{msg.review.comment.size}")
    @Column(name = "comment", length = 500)
    private String comment;

    @NotNull(message = "{msg.review.reservation.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @NotNull(message = "{msg.review.renter.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private Renter renter;

    public Review(Long rating, String comment, Reservation reservation, Renter renter) {
        this.rating = rating;
        this.comment = comment;
        this.reservation = reservation;
        this.renter = renter;
    }
}
