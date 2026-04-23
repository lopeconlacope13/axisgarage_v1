package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReservationDTO {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalPrice;
    private String status;
    private Long vehicleId;
    private Long renterId;

    // --- DATOS PARA MOSTRAR AL CLIENTE (GET) ---
    private String vehicleModel;
    private String renterName;
}
