package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VehicleDTO {

    private Long id;
    private String brand;
    private String model;
    private Integer productionYear;
    private Double pricePerDay;
    private String engineType;
    private Integer horsePower;
    private Integer torqueNm;
    private String transmission;
    private String drivetrain;
    private String fuelType;
    private Double zeroToHundred;
    private String description;
    private Boolean available;

    private List<String> images = new ArrayList<>(); // Para devolver el nombre en el JSON al listar
    private List<MultipartFile> imageFiles = new ArrayList<>();

    private OwnerDTO ownerDTO;

    private Long categoryId;
    private String categoryName;

    private Long locationId;
    private String locationName;
}
