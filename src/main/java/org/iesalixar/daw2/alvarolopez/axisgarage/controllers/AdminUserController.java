package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.UserSummaryDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de usuarios desde el panel de administración.
 * Todos los endpoints de esta clase son exclusivos del rol ADMIN.
 * Permiten listar usuarios, cambiar su rol y eliminarlos.
 * La separación de este controlador respecto a UserController es intencional:
 * UserController gestiona el perfil del usuario autenticado (GET /api/user),
 * mientras que este controlador gestiona la lista global de usuarios (GET /api/users).
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Admin - Usuarios", description = "Operaciones de administración sobre los usuarios del sistema")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    private UserService userService;

    /**
     * Devuelve la lista completa de todos los usuarios registrados en el sistema.
     * Solo accesible para administradores (ROLE_ADMIN).
     * Cada usuario se representa como UserSummaryDTO para no exponer datos sensibles
     * como contraseñas hasheadas ni tokens de recuperación.
     *
     * @return Lista de UserSummaryDTO con todos los usuarios del sistema.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        logger.info("ADMIN: solicitando lista completa de usuarios");
        List<UserSummaryDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Cambia el rol de un usuario específico identificado por su ID.
     * Solo accesible para administradores (ROLE_ADMIN).
     * El body JSON debe incluir el campo "role" con el nombre del nuevo rol.
     * Ejemplo de body: { "role": "ROLE_MANAGER" }
     * Este endpoint elimina todos los roles actuales del usuario y asigna únicamente
     * el nuevo rol indicado. Un usuario solo puede tener un rol activo a la vez.
     *
     * @param id   ID del usuario al que se cambia el rol (en la URL).
     * @param body JSON con la clave "role" y el nombre del nuevo rol como valor.
     * @return UserSummaryDTO actualizado con el nuevo rol asignado, o 400 si el rol no existe.
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String roleName = body.get("role");
            logger.info("ADMIN: cambiando rol del usuario {} a {}", id, roleName);
            UserSummaryDTO updated = userService.changeUserRole(id, roleName);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al cambiar rol del usuario {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al cambiar rol del usuario {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cambiar el rol del usuario.");
        }
    }

    /**
     * Elimina un usuario del sistema de forma permanente.
     * Solo accesible para administradores (ROLE_ADMIN).
     * El usuario con ID=1 (administrador principal) está protegido y no puede eliminarse.
     * Esta operación es irreversible.
     *
     * @param id ID del usuario a eliminar (en la URL).
     * @return 204 No Content si la eliminación fue exitosa, o 400 si se intenta eliminar el admin.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            logger.info("ADMIN: eliminando usuario con ID {}", id);
            userService.deleteUser(id);
            // 204 No Content: operación exitosa, no hay cuerpo que devolver
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Intento de eliminar usuario protegido (ID {}): {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar usuario con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el usuario.");
        }
    }
}
