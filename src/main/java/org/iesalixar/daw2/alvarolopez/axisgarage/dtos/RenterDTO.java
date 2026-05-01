package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenterDTO {

    private Long id;
    private String name;
    private String lastName;
    private String email;
    private String dni;
    private String phone;
    private String address;
}
