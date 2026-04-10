package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.PropietarioDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.services.PropietarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador REST para la gestión de Propietarios en el sistema LopeBnB.
 * Permite realizar operaciones CRUD sobre los dueños de los alojamientos,
 * delegando la lógica de validación de unicidad (email y teléfono) al servicio.
 *
 * @author Alvaro Lopez
 */
@RestController
@RequestMapping("/api/propietarios")
@Tag(name = "Propietarios", description = "Operaciones CRUD para la gestión de propietarios")
public class PropietarioController {

    private static final Logger logger = LoggerFactory.getLogger(PropietarioController.class);

    @Autowired
    private PropietarioService propietarioService;

    // --- 1. LISTAR (PAGINADO) ---

    @Operation(summary = "Obtener lista paginada de propietarios", description = "Devuelve una lista paginada de todos los propietarios registrados en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de Propietarios recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PropietarioDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<PropietarioDTO>> getAllPropietarios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        try {
            Page<PropietarioDTO> propietarios = propietarioService.getAllPropietarios(nombre, email, pageable);
            return ResponseEntity.ok(propietarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER UNO POR ID ---

    @Operation(summary = "Obtener un Propietario por ID", description = "Recupera los datos de un propietario específico según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propietario encontrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PropietarioDTO.class))),
            @ApiResponse(responseCode = "404", description = "Propietario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getPropietarioById(@PathVariable Long id) {
        try {
            Optional<PropietarioDTO> propietarioDTO = propietarioService.getPropietarioById(id);
            if (propietarioDTO.isPresent()) {
                logger.info("Se ha encontrado Propietario por ID {}", id);
                return ResponseEntity.ok(propietarioDTO.get());
            } else {
                logger.info("No se encontró Propietario por ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El propietario no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al obtener Propietario por ID {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el propietario con ID " + id);
        }
    }

    // --- 3. CREAR ---

    @Operation(summary = "Crear un nuevo propietario", description = "Registra un nuevo propietario validando previamente que el email y el teléfono no estén en uso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Propietario creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PropietarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (email o teléfono duplicados)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createPropietario(@Valid @RequestBody PropietarioDTO dto) {
        try {
            logger.info("REST: Creando nuevo propietario");
            PropietarioDTO creado = propietarioService.createPropietario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al crear el propietario");
        }
    }

    // --- 4. ACTUALIZAR ---

    @Operation(summary = "Actualizar un propietario existente", description = "Modifica los datos de un propietario, asegurando que los nuevos datos de contacto no pertenezcan a otro usuario registrado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propietario actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PropietarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o generación de conflictos con otros usuarios"),
            @ApiResponse(responseCode = "404", description = "El propietario a actualizar no existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePropietario(@PathVariable Long id, @Valid @RequestBody PropietarioDTO dto) {
        try {
            logger.info("REST: Actualizando propietario con ID: {}", id);
            PropietarioDTO actualizado = propietarioService.updatePropietario(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al actualizar el propietario");
        }
    }

    // --- 5. BORRAR ---

    @Operation(summary = "Eliminar un propietario por su ID", description = "Elimina físicamente a un propietario de la base de datos a partir de su identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Propietario eliminado exitosamente (No Content)"),
            @ApiResponse(responseCode = "404", description = "El propietario a eliminar no fue encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePropietario(@PathVariable Long id) {
        try {
            logger.info("REST: Borrando propietario con ID: {}", id);
            propietarioService.deletePropietario(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al borrar el propietario");
        }
    }
}