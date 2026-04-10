package org.iesalixar.daw2.alvarolopez.axisgarage.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "La marca es obligatoria")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String brand;

    @NotEmpty(message = "El modelo es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String model;

    @NotNull(message = "El año de producción es obligatorio")
    @Column(name = "production_year", nullable = false)
    private Integer productionYear;

    @NotNull(message = "El precio por dia es obligatorio")
    @Column(name = "price_per_day", nullable = false)
    private Double pricePerDay;

    @NotEmpty(message = "El tipo de motor es obligatorio")
    @Column(name = "engine_type", nullable = false)
    private String engineType;

    @NotNull(message = "Los CV son obligatorios")
    @Column(name = "horse_power", nullable = false)
    private Integer horsePower;

    @NotNull(message = "El torque es obligatorio")
    @Column(name = "torque_nm", nullable = false)
    private Integer torqueNm;

    @NotEmpty(message = "La transmisión es obligatoria")
    @Column(name = "transmission", nullable = false)
    private String transmission;

    @NotEmpty(message = "La tracción es obligatoria")
    @Column(name = "drivetrain", nullable = false)
    private String drivetrain;

    @NotEmpty(message = "El tipo de combustible es obligatorio")
    @Column(name = "fuel_type", nullable = false)
    private String fuelType;

    @Column(name = "zero_to_hundred")
    private Double zeroToHundred;

    @Column(name = "description", length = 1000)
    private String description;

    @NotNull(message = "La disponibilidad es obligatoria")
    @Column(name = "available", nullable = false)
    private Boolean available;

    // Relación con el Owner (Dueño). Carga perezosa (Lazy).
    @NotNull(message = "El vehículo debe tener un propietario asignado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    // Relación con VehicleCategory (Categoría)
    @NotNull(message = "El vehículo debe tener una categoría asignada")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private VehicleCategory category;

    // Relación con Location (Sede)
    @NotNull(message = "El vehículo debe tener una sede asignada")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // Relación con Reservations.
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "vehicle_image", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    public Vehicle(String brand, String model, Integer productionYear, Double pricePerDay, String engineType,
            Integer horsePower, Integer torqueNm, String transmission, String drivetrain, String fuelType,
            Double zeroToHundred, String description, Boolean available, Owner owner, List<Reservation> reservations,
            List<String> images) {
        this.brand = brand;
        this.model = model;
        this.productionYear = productionYear;
        this.pricePerDay = pricePerDay;
        this.engineType = engineType;
        this.horsePower = horsePower;
        this.torqueNm = torqueNm;
        this.transmission = transmission;
        this.drivetrain = drivetrain;
        this.fuelType = fuelType;
        this.zeroToHundred = zeroToHundred;
        this.description = description;
        this.available = available;
        this.owner = owner;
        this.reservations = reservations;
        this.images = images;
    }
}
