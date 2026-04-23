package org.iesalixar.daw2.alvarolopez.axisgarage.mappers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.RoleDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Role;
import org.springframework.stereotype.Component;


@Component
public class RoleMapper {

    public RoleDTO toDTO(Role entity) {
        if (entity == null) {
            return null;
        }

        RoleDTO dto = new RoleDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());

        return dto;
    }

    public Role toEntity(RoleDTO dto) {
        if (dto == null) {
            return null;
        }

        Role entity = new Role();
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setName(dto.getName());

        return entity;
    }
}
