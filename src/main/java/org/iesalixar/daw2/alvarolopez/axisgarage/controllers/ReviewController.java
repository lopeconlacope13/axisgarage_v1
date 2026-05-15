package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.ReviewDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.ReviewService;
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

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Operaciones CRUD para la gestión de reseñas de vehículos alquilados")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    // --- 1. LISTAR (PAGINADO) ---

    @Operation(summary = "Obtener todas las reseñas", description = "Devuelve una lista paginada de todas las opiniones. Permite filtrar por puntuación mínima y por el ID de la reserva.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseñas recuperadas exitosamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReviewDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<ReviewDTO>> getAllReviews(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Long reservationId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        try {
            logger.info("REST: Petición para listar opiniones (Filtros -> rating: {}, reservationId: {})",
                    rating, reservationId);
            return ResponseEntity.ok(reviewService.getAllReviews(rating, reservationId, pageable));
        } catch (Exception e) {
            logger.error("Error al listar opiniones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER UNA POR ID ---

    @Operation(summary = "Obtener una reseña por ID", description = "Busca y devuelve los detalles de una opinión concreta utilizando su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña encontrada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        try {
            logger.info("REST: Petición para obtener opinión con ID {}", id);
            Optional<ReviewDTO> dto = reviewService.getReviewById(id);
            if (dto.isPresent()) {
                return ResponseEntity.ok(dto.get());
            } else {
                logger.warn("No se encontró la opinión con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Opinión no encontrada.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar la opinión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al buscar la opinión.");
        }
    }

    // --- 3. LISTAR OPINIONES DE UNA RESERVA ---

    @Operation(summary = "Obtener opiniones de una reserva", description = "Devuelve todas las opiniones registradas para una reserva específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseñas recuperadas", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReviewDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<?> getReviewsByReservation(@PathVariable Long reservationId) {
        try {
            logger.info("REST: Petición para obtener opiniones de reserva {}", reservationId);
            List<ReviewDTO> reviews = reviewService.getReviewsByReservation(reservationId);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            logger.warn("Reserva no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al obtener opiniones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al obtener las opiniones.");
        }
    }

    // --- 3b. LISTAR OPINIONES DE UN VEHÍCULO ---

    @Operation(summary = "Obtener opiniones de un vehículo", description = "Devuelve todas las valoraciones registradas para un vehículo específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseñas recuperadas", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReviewDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> getReviewsByVehicle(@PathVariable Long vehicleId) {
        try {
            logger.info("REST: Petición para obtener opiniones del vehículo {}", vehicleId);
            List<ReviewDTO> reviews = reviewService.getReviewsByVehicle(vehicleId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("Error al obtener opiniones del vehículo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al obtener las opiniones del vehículo.");
        }
    }

    // --- 4. CREAR OPINIÓN ---

    @Operation(summary = "Crear una nueva opinión", description = "Registra una nueva opinión validando reglas de negocio.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Opinión creada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o violación de reglas de negocio"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            logger.info("REST: Petición para crear nueva opinión");
            ReviewDTO creada = reviewService.createReview(reviewDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al crear opinión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear la opinión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al publicar la opinión.");
        }
    }

    // --- 5. ACTUALIZAR OPINIÓN ---

    @Operation(summary = "Actualizar una opinión", description = "Modifica los datos de una opinión existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opinión actualizada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o violación de reglas de negocio"),
            @ApiResponse(responseCode = "404", description = "La opinión a actualizar no existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            logger.info("REST: Petición para actualizar opinión con ID {}", id);
            ReviewDTO actualizada = reviewService.updateReview(id, reviewDTO);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al actualizar opinión: {}", e.getMessage());
            if (e.getMessage().contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al actualizar la opinión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la opinión.");
        }
    }

    // --- 6. BORRAR OPINIÓN ---

    @Operation(summary = "Eliminar una opinión", description = "Elimina el registro de una opinión.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Opinión eliminada exitosamente (No Content)"),
            @ApiResponse(responseCode = "404", description = "La opinión no fue encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            logger.info("REST: Petición para borrar opinión con ID {}", id);
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Opinión no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al borrar la opinión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la opinión.");
        }
    }
}
