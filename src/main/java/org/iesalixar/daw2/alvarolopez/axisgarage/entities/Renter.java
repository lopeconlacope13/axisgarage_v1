package org.iesalixar.daw2.alvarolopez.axisgarage.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "renter")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Renter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "{msg.renter.name.notEmpty}")
    @Size(max = 100, message = "{msg.renter.name.size}")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotEmpty(message = "{msg.renter.lastName.notEmpty}")
    @Size(max = 100, message = "{msg.renter.lastName.size}")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotEmpty(message = "{msg.renter.email.notEmpty}")
    @Email(message = "{msg.renter.email.email}")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotEmpty(message = "{msg.renter.dni.notEmpty}")
    @Size(max = 20, message = "{msg.renter.dni.size}")
    @Column(name = "dni", length = 20)
    private String dni;

    @NotEmpty(message = "{msg.renter.phone.notEmpty}")
    @Pattern(regexp = "^[0-9]{9}$", message = "{msg.renter.phone.pattern}")
    @Column(name = "phone", nullable = false, unique = true, length = 12)
    private String phone;

    // Relación 1:N con Reservations
    @OneToMany(mappedBy = "renter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Reservation> reservations;

    // Relación 1:N con Reviews
    @OneToMany(mappedBy = "renter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Review> reviews;

    public Renter(String name, String lastName, String email, String dni, String phone, List<Reservation> reservations,
            List<Review> reviews) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.dni = dni;
        this.phone = phone;
        this.reservations = reservations;
        this.reviews = reviews;
    }
}
