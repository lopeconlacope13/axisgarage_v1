package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CasaRuralDTO {

    private Long id;

    @NotEmpty(message = "El nombre de la casa es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotEmpty(message = "La direccion es obligatoria")
    @Size(max = 255, message = "La dirección es demasiado larga")
    private String direccion;

    @NotNull(message = "El precio por noche es obligatorio")
    private Double precioNoche;

    // No le pusiste validación en la entidad, así que aquí tampoco hace falta
    private Long capacidadPersonas;

    // En lugar de pedir la entidad Propietario entera,
    // solo pedimos su ID para que Angular nos mande: "propietarioId": 5
    @NotNull(message = "La casa debe tener un propietario asignado")
    private Long propietarioId;
    private String nombrePropietario;

    // Omitimos por completo las listas de 'reservas' y 'opiniones'
    // para que el JSON sea ligero y no haya bucles infinitos.


}
