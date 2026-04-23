package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDTO {

    private Long id;
    private Long rating;
    private String comment;
    private Long reservationId;
    private Long renterId;

    // --- DATOS PARA MOSTRAR AL CLIENTE (GET) ---
    private String reservationString;
    private String renterName;
}
