package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.VehicleDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Operaciones CRUD para la gestión de vehículos")
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    @Autowired
    private VehicleService vehicleService;

    // --- 1. LISTAR ---

    @Operation(summary = "Obtener todos los Vehículos", description = "Devuelve una lista paginada de todos los vehículos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehicleDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<VehicleDTO>> getAllVehicles(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer horsePower,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "model") Pageable pageable) {

        logger.info("REST: Solicitando Vehículos (Filtros -> marca: {}, modelo: {}, caballos: {}, categoría: {}) | Pág: {}, Tamaño: {}",
                brand, model, horsePower, categoryId, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<VehicleDTO> vehicles = vehicleService.getAllVehicles(brand, model, horsePower, categoryId, pageable);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            logger.error("Error al listar los Vehículos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER POR ID ---

    @Operation(summary = "Obtener un Vehículo por ID", description = "Recupera la ficha técnica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicleById(@PathVariable Long id) {
        logger.info("Buscando vehículo con ID {}", id);
        try {
            Optional<VehicleDTO> vehicleDTO = vehicleService.getVehicleById(id);

            if (vehicleDTO.isPresent()) {
                return ResponseEntity.ok(vehicleDTO.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El vehículo no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar el vehículo con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el vehículo.");
        }
    }

    // --- 3. CREAR VEHÍCULO ---

    @Operation(summary = "Crear un nuevo vehículo", description = "Registra un nuevo coche en la plataforma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos técnicos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createVehicle(@Valid @ModelAttribute VehicleDTO vehicleDTO, Locale locale) {
        logger.info("Creando nuevo vehículo: {}", vehicleDTO.getModel());
        try {
            VehicleDTO createdVehicle = vehicleService.createVehicle(vehicleDTO, locale);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicle);
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al crear el vehículo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al publicar el vehículo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el vehículo.");
        }
    }

    // --- 4. ACTUALIZAR VEHÍCULO ---

    @Operation(summary = "Actualizar ficha de vehículo", description = "Actualiza los datos de un vehículo existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateVehicle(
            @PathVariable Long id, @Valid @ModelAttribute VehicleDTO vehicleDTO, Locale locale) {

        logger.info("Actualizando vehículo con ID {}", id);
        try {
            VehicleDTO updatedVehicle = vehicleService.updateVehicle(id, vehicleDTO, locale);
            return ResponseEntity.ok(updatedVehicle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el vehículo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el vehículo.");
        }
    }

    // --- 5. BORRAR VEHÍCULO ---

    @Operation(summary = "Alternar disponibilidad del vehículo", description = "Cambia available de true a false o viceversa. Sin necesidad de FormData.")
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(vehicleService.toggleAvailability(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Eliminar un vehículo", description = "Elimina un vehículo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo retirado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        logger.info("Retirando vehículo con ID {}", id);
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok("Vehículo retirado con éxito.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error crítico al eliminar.");
        }
    }
}
