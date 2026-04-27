package org.iesalixar.daw2.alvarolopez.axisgarage.handlers;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.User;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Role;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.UserRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler personalizado para manejar eventos de éxito en la autenticación con OAuth2.
 * Este handler verifica el usuario autenticado con un proveedor externo (Google/Facebook).
 * Posteriormente genera el JWT y redirige al frontend inyectando el token en la ruta de login.
 */
@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Maneja el evento de autenticación exitosa con OAuth2.
     * Recupera el email, obtiene al usuario de la base de datos y le genera un token definitivo.
     *
     * @param request       Objeto HttpServletRequest de la solicitud HTTP.
     * @param response      Objeto HttpServletResponse de la respuesta HTTP.
     * @param authentication Objeto Authentication del usuario que se acaba de loguear vía externa.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
                                            
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email).orElseThrow(
            () -> new RuntimeException("User not found after OAuth2 success")
        );

        // Añadimos el prefijo "ROLE_" para que Spring Security entienda las autoridades
        // correctamente. El filtro JWT (JwtAuthenticationFilter) usa estos valores directamente
        // como SimpleGrantedAuthority, y hasAnyRole("USER") espera "ROLE_USER".
        // Sin este prefijo, los usuarios OAuth reciben un 403 al intentar acceder a rutas protegidas.
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName())
                .collect(Collectors.toList());

        // Generar JWT
        String token = jwtUtil.generateToken(user.getEmail(), roles, user.getId());

        // Redirigimos al frontend con el token JWT definitivo incluido como parámetro de URL.
        response.sendRedirect("http://localhost:4200/login?token=" + token);
    }
}
