package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.UserDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.UserService;
import org.iesalixar.daw2.alvarolopez.axisgarage.utils.JwtUtil;
import org.iesalixar.daw2.alvarolopez.axisgarage.utils.MessageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controlador REST que expone las operaciones del usuario autenticado:
 * consultar su perfil, subir avatar y cambiar contraseña.
 * Todos los endpoints extraen la identidad del usuario desde el token JWT
 * incluido en la cabecera Authorization.
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "Usuario", description = "Operaciones para obtener información del usuario autenticado")
public class UserController {

    // ── Constantes ────────────────────────────────────────────────────────────
    /** Prefijo estándar del esquema Bearer (RFC 6750) que se elimina para obtener el token puro. */
    private static final String BEARER_PREFIX = "Bearer ";

    // ── Dependencias ─────────────────────────────────────────────────────────
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "Obtener perfil del usuario", description = "Devuelve los datos del usuario logueado extrayendo su ID desde el token JWT proporcionado en la cabecera.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información del usuario recuperada con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token ausente o inválido"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String tokenHeader) {
        logger.info("Solicitando la información del usuario logueado");

        try {
            // Limpiamos el prefijo "Bearer "
            String token = tokenHeader.replace(BEARER_PREFIX, "");

            // Usamos el servicio jwt/utilidad para extraer el id
            Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));

            UserDTO userDTO = userService.getUserDTOById(id);
            logger.info("La información del usuario logueado con id {} ha sido recuperada.", id);
            return ResponseEntity.ok(userDTO);

        } catch (Exception e) {
            logger.error("Error al obtener el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MessageConstants.USER_FETCH_ERROR);
        }
    }

    /**
     * Sube o reemplaza la foto de perfil del usuario autenticado.
     * Extrae el ID del usuario desde el JWT, pasa el archivo a UserService
     * y devuelve el UserDTO actualizado con el nuevo nombre de imagen.
     *
     * @param tokenHeader Cabecera Authorization con el JWT.
     * @param file        Archivo de imagen enviado desde el formulario (multipart).
     * @return UserDTO con el campo 'image' actualizado.
     */
    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadAvatar(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestParam("file") MultipartFile file) {
        try {
            String token = tokenHeader.replace(BEARER_PREFIX, "");
            Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
            UserDTO updated = userService.uploadAvatar(id, file);
            logger.info("Avatar actualizado para usuario con ID {}", id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error al subir avatar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MessageConstants.USER_PHOTO_UPLOAD_ERROR);
        }
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     * Extrae el ID del usuario desde el token JWT, verifica la contraseña actual
     * y, si es correcta, guarda la nueva hasheada con BCrypt.
     *
     * @param tokenHeader Cabecera Authorization con el JWT (formato "Bearer TOKEN").
     * @param body        JSON con los campos "currentPassword" y "newPassword".
     * @return 200 OK si el cambio fue correcto, 400 si la contraseña actual no coincide.
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody Map<String, String> body) {
        try {
            String token = tokenHeader.replace(BEARER_PREFIX, "");
            Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
            userService.changePassword(id, body.get("currentPassword"), body.get("newPassword"));
            logger.info("Contraseña actualizada para el usuario con ID {}", id);
            return ResponseEntity.ok(MessageConstants.USER_PASSWORD_UPDATED);
        } catch (IllegalArgumentException e) {
            logger.warn("Intento fallido de cambio de contraseña: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al cambiar la contraseña: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MessageConstants.USER_PASSWORD_CHANGE_ERROR);
        }
    }

}