package org.iesalixar.daw2.alvarolopez.axisgarage.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "{msg.location.name.notEmpty}")
    @Size(max = 100, message = "{msg.location.name.size}")
    @Column(nullable = false, length = 100)
    private String name;

    @NotEmpty(message = "{msg.location.city.notEmpty}")
    @Size(max = 100, message = "{msg.location.city.size}")
    @Column(nullable = false, length = 100)
    private String city;

    @NotEmpty(message = "{msg.location.address.notEmpty}")
    @Size(max = 255, message = "{msg.location.address.size}")
    @Column(nullable = false, length = 255)
    private String address;

    @Size(max = 10, message = "{msg.location.postalCode.size}")
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @NotEmpty(message = "{msg.location.country.notEmpty}")
    @Size(max = 50, message = "{msg.location.country.size}")
    @Column(nullable = false, length = 50)
    private String country;

    @Size(max = 20, message = "{msg.location.phone.size}")
    @Column(length = 20)
    private String phone;

    @Size(max = 100, message = "{msg.location.email.size}")
    @Column(length = 100)
    private String email;
}
