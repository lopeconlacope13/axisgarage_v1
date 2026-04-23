package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.OwnerDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.OwnerService;
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

@RestController
@RequestMapping("/api/owners")
@Tag(name = "Owners", description = "Operaciones CRUD para la gestión de propietarios (Owners)")
public class OwnerController {

    private static final Logger logger = LoggerFactory.getLogger(OwnerController.class);

    @Autowired
    private OwnerService ownerService;

    // --- 1. LISTAR (PAGINADO) ---

    @Operation(summary = "Obtener lista paginada de propietarios", description = "Devuelve una lista paginada de todos los propietarios registrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OwnerDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<OwnerDTO>> getAllOwners(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        try {
            Page<OwnerDTO> owners = ownerService.getAllOwners(name, email, pageable);
            return ResponseEntity.ok(owners);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER UNO POR ID ---

    @Operation(summary = "Obtener un Propietario por ID", description = "Recupera los datos de un propietario específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propietario encontrado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OwnerDTO.class))),
            @ApiResponse(responseCode = "404", description = "Propietario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getOwnerById(@PathVariable Long id) {
        try {
            Optional<OwnerDTO> ownerDTO = ownerService.getOwnerById(id);
            if (ownerDTO.isPresent()) {
                logger.info("Se ha encontrado Propietario por ID {}", id);
                return ResponseEntity.ok(ownerDTO.get());
            } else {
                logger.info("No se encontró Propietario por ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El propietario no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al obtener Propietario por ID {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al buscar el propietario con ID " + id);
        }
    }

    // --- 3. CREAR ---

    @Operation(summary = "Crear un nuevo propietario", description = "Registra un nuevo propietario validando unicidad de email y teléfono.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Propietario creado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OwnerDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (email o teléfono duplicados)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createOwner(@Valid @RequestBody OwnerDTO dto) {
        try {
            logger.info("REST: Creando nuevo propietario");
            OwnerDTO created = ownerService.createOwner(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al crear el propietario");
        }
    }

    // --- 4. ACTUALIZAR ---

    @Operation(summary = "Actualizar un propietario existente", description = "Modifica los datos de un propietario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propietario actualizado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OwnerDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o generación de conflictos"),
            @ApiResponse(responseCode = "404", description = "El propietario no existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOwner(@PathVariable Long id, @Valid @RequestBody OwnerDTO dto) {
        try {
            logger.info("REST: Actualizando propietario con ID: {}", id);
            OwnerDTO updated = ownerService.updateOwner(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al actualizar el propietario");
        }
    }

    // --- 5. BORRAR ---

    @Operation(summary = "Eliminar un propietario por su ID", description = "Elimina físicamente a un propietario de la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Propietario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "El propietario a eliminar no fue encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOwner(@PathVariable Long id) {
        try {
            logger.info("REST: Borrando propietario con ID: {}", id);
            ownerService.deleteOwner(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al borrar el propietario");
        }
    }
}
