package org.iesalixar.daw2.alvarolopez.lopebnb.services;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.PropietarioDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
import org.iesalixar.daw2.alvarolopez.lopebnb.mappers.PropietarioMapper;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.PropietarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PropietarioService {

    private static final Logger logger = LoggerFactory.getLogger(PropietarioService.class);

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private PropietarioMapper propietarioMapper;

    // --- 1. LISTAR CON PAGINACIÓN ---
    public Page<PropietarioDTO> getAllPropietarios(Pageable pageable) {
        try {
            logger.info("Solicitando todos los propietarios con paginación: página {}, tamaño {}",
                    pageable.getPageNumber(), pageable.getPageSize());

            Page<Propietario> propietarios = propietarioRepository.findAll(pageable);
            logger.info("Se han encontrado {} propietarios en la página actual.", propietarios.getNumberOfElements());

            return propietarios.map(propietarioMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista paginada de propietarios: {}", e.getMessage());
            throw new RuntimeException("Error interno al listar los propietarios", e);
        }
    }

    // --- 2. OBTENER UNO POR ID ---
    public Optional<PropietarioDTO> getPropietarioById(Long id) {
        try {
            logger.info("Buscando propietario con ID {}", id);
            return propietarioRepository.findById(id).map(propietarioMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar el propietario con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar el propietario.", e);
        }
    }

    // --- 3. CREAR PROPIETARIO ---
    public PropietarioDTO createPropietario(@Valid PropietarioDTO propietarioDTO) {
        try {
            logger.info("Creando nuevo propietario con email: {}", propietarioDTO.getEmail());

            // Comprobamos que el email y el teléfono deben ser únicos
            if (propietarioRepository.findByEmail(propietarioDTO.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Error: El email '" + propietarioDTO.getEmail() + "' ya está registrado.");
            }
            if (propietarioRepository.findByTelefono(propietarioDTO.getTelefono()).isPresent()) {
                throw new IllegalArgumentException("Error: El teléfono '" + propietarioDTO.getTelefono() + "' ya está registrado.");
            }

            Propietario propietario = propietarioMapper.toEntity(propietarioDTO);
            Propietario savedPropietario = propietarioRepository.save(propietario);

            logger.info("Propietario creado exitosamente con ID {}", savedPropietario.getId());
            return propietarioMapper.toDTO(savedPropietario);

        } catch (IllegalArgumentException e) {
            // Si es un error de los que hemos lanzado nosotros (email duplicado, etc)
            logger.warn("Validación fallida al crear: {}", e.getMessage());
            throw e; // Lo relanzamos para que el Controlador devuelva un HTTP 400
        } catch (Exception e) {
            // Si es un error grave de base de datos
            logger.error("Error inesperado al crear el propietario: {}", e.getMessage());
            throw new RuntimeException("Error interno al crear el propietario.");
        }
    }

    // --- 4. ACTUALIZAR PROPIETARIO ---
    public PropietarioDTO updatePropietario(Long id, @Valid PropietarioDTO propietarioDTO) {
        try {
            logger.info("Actualizando propietario con ID {}", id);

            // 1. Comprobamos que el propietario existe
            Propietario existente = propietarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Error: El propietario con ID " + id + " no existe."));

            // 2. LÓGICA DE NEGOCIO (Excluyendo al propio usuario)
            Optional<Propietario> porEmail = propietarioRepository.findByEmail(propietarioDTO.getEmail());
            if (porEmail.isPresent() && !porEmail.get().getId().equals(id)) {
                throw new IllegalArgumentException("Error: El email ya pertenece a otro propietario.");
            }

            Optional<Propietario> porTelefono = propietarioRepository.findByTelefono(propietarioDTO.getTelefono());
            if (porTelefono.isPresent() && !porTelefono.get().getId().equals(id)) {
                throw new IllegalArgumentException("Error: El teléfono ya pertenece a otro propietario.");
            }

            // 3. Actualizamos los datos
            existente.setNombre(propietarioDTO.getNombre());
            existente.setApellidos(propietarioDTO.getApellidos());
            existente.setEmail(propietarioDTO.getEmail());
            existente.setTelefono(propietarioDTO.getTelefono());

            // 4. Guardamos y devolvemos mapeado
            Propietario updatedPropietario = propietarioRepository.save(existente);
            logger.info("Propietario actualizado con éxito (ID {})", id);
            return propietarioMapper.toDTO(updatedPropietario);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al actualizar: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el propietario: {}", e.getMessage());
            throw new RuntimeException("Error interno al actualizar.");
        }
    }

    // --- 5. BORRAR ---
    public void deletePropietario(Long id) {
        try {
            logger.info("Borrando propietario con ID {}", id);
            if (!propietarioRepository.existsById(id)) {
                throw new IllegalArgumentException("Propietario no encontrado con ID: " + id);
            }
            propietarioRepository.deleteById(id);
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