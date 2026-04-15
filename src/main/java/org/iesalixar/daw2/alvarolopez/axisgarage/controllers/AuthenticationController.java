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

/**
 * Controlador responsable de gestionar las solicitudes relacionadas con la autenticación.
 * Proporciona un endpoint para autenticar usuarios y generar un token JWT en caso de éxito.
 */
@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

	@Autowired
	private UserService userService;

    /**
     * genera un token JWT que incluye informacion del usuario y sus roles
     *
     * @Param authRequest Un Objeto
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthRequestDTO authRequest) {
		try {
			if (authRequest.getEmail() == null || authRequest.getPassword() == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponseDTO(null, "El email y la contraseña son obligatorios."));
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

			return ResponseEntity.ok(new AuthResponseDTO(token, "Autenticación correcta."));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new AuthResponseDTO(null, "Credenciales inválidas. Por favor, verifica tu email y contraseña."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new AuthResponseDTO(null, "Error inesperado. Por favor, inténtalo de nuevo más tarde."));
		}
	}

    @ExceptionHandler({Exception.class})
    public ResponseEntity<AuthResponseDTO> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponseDTO(null, "Ocurrió un error inesperado: " + e.getMessage()));
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
                    .body("Error al registrar el usuario.");
        }
    }
}