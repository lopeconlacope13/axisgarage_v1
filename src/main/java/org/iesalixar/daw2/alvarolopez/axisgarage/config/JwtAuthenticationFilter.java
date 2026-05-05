package org.iesalixar.daw2.alvarolopez.axisgarage.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.CustomUserDetailsService;
import org.iesalixar.daw2.alvarolopez.axisgarage.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que intercepta cada petición HTTP para comprobar si lleva un token JWT válido
 * en la cabecera Authorization. Si el token es correcto, establece la autenticación
 * en el contexto de Spring Security para que el resto de la cadena de filtros lo reconozca.
 * <p>
 * Se ejecuta UNA SOLA VEZ por petición gracias a {@link OncePerRequestFilter}.
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // ── Constantes ────────────────────────────────────────────────────────────
    /** Prefijo estándar del esquema Bearer definido en RFC 6750. */
    private static final String BEARER_PREFIX = "Bearer ";

    /** Longitud del prefijo "Bearer " (7 caracteres). Se usa para extraer el token sin substring(7) desnudo. */
    private static final int BEARER_PREFIX_LENGTH = 7;

    // ── Dependencias ─────────────────────────────────────────────────────────
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Lógica principal del filtro: extrae el JWT de la cabecera, lo valida
     * y, si es correcto, inyecta la autenticación en el SecurityContext.
     *
     * @param request     Petición HTTP entrante.
     * @param response    Respuesta HTTP saliente.
     * @param filterChain Cadena de filtros de Spring Security.
     * @throws ServletException si ocurre un error de servlet.
     * @throws IOException      si ocurre un error de E/S.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extraer el encabezado Authorization de la solicitud
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Si no hay cabecera o no empieza por "Bearer ", dejamos pasar la petición sin autenticar.
        //    Las rutas públicas (permitAll) seguirán funcionando; las protegidas devolverán 401.
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token JWT quitando el prefijo "Bearer " (7 caracteres)
        jwt = authHeader.substring(BEARER_PREFIX_LENGTH);

        // 4. Extraer el nombre de usuario
        username = jwtUtil.extractUsername(jwt);

        // 5. Verificar si no hay autenticación existente
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Cargar los detalles del usuario
            var userDetails = userDetailsService.loadUserByUsername(username);

            // 7. Validar el token
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // 8. Extraer los claims
                Claims claims = jwtUtil.extractAllClaims(jwt);

                // 9. Extraer roles y convertir
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // 10. Crear objeto de autenticación
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                // 11. Configurar detalles
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 12. Establecer autenticación en el contexto
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 13. Continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }
}