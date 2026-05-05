package org.iesalixar.daw2.alvarolopez.axisgarage.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handler personalizado para manejar fallos en la autenticación con OAuth2.
 * Este handler se encarga de redirigir al usuario al frontend de Angular
 * pasando un parámetro de error claro en caso de que la autenticación fracase
 * o el usuario deniegue el acceso en la pasarela externa.
 */
@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    // ── Constantes ────────────────────────────────────────────────────────────
    /** Ruta del frontend a la que se redirige cuando la autenticación OAuth2 falla. */
    private static final String LOGIN_ERROR_PATH = "/login?error=";

    // ── Dependencias ─────────────────────────────────────────────────────────
    /**
     * URL base del frontend Angular. Se inyecta desde application.properties
     * (clave: frontend.url). Por defecto apunta a localhost:4200 en desarrollo local.
     */
    @Value("${frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Captura fallos lanzados por Spring Security o CustomOAuth2UserService y
     * redirige al cliente Angular inyectando el error codificado en la URL.
     *
     * @param request   Petición HTTP original.
     * @param response  Respuesta HTTP donde se escribe el redirect.
     * @param exception Motivo real del fracaso de autenticación.
     * @throws IOException      si hay un problema al escribir la respuesta HTTP.
     * @throws ServletException si ocurre un error de servlet.
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // Codificamos el mensaje de error en UTF-8 para incluirlo de forma segura en la URL
        String errorMsg = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        response.sendRedirect(frontendUrl + LOGIN_ERROR_PATH + errorMsg);
    }
}
