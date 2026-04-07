package org.iesalixar.daw2.alvarolopez.lopebnb.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.iesalixar.daw2.alvarolopez.lopebnb.services.CustomUserDetailsService;
import org.iesalixar.daw2.alvarolopez.lopebnb.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extraer el encabezado Authorization de la solicitud
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Verificar si el encabezado Authorization está presente y tiene un token válido
        // REFERENCIA PDF PÁGINA 22
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // <--- ESTO ES LO QUE TE FALTA Y VIENE EN EL PDF
        }

        // 3. Extraer el token JWT del encabezado
        jwt = authHeader.substring(7);

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