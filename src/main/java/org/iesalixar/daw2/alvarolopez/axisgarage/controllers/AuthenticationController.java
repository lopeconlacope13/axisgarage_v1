package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.AuthRequestDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.AuthResponseDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.RegisterRequestDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.UserDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.UserService;
import org.iesalixar.daw2.alvarolopez.axisgarage.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST encargado de gestionar el ciclo completo de autenticación de usuarios.
 * <p>
 * Expone los endpoints de login (JWT), registro, recuperación y reset de contraseña.
 * Todas las rutas de este controlador son públicas (sin autenticación previa requerida),
 * tal y como se configura en {@link org.iesalixar.daw2.alvarolopez.axisgarage.config.SecurityConfig}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Autenticación", description = "Endpoints públicos para login, registro y recuperación de contraseña")
public class AuthenticationController {

    // ── Dependencias ─────────────────────────────────────────────────────────
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    // Fuente de mensajes i18n — lee de messages_en.properties o messages_es.properties
    @Autowired
    private MessageSource messageSource;

    /**
     * Autentica a un usuario con email y contraseña y devuelve un token JWT firmado.
     * <p>
     * El token incluye el email (subject), la lista de roles (claim "roles") y el ID
     * del usuario (claim "id"). El frontend lo almacena en localStorage y lo adjunta
     * en cada petición a través de la cabecera Authorization: Bearer {token}.
     * </p>
     *
     * @param authRequest DTO con el email y la contraseña en texto plano.
     * @return {@link AuthResponseDTO} con el token JWT si las credenciales son correctas,
     *         o un mensaje de error si son inválidas.
     */
    @Operation(summary = "Iniciar sesión con email y contraseña",
               description = "Valida las credenciales y devuelve un token JWT firmado con roles e ID del usuario.")
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthRequestDTO authRequest) {
        try {
            if (authRequest.getEmail() == null || authRequest.getPassword() == null) {
                String msg = messageSource.getMessage("msg.auth-controller.credentials.required", null, LocaleContextHolder.getLocale());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponseDTO(null, msg));
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            String email = authentication.getName();

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            Long id = userService.getIdByEmail(email);

            String token = jwtUtil.generateToken(email, roles, id);

            String successMsg = messageSource.getMessage("msg.auth-controller.success", null, LocaleContextHolder.getLocale());
            return ResponseEntity.ok(new AuthResponseDTO(token, successMsg));
        } catch (BadCredentialsException e) {
            String msg = messageSource.getMessage("msg.auth-controller.credentials.invalid", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(null, msg));
        } catch (Exception e) {
            String msg = messageSource.getMessage("msg.auth-controller.unexpected.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponseDTO(null, msg));
        }
    }

    /**
     * Registra un nuevo usuario con rol ROLE_USER.
     *
     * @param dto DTO con nombre, apellido, email y contraseña.
     * @return ResponseEntity con el UserDTO creado o error.
     */
    @Operation(summary = "Registrar nuevo usuario",
               description = "Crea una nueva cuenta de usuario con rol USER. Lanza error 400 si el email ya existe.")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto) {
        try {
            UserDTO creado = userService.registrarUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            String msg = messageSource.getMessage("msg.auth-controller.register.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

    /**
     * Inicia el flujo de recuperación de contraseña enviando un email al usuario.
     * Devuelve siempre 200 OK aunque el email no exista, para no revelar
     * si una dirección está registrada en el sistema.
     *
     * @param body JSON con la clave "email".
     * @return 200 OK con mensaje informativo.
     */
    @Operation(summary = "Solicitar enlace de recuperación de contraseña",
               description = "Envía un email con enlace de reset al usuario. Siempre devuelve 200 para no exponer qué emails están registrados.")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        userService.forgotPassword(body.get("email"));
        String msg = messageSource.getMessage("msg.auth-controller.forgot-password.sent", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(msg);
    }

    /**
     * Completa el flujo de recuperación estableciendo la nueva contraseña.
     *
     * @param body JSON con "token" y "newPassword".
     * @return 200 OK si el token es válido, 400 si ha caducado o no existe.
     */
    @Operation(summary = "Resetear contraseña con token",
               description = "Establece una nueva contraseña si el token de recuperación es válido y no ha expirado.")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        try {
            userService.resetPassword(body.get("token"), body.get("newPassword"));
            String msg = messageSource.getMessage("msg.auth-controller.password.updated", null, LocaleContextHolder.getLocale());
            return ResponseEntity.ok(msg);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
