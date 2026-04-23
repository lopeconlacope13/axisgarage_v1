package org.iesalixar.daw2.alvarolopez.axisgarage.mappers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.VehicleCategoryDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.VehicleCategory;
import org.springframework.stereotype.Component;

@Component
public class VehicleCategoryMapper {

    public VehicleCategoryDTO toDTO(VehicleCategory entity) {
        if (entity == null) return null;

        VehicleCategoryDTO dto = new VehicleCategoryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    public VehicleCategory toEntity(VehicleCategoryDTO dto) {
        if (dto == null) return null;

        VehicleCategory entity = new VehicleCategory();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
