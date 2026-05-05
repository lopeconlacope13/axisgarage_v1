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
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.RenterMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.RenterRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.RenterService;
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

import java.util.Optional;

/**
 * Controlador REST para la gestión de Renters (clientes que alquilan vehículos).
 * Proporciona operaciones CRUD completas y un endpoint especial {@code /ensure}
 * que crea o recupera el perfil de cliente del usuario autenticado.
 */
@RestController
@RequestMapping("/api/renters")
@Tag(name = "Renters", description = "Operaciones CRUD para la gestión de los clientes/huéspedes (Renters)")
public class RenterController {

    // ── Constantes ────────────────────────────────────────────────────────────
    /** Prefijo estándar Bearer que se elimina de la cabecera Authorization para obtener el token puro. */
    private static final String BEARER_PREFIX = "Bearer ";

    // ── Dependencias ─────────────────────────────────────────────────────────
    private static final Logger logger = LoggerFactory.getLogger(RenterController.class);

    @Autowired
    private RenterService renterService;

    // Acceso directo al repositorio para la búsqueda por email (reutiliza findByEmail ya existente)
    @Autowired
    private RenterRepository renterRepository;

    @Autowired
    private RenterMapper renterMapper;

    @Autowired
    private JwtUtil jwtUtil;

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

    // --- 2b. OBTENER POR EMAIL ---

    @Operation(summary = "Obtener un huésped por email", description = "Reutiliza el findByEmail del repositorio para localizar el perfil de cliente. Usado en el checkout para resolver el renterId a partir del JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Huésped encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenterDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe ningún huésped con ese email"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/by-email")
    public ResponseEntity<?> getRenterByEmail(@RequestParam String email) {
        try {
            Optional<RenterDTO> renterDTO = renterRepository.findByEmail(email).map(renterMapper::toDTO);
            if (renterDTO.isPresent()) {
                return ResponseEntity.ok(renterDTO.get());
            } else {
                logger.warn("REST: No se encontró ningún huésped con email {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe ningún huésped con ese email.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el huésped.");
        }
    }

    // --- 2c. ASEGURAR EXISTENCIA DEL PERFIL DE HUÉSPED ---

    @Operation(summary = "Asegurar perfil de huésped del usuario actual",
            description = "Devuelve el RenterDTO del usuario autenticado. Si todavía no existe un perfil de Renter asociado a su email, lo crea automáticamente con los datos del User (nombre, apellido, email) y placeholders únicos para DNI y teléfono. Si se envía un body con dni/phone/address, valida y actualiza el perfil existente (o lo crea con esos datos). Idempotente: llamar varias veces siempre devuelve el mismo perfil.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Renter existente o recién creado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RenterDTO.class))),
            @ApiResponse(responseCode = "400", description = "DNI inválido o duplicado"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/ensure")
    public ResponseEntity<?> ensureRenter(@RequestHeader("Authorization") String tokenHeader,
                                          @RequestBody(required = false) RenterDTO dto) {
        try {
            String token = tokenHeader.replace(BEARER_PREFIX, "");
            Long userId = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
            RenterDTO renter = renterService.ensureRenterFromUser(userId, dto);
            return ResponseEntity.ok(renter);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error en ensureRenter: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al asegurar el perfil de cliente.");
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
