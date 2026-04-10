package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.OpinionDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.services.OpinionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de Opiniones.
 * Actúa como punto de entrada para las peticiones HTTP relacionadas con las opiniones de casas rurales.
 * Se encarga de recibir los DTOs, delegar la lógica de negocio al servicio y devolver las
 * respuestas HTTP adecuadas estandarizadas.
 *
 * @author Alvaro Lopez
 */
@RestController
@RequestMapping("/api/opiniones")
@Tag(name = "Opiniones", description = "Operaciones CRUD para la gestión de opiniones de casas rurales")
public class OpinionController {

    private static final Logger logger = LoggerFactory.getLogger(OpinionController.class);

    @Autowired
    private OpinionService opinionService;

    // --- 1. LISTAR (PAGINADO) ---

    /**
     * Obtiene una lista paginada de todas las opiniones registradas.
     *
     * @param pageable Parámetros de paginación (página, tamaño, ordenamiento).
     * @return Page de OpinionDTO con las opiniones.
     */
    @Operation(summary = "Obtener todas las opiniones",
            description = "Devuelve una lista paginada de todas las opiniones. Permite filtrar por puntuación mínima y por el ID de la casa rural.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opiniones recuperadas exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OpinionDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<OpinionDTO>> getAllOpiniones(
            @RequestParam(required = false) Integer puntuacionMinima,
            @RequestParam(required = false) Long casaRuralId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        try {
            logger.info("REST: Petición para listar opiniones (Filtros -> ptMin: {}, casaId: {})",
                    puntuacionMinima, casaRuralId);
            return ResponseEntity.ok(opinionService.getAllOpiniones(puntuacionMinima, casaRuralId, pageable));
        } catch (Exception e) {
            logger.error("Error al listar opiniones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER UNA POR ID ---

    /**
     * Obtiene una opinión específica por su ID.
     *
     * @param id Identificador de la opinión.
     * @return OpinionDTO si existe, o 404 si no.
     */
    @Operation(summary = "Obtener una opinión por ID",
               description = "Busca y devuelve los detalles de una opinión concreta utilizando su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opinión encontrada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OpinionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Opinión no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getOpinionById(@PathVariable Long id) {
        try {
            logger.info("REST: Petición para obtener opinión con ID {}", id);
            Optional<OpinionDTO> opinionDTO = opinionService.getOpinionById(id);
            if (opinionDTO.isPresent()) {
                return ResponseEntity.ok(opinionDTO.get());
            } else {
                logger.warn("No se encontró la opinión con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La opinión solicitada no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar la opinión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al buscar la opinión.");
        }
    }

    // --- 3. LISTAR OPINIONES DE UNA CASA ---

    /**
     * Obtiene todas las opiniones de una casa rural específica.
     *
     * @param casaRuralId Identificador de la casa rural.
     * @return Lista de OpinionDTO de esa casa.
     */
    @Operation(summary = "Obtener opiniones de una casa",
               description = "Devuelve todas las opiniones registradas para una casa rural específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opiniones recuperadas exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OpinionDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Casa rural no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/casa/{casaRuralId}")
    public ResponseEntity<?> getOpinionesByCasaRural(@PathVariable Long casaRuralId) {
        try {
            logger.info("REST: Petición para obtener opiniones de casa {}", casaRuralId);
            List<OpinionDTO> opiniones = opinionService.getOpinionesByCasaRural(casaRuralId);
            return ResponseEntity.ok(opiniones);
        } catch (IllegalArgumentException e) {
            logger.warn("Casa no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al obtener opiniones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al obtener las opiniones.");
        }
    }

    // --- 4. CREAR OPINIÓN ---

    /**
     * Crea una nueva opinión registrando la puntuación y comentario de un huésped sobre una casa.
     *
     * @param opinionDTO Datos de la opinión a crear.
     * @return OpinionDTO creada con status 201.
     */
    @Operation(summary = "Crear una nueva opinión",
               description = "Registra una nueva opinión validando que puntuación sea 1-5, que casa y huésped existan, y que no haya opinión previa del huésped en esa casa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Opinión creada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OpinionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o violación de reglas de negocio"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para crear opiniones (solo huéspedes autenticados)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createOpinion(@Valid @RequestBody OpinionDTO opinionDTO) {
        try {
            logger.info("REST: Petición para crear nueva opinión");
            OpinionDTO creada = opinionService.createOpinion(opinionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al crear opinión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear la opinión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al crear la opinión.");
        }
    }

    // --- 5. ACTUALIZAR OPINIÓN ---

    /**
     * Actualiza una opinión existente.
     *
     * @param id Identificador de la opinión.
     * @param opinionDTO Nuevos datos de la opinión.
     * @return OpinionDTO actualizada.
     */
    @Operation(summary = "Actualizar una opinión existente",
               description = "Modifica los datos de una opinión existente, validando que la nueva configuración no viole reglas de unicidad.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opinión actualizada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OpinionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o violación de reglas de negocio"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para actualizar opiniones"),
            @ApiResponse(responseCode = "404", description = "La opinión a actualizar no existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOpinion(@PathVariable Long id, @Valid @RequestBody OpinionDTO opinionDTO) {
        try {
            logger.info("REST: Petición para actualizar opinión con ID {}", id);
            OpinionDTO actualizada = opinionService.updateOpinion(id, opinionDTO);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al actualizar opinión: {}", e.getMessage());
            if (e.getMessage().contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al actualizar la opinión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al actualizar la opinión.");
        }
    }

    // --- 6. BORRAR OPINIÓN ---

    /**
     * Elimina una opinión de la base de datos.
     *
     * @param id Identificador de la opinión a eliminar.
     * @return 204 No Content si se elimina correctamente.
     */
    @Operation(summary = "Cancelar y eliminar una opinión",
               description = "Elimina de forma física el registro de una opinión de la base de datos a partir de su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Opinión eliminada exitosamente (No Content)"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para eliminar opiniones"),
            @ApiResponse(responseCode = "404", description = "La opinión a eliminar no fue encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOpinion(@PathVariable Long id) {
        try {
            logger.info("REST: Petición para borrar opinión con ID {}", id);
            opinionService.deleteOpinion(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Opinión no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al borrar la opinión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al borrar la opinión.");
        }
    }
}