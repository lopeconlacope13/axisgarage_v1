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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Usuario", description = "Operaciones para obtener información del usuario autenticado")
public class UserController {

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
            String token = tokenHeader.replace("Bearer ", "");

            // Usamos el servicio jwt/utilidad para extraer el id
            Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));

            UserDTO userDTO = userService.getUserDTOById(id);
            logger.info("La información del usuario logueado con id {} ha sido recuperada.", id);
            return ResponseEntity.ok(userDTO);

        } catch (Exception e) {
            logger.error("Error al obtener el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener la información del usuario.");
        }
    }
}