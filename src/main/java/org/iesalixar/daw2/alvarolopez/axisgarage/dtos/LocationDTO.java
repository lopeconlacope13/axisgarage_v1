package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationDTO {

    private Long id;
    private String name;
    private String city;
    private String address;
    private String postalCode;
    private String country;
    private String phone;
    private String email;
}
