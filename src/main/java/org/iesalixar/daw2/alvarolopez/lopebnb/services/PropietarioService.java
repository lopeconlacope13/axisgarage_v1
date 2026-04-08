package org.iesalixar.daw2.alvarolopez.lopebnb.services;

import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.PropietarioDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.PropietarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PropietarioService {

    @Autowired
    private PropietarioRepository propietarioRepository;

    // --- 1. LISTAR CON PAGINACIÓN (Basado en UD06-4) ---
    public Page<PropietarioDTO> getAllPropietarios(Pageable pageable) {
        Page<Propietario> propietariosPage = propietarioRepository.findAll(pageable);
        // Transformamos cada Entity de la página en un DTO
        return propietariosPage.map(this::toDTO);
    }

    // --- 2. OBTENER UNO POR ID ---
    public PropietarioDTO getPropietarioById(Long id) {
        Propietario propietario = propietarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado con ID: " + id));
        return toDTO(propietario);
    }

    // --- 3. CREAR (Aquí movemos tu lógica del controlador antiguo) ---
    public PropietarioDTO createPropietario(PropietarioDTO dto) {
        // Validaciones que antes tenías en el Controller
        if (propietarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya está en uso.");
        }
        if (propietarioRepository.findByTelefono(dto.getTelefono()).isPresent()) {
            throw new IllegalArgumentException("El teléfono ya está en uso.");
        }

        Propietario propietario = toEntity(dto);
        Propietario saved = propietarioRepository.save(propietario);
        return toDTO(saved);
    }

    // --- 4. ACTUALIZAR ---
    public PropietarioDTO updatePropietario(Long id, PropietarioDTO dto) {
        Propietario existente = propietarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado con ID: " + id));

        // Actualizamos los datos
        existente.setNombre(dto.getNombre());
        existente.setApellidos(dto.getApellidos());

        // Cuidado al actualizar email/telefono: comprobar que no choquen con OTRO usuario
        Optional<Propietario> porEmail = propietarioRepository.findByEmail(dto.getEmail());
        if (porEmail.isPresent() && !porEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("El email ya pertenece a otro propietario.");
        }
        existente.setEmail(dto.getEmail());

        Optional<Propietario> porTelefono = propietarioRepository.findByTelefono(dto.getTelefono());
        if (porTelefono.isPresent() && !porTelefono.get().getId().equals(id)) {
            throw new IllegalArgumentException("El teléfono ya pertenece a otro propietario.");
        }
        existente.setTelefono(dto.getTelefono());

        return toDTO(propietarioRepository.save(existente));
    }

    // --- 5. BORRAR ---
    public void deletePropietario(Long id) {
        if (!propietarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Propietario no encontrado con ID: " + id);
        }
        propietarioRepository.deleteById(id);
    }

    // --- MÉTODOS PRIVADOS PARA MAPEAR (De Entity a DTO y viceversa) ---
    private PropietarioDTO toDTO(Propietario entity) {
        PropietarioDTO dto = new PropietarioDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setApellidos(entity.getApellidos());
        dto.setEmail(entity.getEmail());
        dto.setTelefono(entity.getTelefono());
        return dto;
    }

    private Propietario toEntity(PropietarioDTO dto) {
        Propietario entity = new Propietario();
        // No se mapea el ID aquí porque al crear es autogenerado
        entity.setNombre(dto.getNombre());
        entity.setApellidos(dto.getApellidos());
        entity.setEmail(dto.getEmail());
        entity.setTelefono(dto.getTelefono());
        return entity;
    }
}