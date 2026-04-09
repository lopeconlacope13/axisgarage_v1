package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PropietarioDTO {

    private Long id;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;


}
