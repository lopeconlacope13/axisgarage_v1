package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HuespedDTO {

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

    @NotEmpty(message = "El dni es obligatorio")
    @Size(max = 20)
    private String dni;

    @NotEmpty(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe contener exactamente 9 dígitos numéricos")
    private String telefono;

    //Omitimos reservas y opiniones, solo nos interesa los datos de los huéspedes
}
