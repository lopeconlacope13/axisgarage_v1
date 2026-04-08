package org.iesalixar.daw2.alvarolopez.lopebnb.dtos;

import java.time.LocalDateTime;
import java.util.Set;

public class UserDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String image;
    private LocalDateTime createdDate;

    //omitimos password

    private Set<String> roles;
}
