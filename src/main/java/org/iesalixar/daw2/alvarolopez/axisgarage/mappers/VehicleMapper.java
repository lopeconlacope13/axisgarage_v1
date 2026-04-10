package org.iesalixar.daw2.alvarolopez.axisgarage.mappers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.VehicleDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Location;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Owner;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Vehicle;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.VehicleCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    @Autowired
    private OwnerMapper ownerMapper;

    // --- DE ENTIDAD A DTO ---
    public VehicleDTO toDTO(Vehicle vehicle) {
        if (vehicle == null)
            return null;

        VehicleDTO dto = new VehicleDTO();
        dto.setId(vehicle.getId());
        dto.setBrand(vehicle.getBrand());
        dto.setModel(vehicle.getModel());
        dto.setProductionYear(vehicle.getProductionYear());
        dto.setPricePerDay(vehicle.getPricePerDay());
        dto.setEngineType(vehicle.getEngineType());
        dto.setHorsePower(vehicle.getHorsePower());
        dto.setTorqueNm(vehicle.getTorqueNm());
        dto.setTransmission(vehicle.getTransmission());
        dto.setDrivetrain(vehicle.getDrivetrain());
        dto.setFuelType(vehicle.getFuelType());
        dto.setZeroToHundred(vehicle.getZeroToHundred());
        dto.setDescription(vehicle.getDescription());
        dto.setAvailable(vehicle.getAvailable());
        dto.setImages(vehicle.getImages());

        // AQUÍ USAMOS EL MAPPER INYECTADO PARA METER AL DUEÑO
        if (vehicle.getOwner() != null) {
            dto.setOwnerDTO(ownerMapper.toDTO(vehicle.getOwner()));
        }

        if (vehicle.getCategory() != null) {
            dto.setCategoryId(vehicle.getCategory().getId());
            dto.setCategoryName(vehicle.getCategory().getName());
        }

        if (vehicle.getLocation() != null) {
            dto.setLocationId(vehicle.getLocation().getId());
            dto.setLocationName(vehicle.getLocation().getName());
        }

        return dto;
    }

    // --- DE DTO A ENTIDAD ---
    // Le pasamos las Entidades de relación como parámetros
    public Vehicle toEntity(VehicleDTO createDTO, Owner ownerReal, VehicleCategory category, Location location) {
        if (createDTO == null)
            return null;

        Vehicle vehicle = new Vehicle();
        vehicle.setId(createDTO.getId());
        vehicle.setBrand(createDTO.getBrand());
        vehicle.setModel(createDTO.getModel());
        vehicle.setProductionYear(createDTO.getProductionYear());
        vehicle.setPricePerDay(createDTO.getPricePerDay());
        vehicle.setEngineType(createDTO.getEngineType());
        vehicle.setHorsePower(createDTO.getHorsePower());
        vehicle.setTorqueNm(createDTO.getTorqueNm());
        vehicle.setTransmission(createDTO.getTransmission());
        vehicle.setDrivetrain(createDTO.getDrivetrain());
        vehicle.setFuelType(createDTO.getFuelType());
        vehicle.setZeroToHundred(createDTO.getZeroToHundred());
        vehicle.setDescription(createDTO.getDescription());
        vehicle.setAvailable(createDTO.getAvailable());

        vehicle.setOwner(ownerReal);
        vehicle.setCategory(category);
        vehicle.setLocation(location);

        return vehicle;
    }
}
