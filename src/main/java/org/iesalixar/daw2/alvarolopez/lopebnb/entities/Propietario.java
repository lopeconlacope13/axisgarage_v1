package org.iesalixar.daw2.alvarolopez.lopebnb.entities;


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
@Table(name = "propietario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Propietario {

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

    @NotEmpty(message = "El telefono es obligatorio")
    @Size(max = 12)
    @Column(name = "telefono", length = 12)
    private String telefono;

    // Relación 1:N con CasaRural
    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<CasaRural> casas;

    public Propietario(String nombre, String apellidos, String email, String telefono, List<CasaRural> casas) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.casas = casas;
    }
}