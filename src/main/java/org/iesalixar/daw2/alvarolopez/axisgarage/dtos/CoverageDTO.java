package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoverageDTO {

    private Long id;
    private String type;
    private Double totalPrice;
    private Long reservationId;
}
