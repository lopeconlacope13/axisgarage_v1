package org.iesalixar.daw2.alvarolopez.lopebnb.mappers;

import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.UserDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.User;
import org.springframework.stereotype.Component;


@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }
}
