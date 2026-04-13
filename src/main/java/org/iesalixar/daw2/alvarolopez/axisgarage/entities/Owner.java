package org.iesalixar.daw2.alvarolopez.axisgarage.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "owner")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "{msg.owner.name.notEmpty}")
    @Size(max = 100, message = "{msg.owner.name.size}")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotEmpty(message = "{msg.owner.lastName.notEmpty}")
    @Size(max = 100, message = "{msg.owner.lastName.size}")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotEmpty(message = "{msg.owner.email.notEmpty}")
    @Email(message = "{msg.owner.email.email}")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotEmpty(message = "{msg.owner.phone.notEmpty}")
    @Size(max = 12, message = "{msg.owner.phone.size}")
    @Column(name = "phone", length = 12)
    private String phone;

    // Relación 1:N con Vehicle
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Vehicle> vehicles;

    public Owner(String name, String lastName, String email, String phone, List<Vehicle> vehicles) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.vehicles = vehicles;
    }
}
