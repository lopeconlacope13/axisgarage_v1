package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.CasaRuralDTO;
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
import java.util.OptionalInt;

@RestController
@RequestMapping("/api/propietarios")
@Tag(name = "Propietarios", description = "Operaciones CRUD para la gestión de propietarios")
public class PropietarioController {

    private static final Logger logger = LoggerFactory.getLogger(PropietarioController.class);

    @Autowired
    private PropietarioService propietarioService;

    // --- 1. LISTAR (PAGINADO) ---
    @Operation(summary = "Obtener lista paginada de propietarios", description = "Devuelve una lista paginada de todos los propietarios de Casas Rurales disponibles en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de Propietarios recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CasaRuralDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<PropietarioDTO>> getAllPropietarios(
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        logger.info("Solicitando todos los propietarios con paginación: página {}, tamaño {}",
                pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<PropietarioDTO> propietarios = propietarioService.getAllPropietarios(pageable);
            logger.info("Se han encontrado {} propietarios en la página actual.", propietarios.getNumberOfElements());
            return ResponseEntity.ok(propietarios);
        } catch (Exception e) {
            logger.error("Error al obtener la lista paginada de propietarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER UNO POR ID ---
    @Operation(summary = "Obtener un Propietario por ID", description = "Recupera un propietario específico según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Casa Rural encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CasaRuralDTO.class))),
            @ApiResponse(responseCode = "404", description = "Propietario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getPropietarioById(@PathVariable Long id) {
        try {
            Optional<PropietarioDTO> propietarioDTO = propietarioService.getPropietarioById(id);
            if (propietarioDTO.isPresent()) {
                logger.info("Se han encontrado Propietario por ID {}", id);
                return ResponseEntity.ok(propietarioDTO.get());
            }else  {
                logger.info("No se encontro Propietario por ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El propietario no existe. ");
            }
        } catch (Exception e) {
            logger.error("Error al obtener Propietario por ID {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el propietario con ID" + id);
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