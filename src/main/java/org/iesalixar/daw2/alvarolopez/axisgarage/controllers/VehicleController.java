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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Operaciones CRUD para la gestión de vehículos")
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    @Autowired
    private VehicleService vehicleService;

    // Fuente de mensajes i18n — lee de messages_en.properties o messages_es.properties
    // según el idioma detectado en la petición HTTP
    @Autowired
    private MessageSource messageSource;

    // --- 1. LISTAR ---

    @Operation(summary = "Obtener todos los Vehículos", description = "Devuelve una lista paginada de todos los vehículos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehicleDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<VehicleDTO>> getAllVehicles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer horsePower,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @PageableDefault(size = 10, sort = "model") Pageable pageable) {

        logger.info("REST: Solicitando Vehículos (search: {}, marca: {}, modelo: {}, caballos: {}, categoría: {}, localización: {}) | Pág: {}, Tamaño: {}",
                search, brand, model, horsePower, categoryId, locationId, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<VehicleDTO> vehicles = vehicleService.getAllVehicles(search, brand, model, horsePower, categoryId, locationId, pageable);
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
                // Recuperamos el mensaje del fichero properties según el idioma de la petición
                String msg = messageSource.getMessage("msg.vehicle-controller.notFound", null, LocaleContextHolder.getLocale());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
            }
        } catch (Exception e) {
            logger.error("Error al buscar el vehículo con ID {}: {}", id, e.getMessage());
            String msg = messageSource.getMessage("msg.vehicle-controller.fetch.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
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
            String msg = messageSource.getMessage("msg.vehicle-controller.insert.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
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
            String msg = messageSource.getMessage("msg.vehicle-controller.update.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

    // --- 5. BORRAR VEHÍCULO ---

    // --- 6. AÑADIR IMAGEN ---

    /**
     * Añade una imagen a la galería de un vehículo existente.
     * Solo accesible para usuarios con rol MANAGER o ADMIN.
     *
     * @param id   ID del vehículo.
     * @param file Archivo de imagen enviado como multipart.
     * @return VehicleDTO actualizado con la nueva imagen.
     */
    @PostMapping("/{id}/images")
    public ResponseEntity<?> addImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(vehicleService.addImage(id, file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al subir imagen para vehículo {}: {}", id, e.getMessage());
            String msg = messageSource.getMessage("msg.vehicle-controller.image.upload.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

    // --- 7. ELIMINAR IMAGEN ---

    /**
     * Elimina una imagen concreta de la galería de un vehículo.
     * Solo accesible para usuarios con rol MANAGER o ADMIN.
     *
     * @param id       ID del vehículo.
     * @param filename Nombre del archivo a eliminar.
     * @return VehicleDTO actualizado sin la imagen eliminada.
     */
    @DeleteMapping("/{id}/images/{filename}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id, @PathVariable String filename) {
        try {
            return ResponseEntity.ok(vehicleService.deleteImage(id, filename));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar imagen {} del vehículo {}: {}", filename, id, e.getMessage());
            String msg = messageSource.getMessage("msg.vehicle-controller.image.delete.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

    // --- 8. REORDENAR IMÁGENES ---

    /**
     * Recibe la lista de nombres de archivo en el nuevo orden y la persiste.
     * Solo accesible para usuarios con rol MANAGER o ADMIN.
     *
     * @param id        ID del vehículo.
     * @param filenames Lista de nombres en el nuevo orden (JSON array de strings).
     * @return VehicleDTO actualizado con el nuevo orden de imágenes.
     */
    @PutMapping("/{id}/images/order")
    public ResponseEntity<?> reorderImages(@PathVariable Long id, @RequestBody List<String> filenames) {
        try {
            return ResponseEntity.ok(vehicleService.reorderImages(id, filenames));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al reordenar imágenes del vehículo {}: {}", id, e.getMessage());
            String msg = messageSource.getMessage("msg.vehicle-controller.image.reorder.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

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
            String msg = messageSource.getMessage("msg.vehicle-controller.retired", null, LocaleContextHolder.getLocale());
            return ResponseEntity.ok(msg);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            String msg = messageSource.getMessage("msg.vehicle-controller.delete.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }
}
