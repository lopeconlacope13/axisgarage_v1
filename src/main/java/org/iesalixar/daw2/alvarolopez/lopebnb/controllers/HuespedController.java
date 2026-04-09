package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.HuespedDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.services.HuespedService;
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
 * Controlador REST para la gestión de Huéspedes en el sistema LopeBnB.
 * * Este componente reemplaza al antiguo controlador MVC basado en vistas (Thymeleaf),
 * actuando ahora como una API puramente de datos que recibe y devuelve objetos JSON.
 * Delega toda la lógica de validación de duplicados (DNI, Email, Teléfono) al {@link HuespedService}.
 * * @author Alvaro Lopez
 */
@RestController
@RequestMapping("/api/huespedes")
@Tag(name = "Huéspedes", description = "Operaciones CRUD para la gestión de los clientes/huéspedes")
public class HuespedController {

    private static final Logger logger = LoggerFactory.getLogger(HuespedController.class);

    @Autowired
    private HuespedService huespedService;

    // --- 1. LISTAR (PAGINADO) ---

    @Operation(summary = "Obtener lista de huéspedes", description = "Devuelve una lista paginada de todos los huéspedes disponibles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = HuespedDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<HuespedDTO>> getAllHuespedes(
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        try {
            return ResponseEntity.ok(huespedService.getAllHuespedes(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER UNO POR ID ---

    @Operation(summary = "Obtener un huésped por ID", description = "Busca y devuelve los detalles de un huésped específico usando su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Huésped encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HuespedDTO.class))),
            @ApiResponse(responseCode = "404", description = "Huésped no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getHuespedById(@PathVariable Long id) {
        try {
            Optional<HuespedDTO> huespedDTO = huespedService.getHuespedById(id);
            if (huespedDTO.isPresent()) {
                return ResponseEntity.ok(huespedDTO.get());
            } else {
                logger.warn("REST: No se encontró el huésped con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El huésped no existe.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el huésped.");
        }
    }

    // --- 3. CREAR HUÉSPED ---

    @Operation(summary = "Registrar un nuevo huésped", description = "Crea un nuevo huésped en el sistema validando que el DNI, email y teléfono no existan previamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Huésped creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HuespedDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o duplicados (DNI, Email o Teléfono ya en uso)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor al crear el huésped")
    })
    @PostMapping
    public ResponseEntity<?> createHuesped(@Valid @RequestBody HuespedDTO dto) {
        try {
            logger.info("REST: Creando nuevo huésped");
            HuespedDTO creado = huespedService.createHuesped(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al crear el huésped.");
        }
    }

    // --- 4. ACTUALIZAR HUÉSPED ---

    @Operation(summary = "Actualizar un huésped existente", description = "Modifica los datos de un huésped comprobando que los nuevos datos de contacto no pertenezcan a otro usuario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Huésped actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HuespedDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o generación de conflictos con otros usuarios"),
            @ApiResponse(responseCode = "404", description = "El huésped a actualizar no existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHuesped(@PathVariable Long id, @Valid @RequestBody HuespedDTO dto) {
        try {
            logger.info("REST: Actualizando huésped con ID: {}", id);
            HuespedDTO actualizado = huespedService.updateHuesped(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al actualizar el huésped.");
        }
    }

    // --- 5. BORRAR HUÉSPED ---

    @Operation(summary = "Eliminar un huésped por ID", description = "Borra físicamente a un huésped de la base de datos a partir de su identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Huésped eliminado exitosamente (No Content)"),
            @ApiResponse(responseCode = "404", description = "El huésped a eliminar no fue encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHuesped(@PathVariable Long id) {
        try {
            logger.info("REST: Borrando huésped con ID: {}", id);
            huespedService.deleteHuesped(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al borrar el huésped.");
        }
    }
}