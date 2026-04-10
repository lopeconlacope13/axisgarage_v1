package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.RenterDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Renter;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.RenterMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.RenterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RenterService {

    private static final Logger logger = LoggerFactory.getLogger(RenterService.class);

    @Autowired
    private RenterRepository renterRepository;

    @Autowired
    private RenterMapper renterMapper;

    // --- 1. LISTAR CON PAGINACIÓN ---

    public Page<RenterDTO> getAllRenters(String name, String dni, Pageable pageable) {
        try {
            logger.info("Solicitando huéspedes con filtros -> Nombre: {}, DNI: {}", name, dni);

            Page<Renter> renters = renterRepository.findByFiltros(name, dni, pageable);
            logger.info("Se han encontrado {} huéspedes.", renters.getNumberOfElements());

            return renters.map(renterMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista de huéspedes: {}", e.getMessage());
            throw new RuntimeException("Error interno al listar los huéspedes", e);
        }
    }

    // --- 2. OBTENER UNO POR ID ---

    public Optional<RenterDTO> getRenterById(Long id) {
        try {
            logger.info("Buscando huésped con ID {}", id);
            return renterRepository.findById(id).map(renterMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar el huésped con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar el huésped.", e);
        }
    }

    // --- 3. CREAR HUÉSPED ---

    public RenterDTO createRenter(@Valid RenterDTO renterDTO) {
        try {
            logger.info("Creando nuevo huésped con DNI: {}", renterDTO.getDni());

            // Reglas de negocio: unicidad
            if (renterRepository.findByDni(renterDTO.getDni()).isPresent()) {
                throw new IllegalArgumentException("El DNI ya está registrado.");
            }
            if (renterRepository.findByEmail(renterDTO.getEmail()).isPresent()) {
                throw new IllegalArgumentException("El email ya está registrado.");
            }
            if (renterRepository.findByPhone(renterDTO.getPhone()).isPresent()) {
                throw new IllegalArgumentException("El teléfono ya está registrado.");
            }

            Renter renter = renterMapper.toEntity(renterDTO);
            Renter savedRenter = renterRepository.save(renter);

            logger.info("Huésped creado exitosamente con ID {}", savedRenter.getId());
            return renterMapper.toDTO(savedRenter);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al crear huésped: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear el huésped: {}", e.getMessage());
            throw new RuntimeException("Error interno al crear el huésped.");
        }
    }

    // --- 4. ACTUALIZAR HUÉSPED ---

    public RenterDTO updateRenter(Long id, @Valid RenterDTO renterDTO) {
        try {
            logger.info("Actualizando huésped con ID {}", id);

            Renter existente = renterRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Huésped no encontrado con ID: " + id));

            // Lógica de exclusión para evitar falsos duplicados
            Optional<Renter> porDni = renterRepository.findByDni(renterDTO.getDni());
            if (porDni.isPresent() && !porDni.get().getId().equals(id)) {
                throw new IllegalArgumentException("El DNI ya pertenece a otro huésped.");
            }

            Optional<Renter> porEmail = renterRepository.findByEmail(renterDTO.getEmail());
            if (porEmail.isPresent() && !porEmail.get().getId().equals(id)) {
                throw new IllegalArgumentException("El email ya pertenece a otro huésped.");
            }

            Optional<Renter> porPhone = renterRepository.findByPhone(renterDTO.getPhone());
            if (porPhone.isPresent() && !porPhone.get().getId().equals(id)) {
                throw new IllegalArgumentException("El teléfono ya pertenece a otro huésped.");
            }

            existente.setName(renterDTO.getName());
            existente.setLastName(renterDTO.getLastName());
            existente.setEmail(renterDTO.getEmail());
            existente.setDni(renterDTO.getDni());
            existente.setPhone(renterDTO.getPhone());

            Renter updatedRenter = renterRepository.save(existente);
            logger.info("Huésped actualizado con éxito (ID {})", id);
            return renterMapper.toDTO(updatedRenter);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al actualizar huésped: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el huésped: {}", e.getMessage());
            throw new RuntimeException("Error interno al actualizar.");
        }
    }

    // --- 5. BORRAR ---

    public void deleteRenter(Long id) {
        try {
            logger.info("Borrando huésped con ID {}", id);
            if (!renterRepository.existsById(id)) {
                throw new IllegalArgumentException("Huésped no encontrado con ID: " + id);
            }
            renterRepository.deleteById(id);
            logger.info("Huésped borrado correctamente.");

        } catch (IllegalArgumentException e) {
            logger.warn("No se pudo borrar el huésped: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al borrar el huésped: {}", e.getMessage());
            throw new RuntimeException("Error interno al borrar el huésped.");
        }
    }
}
