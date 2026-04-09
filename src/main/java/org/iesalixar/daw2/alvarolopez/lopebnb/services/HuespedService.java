package org.iesalixar.daw2.alvarolopez.lopebnb.services;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.HuespedDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Huesped;
import org.iesalixar.daw2.alvarolopez.lopebnb.mappers.HuespedMapper;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.HuespedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio encargado de gestionar la lógica de negocio relativa a la entidad {@link Huesped}.
 * Actúa como intermediario entre el Controlador y la Base de Datos.
 * Aplica reglas de negocio estrictas para evitar duplicidades en datos críticos (DNI, Email, Teléfono)
 * y garantiza que las excepciones sean capturadas, registradas (logs) y manejadas correctamente.
 *
 * @author Alvaro Lopez
 */
@Service
public class HuespedService {

    private static final Logger logger = LoggerFactory.getLogger(HuespedService.class);

    @Autowired
    private HuespedRepository huespedRepository;

    @Autowired
    private HuespedMapper huespedMapper;

    // --- 1. LISTAR CON PAGINACIÓN ---

    /**
     * Recupera una lista paginada de todos los huéspedes registrados.
     *
     * @param pageable Objeto inyectado con los parámetros de paginación (tamaño, orden, página).
     * @return {@link Page} de {@link HuespedDTO} con los datos de los huéspedes.
     */
    public Page<HuespedDTO> getAllHuespedes(Pageable pageable) {
        try {
            logger.info("Solicitando huéspedes con paginación: página {}, tamaño {}",
                    pageable.getPageNumber(), pageable.getPageSize());

            Page<Huesped> huespedes = huespedRepository.findAll(pageable);
            logger.info("Se han encontrado {} huéspedes en la página actual.", huespedes.getNumberOfElements());

            return huespedes.map(huespedMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista paginada de huéspedes: {}", e.getMessage());
            throw new RuntimeException("Error interno al listar los huéspedes", e);
        }
    }

    // --- 2. OBTENER UNO POR ID ---

    /**
     * Busca un huésped específico por su identificador único.
     *
     * @param id Identificador del huésped a buscar.
     * @return Un {@link Optional} que contiene el {@link HuespedDTO} si se encuentra, o vacío si no existe.
     */
    public Optional<HuespedDTO> getHuespedById(Long id) {
        try {
            logger.info("Buscando huésped con ID {}", id);
            return huespedRepository.findById(id).map(huespedMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar el huésped con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar el huésped.", e);
        }
    }

    // --- 3. CREAR HUÉSPED ---

    /**
     * Registra un nuevo huésped en el sistema tras validar sus datos.
     * REGLA DE NEGOCIO: DNI, Email y Teléfono deben ser únicos.
     *
     * @param huespedDTO Objeto con los datos del huésped a crear.
     * @return El {@link HuespedDTO} del huésped recién guardado.
     * @throws IllegalArgumentException Si el DNI, email o teléfono ya existen.
     */
    public HuespedDTO createHuesped(@Valid HuespedDTO huespedDTO) {
        try {
            logger.info("Creando nuevo huésped con DNI: {}", huespedDTO.getDni());

            // Reglas de negocio: unicidad
            if (huespedRepository.findByDni(huespedDTO.getDni()).isPresent()) {
                throw new IllegalArgumentException("El DNI ya está registrado.");
            }
            if (huespedRepository.findByEmail(huespedDTO.getEmail()).isPresent()) {
                throw new IllegalArgumentException("El email ya está registrado.");
            }
            if (huespedRepository.findByTelefono(huespedDTO.getTelefono()).isPresent()) {
                throw new IllegalArgumentException("El teléfono ya está registrado.");
            }

            Huesped huesped = huespedMapper.toEntity(huespedDTO);
            Huesped savedHuesped = huespedRepository.save(huesped);

            logger.info("Huésped creado exitosamente con ID {}", savedHuesped.getId());
            return huespedMapper.toDTO(savedHuesped);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al crear huésped: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear el huésped: {}", e.getMessage());
            throw new RuntimeException("Error interno al crear el huésped.");
        }
    }

    // --- 4. ACTUALIZAR HUÉSPED ---

    /**
     * Actualiza la información de un huésped existente aplicando la "trampa del update"
     * para permitir que mantenga sus propios datos únicos sin lanzar falsos positivos.
     *
     * @param id Identificador del huésped a modificar.
     * @param huespedDTO DTO con los nuevos datos a aplicar.
     * @return El {@link HuespedDTO} actualizado.
     */
    public HuespedDTO updateHuesped(Long id, @Valid HuespedDTO huespedDTO) {
        try {
            logger.info("Actualizando huésped con ID {}", id);

            Huesped existente = huespedRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Huésped no encontrado con ID: " + id));

            // Lógica de exclusión para evitar falsos duplicados
            Optional<Huesped> porDni = huespedRepository.findByDni(huespedDTO.getDni());
            if (porDni.isPresent() && !porDni.get().getId().equals(id)) {
                throw new IllegalArgumentException("El DNI ya pertenece a otro huésped.");
            }

            Optional<Huesped> porEmail = huespedRepository.findByEmail(huespedDTO.getEmail());
            if (porEmail.isPresent() && !porEmail.get().getId().equals(id)) {
                throw new IllegalArgumentException("El email ya pertenece a otro huésped.");
            }

            Optional<Huesped> porTelefono = huespedRepository.findByTelefono(huespedDTO.getTelefono());
            if (porTelefono.isPresent() && !porTelefono.get().getId().equals(id)) {
                throw new IllegalArgumentException("El teléfono ya pertenece a otro huésped.");
            }

            existente.setNombre(huespedDTO.getNombre());
            existente.setApellidos(huespedDTO.getApellidos());
            existente.setEmail(huespedDTO.getEmail());
            existente.setDni(huespedDTO.getDni());
            existente.setTelefono(huespedDTO.getTelefono());

            Huesped updatedHuesped = huespedRepository.save(existente);
            logger.info("Huésped actualizado con éxito (ID {})", id);
            return huespedMapper.toDTO(updatedHuesped);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al actualizar huésped: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el huésped: {}", e.getMessage());
            throw new RuntimeException("Error interno al actualizar.");
        }
    }

    // --- 5. BORRAR ---

    /**
     * Elimina a un huésped del sistema.
     *
     * @param id Identificador del huésped a eliminar.
     */
    public void deleteHuesped(Long id) {
        try {
            logger.info("Borrando huésped con ID {}", id);
            if (!huespedRepository.existsById(id)) {
                throw new IllegalArgumentException("Huésped no encontrado con ID: " + id);
            }
            huespedRepository.deleteById(id);
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