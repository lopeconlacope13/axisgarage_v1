package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.LocationDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Location;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.LocationMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona las sedes o ateliers físicos de Axis Garage.
 * Proporciona las operaciones CRUD completas sobre la entidad Location,
 * garantizando que no existan dos sedes con el mismo nombre.
 * Actúa como intermediario entre el controlador REST y el repositorio JPA,
 * aplicando validaciones de negocio antes de delegar en la persistencia.
 */
@Service
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private LocationMapper locationMapper;

    public List<LocationDTO> getAllLocations() {
        logger.info("Listando todas las sedes");
        return locationRepository.findAll()
                .stream()
                .map(locationMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<LocationDTO> getLocationById(Long id) {
        return locationRepository.findById(id).map(locationMapper::toDTO);
    }

    public LocationDTO createLocation(LocationDTO dto) {
        if (locationRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Ya existe una sede con ese nombre.");
        }
        Location entity = locationMapper.toEntity(dto);
        Location saved = locationRepository.save(entity);
        logger.info("Sede creada con ID {}", saved.getId());
        return locationMapper.toDTO(saved);
    }

    public LocationDTO updateLocation(Long id, LocationDTO dto) {
        Location existing = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada con ID: " + id));

        if (locationRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new IllegalArgumentException("Ya existe otra sede con ese nombre.");
        }

        existing.setName(dto.getName());
        existing.setCity(dto.getCity());
        existing.setAddress(dto.getAddress());
        existing.setPostalCode(dto.getPostalCode());
        existing.setCountry(dto.getCountry());
        existing.setPhone(dto.getPhone());
        existing.setEmail(dto.getEmail());

        Location saved = locationRepository.save(existing);
        logger.info("Sede actualizada con ID {}", saved.getId());
        return locationMapper.toDTO(saved);
    }

    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new IllegalArgumentException("Sede no encontrada con ID: " + id);
        }
        locationRepository.deleteById(id);
        logger.info("Sede eliminada con ID {}", id);
    }
}
