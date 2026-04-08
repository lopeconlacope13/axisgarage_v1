package org.iesalixar.daw2.alvarolopez.lopebnb.services;

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
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PropietarioService {

    private static final Logger logger = LoggerFactory.getLogger(PropietarioService.class);


    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private PropietarioMapper propietarioMapper;

    // --- 1. LISTAR CON PAGINACIÓN (Basado en UD06-4) ---
    public Page<PropietarioDTO> getAllPropietarios(Pageable pageable) {
        logger.info("Solicitando todos los propietarios con paginación: página {}, tamaño {}",
                pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<Propietario> propietarios = propietarioRepository.findAll(pageable);
            logger.info("Se han encontrado {} regiones en la página actual.", propietarios.getNumberOfElements());
            return propietarios.map(propietarioMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista paginada de propietarios: {}", e.getMessage());
            throw e;
        }
    }

    // --- 2. OBTENER UNO POR ID ---
    public PropietarioDTO getPropietarioById(Long id) {
        try {
            logger.info("Buscando propietario con ID {}", id);
            Optional<Propietario> propietario = propietarioRepository.findById(id);
            return propietario.map(propietarioMapper::toDTO)
                    .orElseThrow(() -> new RuntimeException("El propietario con ID " + id + " no existe."));;
        } catch (Exception e) {
            logger.error("Error al buscar el propietario con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar la región.", e);
        }
    }

    // --- 3. CREAR PROPIETARIO ---
    public PropietarioDTO createPropietario(PropietarioDTO propietarioDTO) {

        // 1. LÓGICA DE NEGOCIO: El email y el teléfono deben ser únicos en toda la BBDD
        if (propietarioRepository.findByEmail(propietarioDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Error: El email '" + propietarioDTO.getEmail() + "' ya está registrado.");
        }
        if (propietarioRepository.findByTelefono(propietarioDTO.getTelefono()).isPresent()) {
            throw new IllegalArgumentException("Error: El teléfono '" + propietarioDTO.getTelefono() + "' ya está registrado.");
        }

        // 2. Se convierte a Entity (El ID será nulo y la BBDD lo autogenerará)
        Propietario propietario = propietarioMapper.toEntity(propietarioDTO);

        // 3. Guardamos en la base de datos
        Propietario savedPropietario = propietarioRepository.save(propietario);

        // 4. Se devuelve el DTO ya con su ID generado
        return propietarioMapper.toDTO(savedPropietario);
    }

    // --- 4. ACTUALIZAR PROPIETARIO ---
    public PropietarioDTO updatePropietario(Long id, PropietarioDTO propietarioDTO) {

        // 1. Comprobamos que el propietario existe
        Propietario existente = propietarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: El propietario con ID " + id + " no existe."));

        // 2. LÓGICA DE NEGOCIO (Excluyendo al propio usuario)
        // ¿Hay alguien en la base de datos con este email QUE NO SEA el propio usuario que estamos editando?
        Optional<Propietario> porEmail = propietarioRepository.findByEmail(propietarioDTO.getEmail());
        if (porEmail.isPresent() && !porEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("Error: El email ya pertenece a otro propietario.");
        }

        // ¿Hay alguien en la base de datos con este teléfono QUE NO SEA este mismo usuario?
        Optional<Propietario> porTelefono = propietarioRepository.findByTelefono(propietarioDTO.getTelefono());
        if (porTelefono.isPresent() && !porTelefono.get().getId().equals(id)) {
            throw new IllegalArgumentException("Error: El teléfono ya pertenece a otro propietario.");
        }

        // 3. Actualizamos los datos (NUNCA el ID)
        existente.setNombre(propietarioDTO.getNombre());
        existente.setApellidos(propietarioDTO.getApellidos());
        existente.setEmail(propietarioDTO.getEmail());
        existente.setTelefono(propietarioDTO.getTelefono());

        // 4. Guardamos y devolvemos mapeado
        Propietario updatedPropietario = propietarioRepository.save(existente);
        return propietarioMapper.toDTO(updatedPropietario);
    }

    // --- 5. BORRAR ---
    public void deletePropietario(Long id) {
        if (!propietarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Propietario no encontrado con ID: " + id);
        }
        propietarioRepository.deleteById(id);
    }


}