package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.AuthRequestDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.AuthResponseDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*; // Importamos todo para incluir RequestMapping

import java.util.List;

/**
 * Controlador responsable de gestionar las solicitudes relacionadas con la autenticación.
 * Proporciona un endpoint para autenticar usuarios y generar un token JWT en caso de éxito.
 */
@RestController
@RequestMapping("/api/v1") // <--- ¡ESTA LÍNEA ES LA QUE FALTABA!
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * genera un token JWT que incluye informacion del usuario y sus roles
     *
     * @Param authRequest Un Objeto
     */
    @PostMapping("/authenticate") // Al combinar con la de arriba, la ruta final es /api/v1/authenticate
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthRequestDTO authRequest) {
        try {
            //Validar datos de entrada
            if (authRequest.getUsername() == null || authRequest.getPassword() == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponseDTO("", "El nombre de usuario y la contraseña son obligatorios."));
            }

            //Intenta autenticar al usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            //Obtiene el nombre de usuario autenticado
            String username = authentication.getName();

            //Extrae los roles
            List<String> roles = authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .toList();

            //Genera un token JWT
            String token = jwtUtil.generateToken(username, roles);

            //Retorna respuesta exitosa
            return ResponseEntity.ok(new AuthResponseDTO(token, "Authentication successful"));

        } catch (BadCredentialsException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(null, "Credenciales inválidas. Por favor, verifica tus datos."));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponseDTO(null, "Ocurrió un error inesperado. Por favor, inténtalo de nuevo mas tarde"));
        }
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<AuthResponseDTO> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponseDTO(null, "Ocurrió un error inesperado: " + e.getMessage()));
    }
}