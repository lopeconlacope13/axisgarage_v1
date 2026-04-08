package org.iesalixar.daw2.alvarolopez.lopebnb.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API REST LopeBnB",
                version = "1.0",
                description = "API para la gestión de reservas de alojamientos rurales, propietarios, huéspedes y opiniones."
        ),
        security = @SecurityRequirement(name = "bearerAuth") // Aplica la seguridad a toda la API por defecto
)
@SecurityScheme(
        name = "bearerAuth", // Este nombre enlaza con el de arriba
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        description = "Introduce tu token JWT Bearer aquí para probar las rutas protegidas."
)
public class OpenAPIConfig {
}