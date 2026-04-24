package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.OwnerDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Owner;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.OwnerMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio que gestiona la lógica de negocio de los propietarios de vehículos.
 * <p>
 * En Axis Garage, un {@link Owner} es la persona física o jurídica titular de
 * uno o varios vehículos de la flota. Este servicio garantiza la unicidad de
 * email y teléfono antes de cualquier alta o modificación, evitando registros
 * duplicados en la base de datos.
 * </p>
 */
@Service
public class OwnerService {

    private static final Logger logger = LoggerFactory.getLogger(OwnerService.class);

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private OwnerMapper ownerMapper;

    // --- 1. LISTAR CON PAGINACIÓN ---

    /**
     * Obtiene una lista paginada de propietarios, permitiendo filtrar por nombre e email.
     *
     * @param name Nombre del propietario (opcional).
     * @param email Email del propietario (opcional).
     * @param pageable Configuración de paginación y ordenación.
     * @return Page con los DTOs de los propietarios encontrados.
     * @throws RuntimeException si ocurre un error interno en el proceso.
     */
    public Page<OwnerDTO> getAllOwners(String name, String email, Pageable pageable) {
        try {
            logger.info("Solicitando propietarios con filtros -> Nombre: {}, Email: {}", name, email);

            Page<Owner> owners = ownerRepository.findByFiltros(name, email, pageable);
            logger.info("Se han encontrado {} propietarios.", owners.getNumberOfElements());

            return owners.map(ownerMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista de propietarios: {}", e.getMessage());
            throw new RuntimeException("Error interno al listar los propietarios", e);
        }
    }

    // --- 2. OBTENER UNO POR ID ---

    /**
     * Busca un propietario específico por su identificador único.
     *
     * @param id Identificador del propietario.
     * @return Optional con el DTO del propietario si existe, o vacío si no se encuentra.
     * @throws RuntimeException si ocurre un error al acceder a la base de datos.
     */
    public Optional<OwnerDTO> getOwnerById(Long id) {
        try {
            logger.info("Buscando propietario con ID {}", id);
            return ownerRepository.findById(id).map(ownerMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar el propietario con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar el propietario.", e);
        }
    }

    // --- 3. CREAR PROPIETARIO ---

    /**
     * Crea un nuevo propietario asegurando que su email y teléfono no estén ya registrados.
     *
     * @param ownerDTO Objeto DTO con los datos del propietario.
     * @return OwnerDTO con los datos del propietario creado y guardado.
     * @throws IllegalArgumentException si el email o el teléfono ya están en uso.
     * @throws RuntimeException si ocurre un error inesperado al guardar.
     */
    public OwnerDTO createOwner(@Valid OwnerDTO ownerDTO) {
        try {
            logger.info("Creando nuevo propietario con email: {}", ownerDTO.getEmail());

            // Comprobamos que el email y el teléfono deben ser únicos
            if (ownerRepository.findByEmail(ownerDTO.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Error: El email '" + ownerDTO.getEmail() + "' ya está registrado.");
            }
            if (ownerRepository.findByPhone(ownerDTO.getPhone()).isPresent()) {
                throw new IllegalArgumentException(
                        "Error: El teléfono '" + ownerDTO.getPhone() + "' ya está registrado.");
            }

            Owner owner = ownerMapper.toEntity(ownerDTO);
            Owner savedOwner = ownerRepository.save(owner);

            logger.info("Propietario creado exitosamente con ID {}", savedOwner.getId());
            return ownerMapper.toDTO(savedOwner);

        } catch (IllegalArgumentException e) {
            // Si es un error de validación propio, se relanza tal cual para que el
            // Controlador devuelva un 400 Bad Request
            logger.warn("Validación fallida al crear: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear el propietario: {}", e.getMessage());
            throw new RuntimeException("Error interno al crear el propietario.");
        }
    }

    // --- 4. ACTUALIZAR PROPIETARIO ---

    /**
     * Actualiza la información de un propietario existente, verificando reglas de duplicidad.
     *
     * @param id Identificador del propietario a actualizar.
     * @param ownerDTO DTO con los nuevos datos del propietario.
     * @return OwnerDTO con la información actualizada.
     * @throws IllegalArgumentException si el propietario no existe o si el email/teléfono ya pertenecen a otro.
     * @throws RuntimeException si ocurre un error interno en la actualización.
     */
    public OwnerDTO updateOwner(Long id, @Valid OwnerDTO ownerDTO) {
        try {
            logger.info("Actualizando propietario con ID {}", id);

            // 1. Comprobamos que el propietario existe
            Owner existente = ownerRepository.findById(id)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Error: El propietario con ID " + id + " no existe."));

            // 2. LÓGICA DE NEGOCIO (Excluyendo al propio usuario)
            Optional<Owner> porEmail = ownerRepository.findByEmail(ownerDTO.getEmail());
            if (porEmail.isPresent() && !porEmail.get().getId().equals(id)) {
                throw new IllegalArgumentException("Error: El email ya pertenece a otro propietario.");
            }

            Optional<Owner> porPhone = ownerRepository.findByPhone(ownerDTO.getPhone());
            if (porPhone.isPresent() && !porPhone.get().getId().equals(id)) {
                throw new IllegalArgumentException("Error: El teléfono ya pertenece a otro propietario.");
            }

            // 3. Actualizamos los datos
            existente.setName(ownerDTO.getName());
            existente.setLastName(ownerDTO.getLastName());
            existente.setEmail(ownerDTO.getEmail());
            existente.setPhone(ownerDTO.getPhone());

            // 4. Guardamos y devolvemos mapeado
            Owner updatedOwner = ownerRepository.save(existente);
            logger.info("Propietario actualizado con éxito (ID {})", id);
            return ownerMapper.toDTO(updatedOwner);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al actualizar: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el propietario: {}", e.getMessage());
            throw new RuntimeException("Error interno al actualizar.");
        }
    }

    // --- 5. BORRAR ---

    /**
     * Elimina a un propietario del sistema mediante su identificador.
     *
     * @param id Identificador del propietario a eliminar.
     * @throws IllegalArgumentException si no se encuentra al propietario.
     * @throws RuntimeException si ocurre un error en el proceso de borrado.
     */
    public void deleteOwner(Long id) {
        try {
            logger.info("Borrando propietario con ID {}", id);
            if (!ownerRepository.existsById(id)) {
                throw new IllegalArgumentException("Propietario no encontrado con ID: " + id);
            }
            ownerRepository.deleteById(id);
            logger.info("Propietario borrado correctamente.");

        } catch (IllegalArgumentException e) {
            logger.warn("No se pudo borrar el propietario: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al borrar el propietario: {}", e.getMessage());
            throw new RuntimeException("Error interno al borrar el propietario.");
        }
    }
}
