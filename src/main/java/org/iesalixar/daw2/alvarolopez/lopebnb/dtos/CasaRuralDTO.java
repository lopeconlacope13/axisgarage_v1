package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;


import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CasaRuralDTO {

    private Long id;
    private String nombre;
    private String direccion;
    private Double precioNoche;
    private Long capacidadPersonas;
    private String imagen; // Para devolver el nombre en el JSON al listar
    private MultipartFile imageFile; // Para recibir el archivo físico al crear/editar

    private PropietarioDTO propietarioDTO;


}
