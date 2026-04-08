package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class UserDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String image;
    private LocalDateTime createdDate;

    //omitimos password
    //pasar rolDTO, que es solo 1 sting obje tipado
    private Set<String> roles;
}
