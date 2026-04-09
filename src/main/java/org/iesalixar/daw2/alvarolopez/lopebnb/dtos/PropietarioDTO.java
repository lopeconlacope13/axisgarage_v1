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

    /**
     * Lista de casas rurales del propietario.
     * @JsonIgnoreProperties("propietarioDTO") evita el bucle infinito al serializar a JSON,
     * ya que impide que la CasaRural vuelva a intentar pintar a su propietario.
     */
    @JsonIgnoreProperties("propietarioDTO")
    private List<CasaRuralDTO> casasRuralesDTO = new ArrayList<>();

}
