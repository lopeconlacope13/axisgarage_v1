package org.iesalixar.daw2.alvarolopez.axisgarage.handlers;

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

    /**
     * Captura fallos lanzados por Spring Security o CustomOAuth2UserService y 
     * redirige al cliente Angular inyectando el error codificado en la URL.
     *
     * @param request   Petición original
     * @param response  Respuesta HTTP
     * @param exception Motivo real del fracaso
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
                                            
        String errorMsg = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        response.sendRedirect("http://localhost:4200/login?error=" + errorMsg);
    }
}
