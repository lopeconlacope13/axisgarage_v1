package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.ReservaDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.services.ReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Controlador REST para la gestión de Reservas.
 * Actúa como punto de entrada para las peticiones HTTP relacionadas con el alquiler de casas rurales.
 * Se encarga de recibir los DTOs, delegar la lógica de negocio al servicio y devolver las
 * respuestas HTTP adecuadas estandarizadas.
 *
 * @author Alvaro Lopez
 */
@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Operaciones CRUD para la gestión de reservas de alojamientos")
public class ReservaController {

    private static final Logger logger = LoggerFactory.getLogger(ReservaController.class);

    @Autowired
    private ReservaService reservaService;

    // --- 1. LISTAR (PAGINADO) ---

    @Operation(summary = "Obtener todas las reservas", description = "Devuelve una lista paginada de todas las reservas registradas en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservas recuperadas exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReservaDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<ReservaDTO>> getAllReservas(
            @RequestParam(required = false) Long casaRuralId,
            @RequestParam(required = false) Long huespedId,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @PageableDefault(size = 10, sort = "fechaEntrada") Pageable pageable) {
        try {
            return ResponseEntity.ok(reservaService.getAllReservas(casaRuralId, huespedId, fechaDesde, fechaHasta, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER UNA POR ID ---

    @Operation(summary = "Obtener una reserva por ID", description = "Busca y devuelve los detalles de una reserva concreta utilizando su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva encontrada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservaById(@PathVariable Long id) {
        try {
            Optional<ReservaDTO> reservaDTO = reservaService.getReservaById(id);
            if (reservaDTO.isPresent()) {
                return ResponseEntity.ok(reservaDTO.get());
            } else {
                logger.warn("No se encontró la reserva con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La reserva solicitada no existe.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al buscar la reserva.");
        }
    }

    // --- 3. CREAR RESERVA ---

    @Operation(summary = "Crear una nueva reserva", description = "Registra una nueva reserva validando que las fechas sean lógicas, que la casa y el huésped existan, y que no haya solapamiento de fechas con otras reservas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, fechas ilógicas o solapamiento con otra reserva"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createReserva(@Valid @RequestBody ReservaDTO dto) {
        try {
            logger.info("REST: Petición para crear nueva reserva");
            ReservaDTO creada = reservaService.createReserva(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al crear la reserva.");
        }
    }

    // --- 4. ACTUALIZAR RESERVA ---

    @Operation(summary = "Actualizar una reserva existente", description = "Modifica los datos de una reserva existente, recalculando el precio final y verificando que las nuevas fechas no pisen otras reservas (excluyendo a sí misma de la validación).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva actualizada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o generación de conflicto de fechas"),
            @ApiResponse(responseCode = "404", description = "La reserva a actualizar no existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReserva(@PathVariable Long id, @Valid @RequestBody ReservaDTO dto) {
        try {
            logger.info("REST: Petición para actualizar reserva con ID: {}", id);
            ReservaDTO actualizada = reservaService.updateReserva(id, dto);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al actualizar la reserva.");
        }
    }

    // --- 5. BORRAR RESERVA ---

    @Operation(summary = "Cancelar y eliminar una reserva", description = "Elimina de forma física el registro de una reserva de la base de datos a partir de su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reserva eliminada exitosamente (No Content)"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para eliminar reservas"),
            @ApiResponse(responseCode = "404", description = "La reserva a eliminar no fue encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReserva(@PathVariable Long id) {
        try {
            logger.info("REST: Petición para borrar reserva con ID: {}", id);
            reservaService.deleteReserva(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al borrar la reserva.");
        }
    }
}