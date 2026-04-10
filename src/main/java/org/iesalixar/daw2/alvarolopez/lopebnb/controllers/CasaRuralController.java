package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.CasaRuralDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.OpinionDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.services.CasaRuralService;
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
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador REST para la gestión de Casas Rurales en el sistema LopeBnB.
 * Proporciona los endpoints necesarios para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * sobre la entidad CasaRural, incluyendo el manejo de imágenes mediante multipart/form-data.
 */
@RestController
@RequestMapping("/api/casas")
@Tag(name = "Casa Rural", description = "Operaciones CRUD para la gestión de casas rurales")
public class CasaRuralController {

    private static final Logger logger = LoggerFactory.getLogger(CasaRuralController.class);

    @Autowired
    private CasaRuralService casaRuralService;

    // --- 1. LISTAR ---

    /**
     * Lista todas las casas rurales almacenadas en la base de datos de forma paginada.
     *
     * @param pageable Objeto Pageable inyectado por Spring con la configuración de la página (tamaño, orden, número).
     * @return ResponseEntity conteniendo la página de CasaRuralDTO o un código de error HTTP 500 en caso de fallo.
     */
    @Operation(summary = "Obtener todas las Casas Rurales", description = "Devuelve una lista paginada de todas las Casas Rurales disponibles. Permite filtrar opcionalmente por nombre o por capacidad mínima de personas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de Casas Rurales recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CasaRuralDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<CasaRuralDTO>> getAllCasaRural(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long capacidad,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {

        logger.info("REST: Solicitando Casas Rurales (Filtros -> nombre: {}, capacidad: {}) | Pág: {}, Tamaño: {}",
                nombre, capacidad, pageable.getPageNumber(), pageable.getPageSize());

        try {
            // Le pasamos los nuevos parámetros al servicio
            Page<CasaRuralDTO> casasRurales = casaRuralService.getAllCasasRurales(nombre, capacidad, pageable);
            return ResponseEntity.ok(casasRurales);
        } catch (Exception e) {
            logger.error("Error al listar las Casas Rurales: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 2. OBTENER POR ID ---

    /**
     * Obtiene los detalles de una casa rural específica utilizando su ID.
     *
     * @param id ID único de la casa rural solicitada.
     * @return ResponseEntity con el objeto CasaRuralDTO si se encuentra, o un mensaje de error HTTP 404/500 si falla.
     */
    @Operation(summary = "Obtener una Casa Rural por ID", description = "Recupera una casa rural específica según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Casa Rural encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CasaRuralDTO.class))),
            @ApiResponse(responseCode = "404", description = "Casa Rural no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getCasaRuralById(@PathVariable Long id) {
        logger.info("Buscando casa rural con ID {}", id);
        try {
            Optional<CasaRuralDTO> casaRuralDTO = casaRuralService.getCasaRuralById(id);

            if (casaRuralDTO.isPresent()) {
                logger.info("Casa Rural con ID {} encontrada.", id);
                return ResponseEntity.ok(casaRuralDTO.get());
            } else {
                logger.warn("No se encontró ninguna casa rural con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La casa rural no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar la casa rural con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar la casa rural con ID " + id);
        }
    }

    // --- 3. CREAR CASA RURAL ---

    /**
     * Inserta una nueva casa rural en la base de datos, procesando también la imagen asociada si se proporciona.
     *
     * @param casaRuralDTO Objeto DTO que contiene los datos de la casa rural y el archivo de imagen adjunto.
     * @param locale       Idioma de la petición para la internacionalización de los mensajes de error.
     * @return ResponseEntity con la casa rural creada (HTTP 201) o un mensaje de error si no se superan las validaciones.
     */
    @Operation(summary = "Crear una nueva casa rural", description = "Permite registrar una nueva casa rural en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Casa rural creada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CasaRuralDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos proporcionados (Ej. Nombre duplicado)"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para crear casas (solo propietarios)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor o error al guardar la imagen")
    })
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createCasaRural(@Valid @ModelAttribute CasaRuralDTO casaRuralDTO, Locale locale) {
        logger.info("Insertando nueva casa rural con nombre {}", casaRuralDTO.getNombre());
        try {
            CasaRuralDTO createdCasaRural = casaRuralService.createCasaRural(casaRuralDTO, locale);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCasaRural);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear la casa rural: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error al guardar la imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la imagen.");
        } catch (Exception e) {
            logger.error("Error inesperado al crear la casa rural: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear la casa rural.");
        }
    }

    // --- 4. ACTUALIZAR CASA RURAL ---

    /**
     * Actualiza los datos de una casa rural existente, incluyendo la sustitución de la imagen si se envía una nueva.
     *
     * @param id           ID de la casa rural a actualizar.
     * @param casaRuralDTO Objeto DTO con los nuevos datos y el nuevo archivo de imagen (opcional).
     * @param locale       Idioma de la petición para la internacionalización de los mensajes de error.
     * @return ResponseEntity con la casa rural actualizada (HTTP 200) o un mensaje de error.
     */
    @Operation(summary = "Actualizar una casa rural", description = "Permite actualizar los datos de una casa rural existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Casa rural actualizada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CasaRuralDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (Ej. Nombre duplicado en otra casa)"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para actualizar casas (solo propietarios)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateCasaRural(
            @PathVariable Long id, @Valid @ModelAttribute CasaRuralDTO casaRuralDTO, Locale locale) {

        logger.info("Actualizando casa rural con ID {}", id);
        try {
            CasaRuralDTO updatedCasaRural = casaRuralService.updateCasaRural(id, casaRuralDTO, locale);
            return ResponseEntity.ok(updatedCasaRural);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error al guardar la imagen para la casa rural con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la imagen.");
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar la casa rural con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la casa rural.");
        }
    }

    // --- 5. BORRAR CASA RURAL ---

    /**
     * Elimina una casa rural específica de la base de datos por su ID.
     *
     * @param id ID de la casa rural a eliminar.
     * @return ResponseEntity indicando el éxito de la operación (HTTP 200) o error si no se encuentra (HTTP 404).
     */
    @Operation(summary = "Eliminar una casa rural", description = "Permite eliminar una casa rural específica de la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Casa rural eliminada exitosamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para eliminar casas (solo propietarios)"),
            @ApiResponse(responseCode = "404", description = "Casa rural no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCasaRural(@PathVariable Long id) {
        logger.info("Eliminando casa rural con ID {}", id);
        try {
            casaRuralService.deleteCasaRural(id);
            return ResponseEntity.ok("Casa rural eliminada con éxito.");
        } catch (IllegalArgumentException e) {
            logger.warn("Error al eliminar la casa rural con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar la casa rural con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la casa rural");
        }
    }

    // --- 6. OBTENER OPINIONES DE UNA CASA RURAL ---

    /**
     * Devuelve la lista de opiniones de una casa rural concreta.
     * Útil para vistas en Angular que requieren "listCasasAndOpiniones".
     */
    @Operation(summary = "Obtener opiniones de una casa rural", description = "Devuelve todas las opiniones y puntuaciones asociadas a una casa específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opiniones recuperadas exitosamente"),
            @ApiResponse(responseCode = "404", description = "Casa rural no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}/opiniones")
    public ResponseEntity<?> getOpinionesDeCasa(@PathVariable Long id) {
        logger.info("REST: Buscando opiniones para la casa rural con ID {}", id);
        try {
            List<OpinionDTO> opiniones = casaRuralService.getCasaRuralListOpiniones(id);
            return ResponseEntity.ok(opiniones);
        } catch (IllegalArgumentException e) {
            logger.warn("No se pudo obtener las opiniones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al obtener las opiniones de la casa con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener las opiniones.");
        }
    }
}