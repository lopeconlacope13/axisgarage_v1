package org.iesalixar.daw2.alvarolopez.lopebnb.config;

import org.iesalixar.daw2.alvarolopez.lopebnb.services.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configura la seguridad de la aplicación, definiendo autenticación y autorización
 * para diferentes roles de usuario, y gestionando la política de sesiones.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Activa la seguridad basada en metodos
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configura el filtro de seguridad para las solicitudes HTTP, especificando las
     * rutas permitidas y los roles necesarios para acceder a diferentes endpoints.
     *
     * @param http instancia de {@Link HttpSecurity} para configurar la seguridad.
     * @return una instancia de {@Link SecurityFilterChain} que contiene la configuración de seguridad.
     * @throws Exception si ocurre un error en la configuración de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desactivamos CSRF porque al usar tokens JWT ya estamos protegidos contra este ataque
                .csrf(csrf -> csrf.disable())

                // 2. Indicamos que nuestra API es Stateless (sin estado, no guarda sesiones en memoria)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. REGLAS DE AUTORIZACIÓN (El núcleo de tu seguridad)
                .authorizeHttpRequests(auth -> auth
                        // --- RUTAS PÚBLICAS (No requieren token) ---
                        .requestMatchers("/api/v1/authenticate", "/api/v1/register").permitAll() // Login y Registro
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Documentación Swagger
                        .requestMatchers(HttpMethod.GET, "/api/casas/**").permitAll() // Todo el mundo puede VER las casas

                                // --- RUTAS DE CLIENTE ---
                                .requestMatchers(HttpMethod.POST, "/api/reservas/**").hasAnyRole("USER", "MANAGER", "ADMIN")

// --- RUTAS DE MANAGER ---
                                .requestMatchers("/api/casas/**").hasAnyRole("MANAGER", "ADMIN")
                                .requestMatchers("/api/propietarios/**").hasAnyRole("MANAGER", "ADMIN")

// --- RUTAS EXCLUSIVAS DE ADMIN ---
                                .requestMatchers(HttpMethod.DELETE, "/api/opiniones/**").hasRole("ADMIN")
                                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // --- CUALQUIER OTRA RUTA ---
                        // Por defecto, si se nos olvida alguna ruta, exigimos que al menos esté logueado
                        .anyRequest().authenticated()
                )
                // 4. Añadimos tu filtro JWT para que intercepte las peticiones y valide el token antes que Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura el proveedor de autenticación para usar el servicio de detalles de usuario
     * personalizado y el codificador de contraseñas.
     *
     * @return una instancia de {@Link DaoAuthenticationProvider} para la autenticación.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configura el codificador de contraseñas para cifrar las contraseñas de los usuarios
     * utilizando BCrypt.
     *
     * @return una instancia de {@link PasswordEncoder} que utiliza BCrypt para cifrar contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Entrando en el método passwordEncoder");
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        logger.info("Saliendo del método passwordEncoder");
        return encoder;
    }

    /**
     * Configura y expone un bean de tipo {@link AuthenticationManager}.
     *
     * En Spring Security, el ÀuthenticationManager` es el componente principal que se encarga
     * de procesar solicitudes de autenticación. Este metodo obtiene la instancia de
     * `AuthenticationManager`configurada automáticamente por Spring a través de
     * `AuthenticationConfiguration`y la expone como un bean disponible en el contexto
     * de la aplicación.
     *
     * @param configuration Objeto de tipo {@link AuthenticationConfiguration} que contiene
     *                      la configuración de autenticación de Spring Security. Este objeto
     *                      incluye los detalles del flujo de autenticación configurado, como
     *                      el proveedor de autenticación y los detalles de usuario.
     * @return Una instancia de {@link AuthenticationManager} configurada con los detalles
     *          específicados en la aplicación.
     * @throws Exception Si ocurre algún error al obtener el `AuthenticationManager`.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // Obtiene y devuelve el AuthenticationManager desde la configuración proporcionada
        return configuration.getAuthenticationManager();
    }


}