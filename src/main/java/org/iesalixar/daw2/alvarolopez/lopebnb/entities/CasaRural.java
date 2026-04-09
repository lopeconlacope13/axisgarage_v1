package org.iesalixar.daw2.alvarolopez.lopebnb.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "casa_rural")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasaRural {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre comercial de la casa.
    @NotEmpty(message = "El nombre de la casa es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    // Dirección física.
    @NotEmpty(message = "La direccion es obligatoria")
    @Size(max = 255, message = "La dirección es demasiado larga")
    @Column(nullable = false)
    private String direccion;

    // Precio por noche en euros.
    @NotNull(message = "El precio por noche es obligatorio")
    @Column(name = "precio_noche", nullable = false)
    private Double precioNoche;

    // Capacidad máxima de personas.
    @Column(name = "capacidad_personas")
    private Long capacidadPersonas;

    // Si te da error, lo cambias a EAGER. Si funciona, lo dejas en LAZY.

    // Relación con el Propietario (Dueño). Carga perezosa (Lazy).
    @NotNull(message = "La casa debe tener un propietario asignado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Propietario propietario;

    // Relación con Reservas.
    @OneToMany(mappedBy = "casaRural", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Reserva> reservas = new ArrayList<>();

    // Relación con Opiniones.
    @OneToMany(mappedBy = "casaRural", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Opinion> opiniones = new ArrayList<>();

    @Column(name = "imagen")
    private String imagen;

     //Constructor sin ID para facilitar la creación.
    public CasaRural(String nombre, String direccion, Double precioNoche, Long capacidadPersonas, Propietario propietario) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.precioNoche = precioNoche;
        this.capacidadPersonas = capacidadPersonas;
        this.propietario = propietario;
    }
}