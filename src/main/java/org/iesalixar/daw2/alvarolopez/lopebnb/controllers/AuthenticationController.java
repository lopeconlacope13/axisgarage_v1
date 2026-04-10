package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.AuthRequestDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.AuthResponseDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.services.UserService;
import org.iesalixar.daw2.alvarolopez.lopebnb.utils.JwtUtil;
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
    @PostMapping("/authenticate") // Al combinar con la de arriba, la ruta final es /api/v1/authenticate
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthRequestDTO authRequest) {
		try {
			// Validar datos de entrada (opcional si no usas validación adicional en DTO)
			if (authRequest.getUsername() == null || authRequest.getPassword() == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponseDTO(null, "El nombre de usuario y la contraseña son obligatorios."));
			}

			// Intenta autenticar al usuario con las credenciales proporcionadas
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
			);

			// Obtiene el nombre de usuario autenticado
			String username = authentication.getName();

			// Extrae los roles del usuario autenticado desde las autoridades asignadas
			List<String> roles = authentication.getAuthorities().stream()
					.map(GrantedAuthority::getAuthority) // Convierte cada autoridad en su representación de texto
					.toList();
			Long id = userService.getIdByUsername(username);

			// Genera un token JWT para el usuario autenticado, incluyendo sus roles
			String token = jwtUtil.generateToken(username, roles, id);


			// Retorna una respuesta con el token JWT y un mensaje de éxito
			return ResponseEntity.ok(new AuthResponseDTO(token, "Authentication successful first part"));
		} catch (BadCredentialsException e) {
			// Manejo de credenciales inválidas
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new AuthResponseDTO(null, "Credenciales inválidas. Por favor, verifica tus datos."));
		} catch (Exception e) {
			// Manejo de cualquier otro error
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new AuthResponseDTO(null, "Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde."));
		}
	}

    @ExceptionHandler({Exception.class})
    public ResponseEntity<AuthResponseDTO> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponseDTO(null, "Ocurrió un error inesperado: " + e.getMessage()));
    }
}