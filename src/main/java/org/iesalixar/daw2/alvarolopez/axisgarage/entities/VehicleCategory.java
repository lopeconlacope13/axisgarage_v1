package org.iesalixar.daw2.alvarolopez.axisgarage.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicle_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "{msg.vehicleCategory.name.notEmpty}")
    @Size(max = 50, message = "{msg.vehicleCategory.name.size}")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Size(max = 255, message = "{msg.vehicleCategory.description.size}")
    @Column(length = 255)
    private String description;
}
