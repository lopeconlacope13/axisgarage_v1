package org.iesalixar.daw2.alvarolopez.axisgarage.config;

import org.iesalixar.daw2.alvarolopez.axisgarage.services.CustomUserDetailsService;
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

import static org.springframework.security.config.Customizer.withDefaults;

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
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						// Dejamos pasar los OPTIONS
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						// --- RUTAS PÚBLICAS ---
						.requestMatchers("/api/v1/authenticate", "/api/v1/register").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/vehicles", "/api/vehicles/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/locations", "/api/locations/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
						.requestMatchers("/uploads/**").permitAll()

						// --- RUTAS DE CLIENTE (USER/MANAGER/ADMIN) ---
						.requestMatchers(HttpMethod.GET, "/api/reservas/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/reservas/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/reservas/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/reservas/**").hasAnyRole("USER", "ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/opiniones/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/opiniones/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/opiniones/**").hasAnyRole("USER", "ADMIN")

						.requestMatchers(HttpMethod.GET, "/api/huespedes/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/huespedes/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/huespedes/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/huespedes/**").hasAnyRole("USER", "ADMIN")

						// --- COBERTURAS (USER+) ---
						.requestMatchers(HttpMethod.GET, "/api/coverages/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/coverages/**").hasAnyRole("USER", "MANAGER", "ADMIN")

						// --- RUTAS DE MANAGER ---
						.requestMatchers(HttpMethod.POST, "/api/vehicles/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/vehicles/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasAnyRole("MANAGER", "ADMIN")

						.requestMatchers(HttpMethod.GET, "/api/propietarios/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/propietarios/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/propietarios/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/propietarios/**").hasAnyRole("MANAGER", "ADMIN")

						// --- INFORMES DE DAÑOS (MANAGER+) ---
						.requestMatchers("/api/damage-reports/**").hasAnyRole("MANAGER", "ADMIN")

						// --- RUTAS EXCLUSIVAS DE ADMIN ---
						.requestMatchers("/api/usuarios/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/locations/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/locations/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/locations/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

						// --- CUALQUIER OTRA RUTA ---
						.anyRequest().authenticated()
				)
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
				)
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