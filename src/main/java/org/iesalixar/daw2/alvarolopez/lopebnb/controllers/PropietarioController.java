package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.PropietarioDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.services.PropietarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/propietarios")
@Tag(name = "Propietarios", description = "Operaciones CRUD para la gestión de propietarios")
public class PropietarioController {

    private static final Logger logger = LoggerFactory.getLogger(PropietarioController.class);

    @Autowired
    private PropietarioService propietarioService;

    // --- 1. LISTAR (PAGINADO) ---
    @Operation(summary = "Obtener lista paginada de propietarios")
    @GetMapping
    public ResponseEntity<Page<PropietarioDTO>> getAllPropietarios(Pageable pageable) {
        logger.info("REST: Solicitando todos los propietarios (paginado)");
        Page<PropietarioDTO> propietarios = propietarioService.getAllPropietarios(pageable);
        return ResponseEntity.ok(propietarios);
    }

    // --- 2. OBTENER UNO POR ID ---
    @Operation(summary = "Obtener un propietario por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propietario encontrado"),
            @ApiResponse(responseCode = "404", description = "Propietario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getPropietarioById(@PathVariable Long id) {
        try {
            PropietarioDTO dto = propietarioService.getPropietarioById(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // --- 3. CREAR ---
    @Operation(summary = "Crear un nuevo propietario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (email o teléfono duplicados)")
    })
    @PostMapping
    // @RequestBody indica que los datos vienen en el body del JSON, ya no en el Model de Thymeleaf
    public ResponseEntity<?> createPropietario(@RequestBody PropietarioDTO dto) {
        try {
            logger.info("REST: Creando nuevo propietario");
            PropietarioDTO creado = propietarioService.createPropietario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // --- 4. ACTUALIZAR ---
    @Operation(summary = "Actualizar un propietario existente")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePropietario(@PathVariable Long id, @RequestBody PropietarioDTO dto) {
        try {
            logger.info("REST: Actualizando propietario con ID: {}", id);
            PropietarioDTO actualizado = propietarioService.updatePropietario(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // --- 5. BORRAR ---
    @Operation(summary = "Eliminar un propietario por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePropietario(@PathVariable Long id) {
        try {
            logger.info("REST: Borrando propietario con ID: {}", id);
            propietarioService.deletePropietario(id);
            return ResponseEntity.noContent().build(); // Devuelve 204 No Content
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}