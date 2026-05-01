package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    /** Nombre del archivo de imagen de perfil guardado en el servidor (puede ser null) */
    private String image;
}
