package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.RenterDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.RenterService;
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
@RequestMapping("/api/renters")
@Tag(name = "Renters", description = "Operaciones CRUD para la gestión de los clientes/huéspedes (Renters)")
public class RenterController {

    private static final Logger logger = LoggerFactory.getLogger(RenterController.class);

    @Autowired
    private RenterService renterService;

    // --- 1. LISTAR (PAGINADO) ---

    @Operation(summary = "Obtener lista de huéspedes", description = "Devuelve una lista paginada de huéspedes. Permite filtrar opcionalmente por nombre o DNI.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RenterDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<RenterDTO>> getAllRenters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String dni,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        try {
            return ResponseEntity.ok(renterService.getAllRenters(name, dni, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER UNO POR ID ---

    @Operation(summary = "Obtener un huésped por ID", description = "Busca y devuelve los detalles de un huésped específico usando su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Huésped encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenterDTO.class))),
            @ApiResponse(responseCode = "404", description = "Huésped no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getRenterById(@PathVariable Long id) {
        try {
            Optional<RenterDTO> renterDTO = renterService.getRenterById(id);
            if (renterDTO.isPresent()) {
                return ResponseEntity.ok(renterDTO.get());
            } else {
                logger.warn("REST: No se encontró el huésped con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El huésped no existe.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el huésped.");
        }
    }

    // --- 3. CREAR HUÉSPED ---

    @Operation(summary = "Registrar un nuevo huésped", description = "Crea un nuevo huésped en el sistema validando unicidad.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Huésped creado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenterDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o duplicados"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createRenter(@Valid @RequestBody RenterDTO dto) {
        try {
            logger.info("REST: Creando nuevo huésped");
            RenterDTO created = renterService.createRenter(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al crear el huésped.");
        }
    }

    // --- 4. ACTUALIZAR HUÉSPED ---

    @Operation(summary = "Actualizar un huésped existente", description = "Modifica los datos de un huésped.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Huésped actualizado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenterDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o generación de conflictos"),
            @ApiResponse(responseCode = "404", description = "El huésped no existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRenter(@PathVariable Long id, @Valid @RequestBody RenterDTO dto) {
        try {
            logger.info("REST: Actualizando huésped con ID: {}", id);
            RenterDTO updated = renterService.updateRenter(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al actualizar el huésped.");
        }
    }

    // --- 5. BORRAR HUÉSPED ---

    @Operation(summary = "Eliminar un huésped por ID", description = "Borra físicamente a un huésped de la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Huésped eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "El huésped a eliminar no fue encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRenter(@PathVariable Long id) {
        try {
            logger.info("REST: Borrando huésped con ID: {}", id);
            renterService.deleteRenter(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al borrar el huésped.");
        }
    }
}
