package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DamageReportDTO {

    private Long id;
    private String type;
    private String description;
    private LocalDate reportedDate;
    private String imageUrl;
    private Long reservationId;
}
