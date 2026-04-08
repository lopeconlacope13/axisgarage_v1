package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PropietarioDTO {

    private Long id;

    @NotEmpty(message = "El nombre no puede estar vacío")
    @Size(max = 100)
    private String nombre;

    @NotEmpty(message = "Los apellidos no pueden estar vacíos")
    @Size(max = 100)
    private String apellidos;

    @NotEmpty(message = "El email es obligatorio")
    @Email(message = "Formato de email incorrecto")
    private String email;

    @NotEmpty(message = "El telefono es obligatorio")
    @Size(max = 12)
    private String telefono;

}
