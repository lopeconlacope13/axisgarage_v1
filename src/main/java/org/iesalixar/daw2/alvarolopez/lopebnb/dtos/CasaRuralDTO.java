package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;


import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CasaRuralDTO {

    private Long id;
    private String nombre;
    private String direccion;
    private Double precioNoche;
    private Long capacidadPersonas;
    private List<String> imagenes = new ArrayList<>(); // Para devolver el nombre en el JSON al listar
    private List<MultipartFile> imageFiles = new ArrayList<>();

    private PropietarioDTO propietarioDTO;


}
