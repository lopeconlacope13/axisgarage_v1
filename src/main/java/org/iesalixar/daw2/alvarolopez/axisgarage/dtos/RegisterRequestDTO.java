package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO para el registro de un nuevo usuario en Axis Garage.
 * El username se genera automáticamente a partir del email.
 */
@Getter
@Setter
public class RegisterRequestDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
