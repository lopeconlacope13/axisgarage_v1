package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.AuthRequestDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.AuthResponseDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.RegisterRequestDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.UserDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.UserService;
import org.iesalixar.daw2.alvarolopez.axisgarage.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*; // Importamos todo para incluir RequestMapping

import java.util.List;
import java.util.Map;

/**
 * Controlador responsable de gestionar las solicitudes relacionadas con la autenticación.
 * <p>
 * Expone los endpoints de login (JWT), registro, recuperación y reset de contraseña.
 * Todas las rutas de este controlador son públicas (sin autenticación previa requerida).
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {

    // ── Dependencias ─────────────────────────────────────────────────────────
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

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
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthRequestDTO authRequest) {
		try {
			if (authRequest.getEmail() == null || authRequest.getPassword() == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponseDTO(null, "Email and password are required."));
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

			return ResponseEntity.ok(new AuthResponseDTO(token, "Authentication successful."));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new AuthResponseDTO(null, "Invalid credentials. Please check your email and password."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new AuthResponseDTO(null, "Unexpected error. Please try again later."));
		}
	}

    /**
     * Registra un nuevo usuario con rol ROLE_USER.
     *
     * @param dto DTO con nombre, apellido, email y contraseña.
     * @return ResponseEntity con el UserDTO creado o error.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto) {
        try {
            UserDTO creado = userService.registrarUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering user. Please try again.");
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
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        userService.forgotPassword(body.get("email"));
        return ResponseEntity.ok("If an account exists with that email, you will receive a reset link shortly.");
    }

    /**
     * Completa el flujo de recuperación estableciendo la nueva contraseña.
     *
     * @param body JSON con "token" y "newPassword".
     * @return 200 OK si el token es válido, 400 si ha caducado o no existe.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        try {
            userService.resetPassword(body.get("token"), body.get("newPassword"));
            return ResponseEntity.ok("Password updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}