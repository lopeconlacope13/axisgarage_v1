package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO ligero para representar a un usuario en el panel de administración.
 * <p>
 * Solo expone los campos necesarios para que el ADMIN pueda gestionar usuarios:
 * identificación, email, nombre, estado y roles. No expone la contraseña ni
 * datos sensibles de auditoría.
 * <p>
 * Se usa exclusivamente en los endpoints de gestión de usuarios (GET /api/users,
 * PATCH /api/users/{id}/role) que solo están disponibles para el rol ADMIN.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {

    /** Identificador único del usuario en la base de datos. */
    private Long id;

    /** Nombre de usuario (se deriva del email en el registro). */
    private String username;

    /** Correo electrónico del usuario, usado también como login. */
    private String email;

    /** Primer nombre del usuario. */
    private String firstName;

    /** Apellido del usuario. */
    private String lastName;

    /**
     * Indica si el usuario está habilitado para acceder al sistema.
     * Un usuario deshabilitado no puede autenticarse aunque su cuenta exista.
     */
    private boolean enabled;

    /**
     * Conjunto de nombres de roles asignados al usuario.
     * Ejemplo: {"ROLE_USER"}, {"ROLE_MANAGER"}, {"ROLE_ADMIN"}.
     * Se devuelven solo los nombres (String) para simplificar la respuesta al frontend.
     */
    private Set<String> roles;
}
