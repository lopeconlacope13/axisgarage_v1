package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.ReservationDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Renter;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.RenterRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.ReservationService;
import org.iesalixar.daw2.alvarolopez.axisgarage.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Operaciones CRUD para la gestión de reservas")
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RenterRepository renterRepository;

    // --- 1. LISTAR (PAGINADO) ---

    @Operation(summary = "Obtener todas las reservas", description = "Devuelve una lista paginada de todas las reservas registradas en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservas recuperadas exitosamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReservationDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<ReservationDTO>> getAllReservations(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long renterId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @PageableDefault(size = 10, sort = "startDate") Pageable pageable) {
        try {
            return ResponseEntity
                    .ok(reservationService.getAllReservations(vehicleId, renterId, startDate, endDate, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER UNA POR ID ---

    @Operation(summary = "Obtener una reserva por ID", description = "Busca y devuelve los detalles de una reserva concreta utilizando su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva encontrada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Long id) {
        try {
            Optional<ReservationDTO> dto = reservationService.getReservationById(id);
            if (dto.isPresent()) {
                return ResponseEntity.ok(dto.get());
            } else {
                logger.warn("No se encontró la reserva con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La reserva solicitada no existe.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al buscar la reserva.");
        }
    }

    // --- 3. CREAR RESERVA ---

    @Operation(summary = "Crear una nueva reserva", description = "Registra una nueva reserva.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, fechas ilógicas o solapamiento con otra reserva"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody ReservationDTO dto,
                                                  @RequestHeader("Authorization") String tokenHeader) {
        try {
            logger.info("REST: Petición para crear nueva reserva");

            // Seguridad: extraemos el email del JWT y verificamos que el renterId
            // enviado pertenece al usuario autenticado. Esto evita que un atacante
            // cree reservas suplantando a otro cliente.
            String token = tokenHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);
            Renter renter = renterRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró un perfil de cliente asociado a este usuario."));

            // Usamos 403 FORBIDDEN (no 500) porque el error no es un fallo del servidor:
            // es una acción deliberadamente no autorizada — el renterId del cuerpo
            // no coincide con el usuario que firmó el JWT.
            if (!renter.getId().equals(dto.getRenterId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El renterId no coincide con el usuario autenticado");
            }

            ReservationDTO creada = reservationService.createReservation(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear reserva: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al crear la reserva.");
        }
    }

    // --- 4. ACTUALIZAR RESERVA ---

    @Operation(summary = "Actualizar una reserva existente", description = "Modifica los datos de una reserva existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva actualizada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o generación de conflicto de fechas"),
            @ApiResponse(responseCode = "404", description = "La reserva no existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @Valid @RequestBody ReservationDTO dto) {
        try {
            logger.info("REST: Petición para actualizar reserva con ID: {}", id);
            ReservationDTO actualizada = reservationService.updateReservation(id, dto);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al actualizar la reserva.");
        }
    }

    // --- 5. BORRAR RESERVA ---

    @Operation(summary = "Eliminar una reserva", description = "Elimina de forma física el registro de una reserva.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reserva eliminada exitosamente (No Content)"),
            @ApiResponse(responseCode = "404", description = "La reserva a eliminar no fue encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long id) {
        try {
            logger.info("REST: Petición para borrar reserva con ID: {}", id);
            reservationService.deleteReservation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al borrar la reserva.");
        }
    }
}
