package org.iesalixar.daw2.alvarolopez.axisgarage.mappers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.OwnerDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Owner;
import org.springframework.stereotype.Component;

@Component
public class OwnerMapper {

    public OwnerDTO toDTO(Owner owner) {
        if (owner == null)
            return null;
        OwnerDTO dto = new OwnerDTO();
        dto.setId(owner.getId());
        dto.setName(owner.getName());
        dto.setLastName(owner.getLastName());
        dto.setEmail(owner.getEmail());
        dto.setPhone(owner.getPhone());
        return dto;
    }

    public Owner toEntity(OwnerDTO dto) {
        if (dto == null)
            return null;
        Owner entity = new Owner();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        return entity;
    }

}
