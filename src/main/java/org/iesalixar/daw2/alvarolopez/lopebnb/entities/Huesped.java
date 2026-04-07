package org.iesalixar.daw2.alvarolopez.lopebnb.entities;

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
@Table(name = "huesped")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Huesped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El nombre no puede estar vacío")
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotEmpty(message = "Los apellidos no pueden estar vacíos")
    @Size(max = 100)
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @NotEmpty(message = "El email es obligatorio")
    @Email(message = "Formato de email incorrecto")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotEmpty(message = "El dni es obligatorio")
    @Size(max = 20)
    @Column(name = "dni", length = 20)
    private String dni;

    @NotEmpty(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe contener exactamente 9 dígitos numéricos")
    @Column(name = "telefono", nullable = false, unique = true, length = 12)
    private String telefono;

    // Relación 1:N con Reservas
    @OneToMany(mappedBy = "huesped", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Reserva> reservas;

    // Relación 1:N con Opiniones
    @OneToMany(mappedBy = "huesped", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Opinion> opiniones;


    public Huesped(String nombre, String apellidos, String email, String dni, String telefono, List<Reserva> reservas, List<Opinion> opiniones) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.dni = dni;
        this.telefono = telefono;
        this.reservas = reservas;
        this.opiniones = opiniones;
    }
}