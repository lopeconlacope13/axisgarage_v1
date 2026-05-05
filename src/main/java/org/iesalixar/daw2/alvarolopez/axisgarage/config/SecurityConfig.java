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

    @Autowired
    private org.iesalixar.daw2.alvarolopez.axisgarage.handlers.CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Autowired
    private org.iesalixar.daw2.alvarolopez.axisgarage.handlers.CustomOAuth2FailureHandler customOAuth2FailureHandler;

    @Autowired
    private org.iesalixar.daw2.alvarolopez.axisgarage.services.CustomOAuth2UserService customOAuth2UserService;

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
				// IMPORTANTE TFG: OAuth2 necesita guardar un parámetro "state" temporal en memoria (Session)
				// durante el viaje a Google/Facebook para prevenir ataques CSRF. Por tanto, no podemos usar
				// STATELESS estricto aquí o fallará al volver. Usamos IF_REQUIRED para que Spring solo
				// levante la sesión para el Handshake OAuth, pero nuestra API sigue dependiendo de JWT.
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.oauth2Login(oauth2 -> oauth2
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
						.successHandler(customOAuth2SuccessHandler)
						.failureHandler(customOAuth2FailureHandler)
				)
				.authorizeHttpRequests(auth -> auth
						// Dejamos pasar los OPTIONS
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						// --- RUTAS PÚBLICAS ---
						.requestMatchers("/api/v1/authenticate", "/api/v1/register",
								"/api/v1/forgot-password", "/api/v1/reset-password").permitAll()
						
						// IMPORTANTE TFG: Las rutas internas que usa Spring Boot para interceptar 
						// la subida a Google (/oauth2/authorization/{provider}) y la bajada 
						// (/login/oauth2/code/{provider}) deben figurar en la lista blanca explícita.
						.requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
						
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/vehicles", "/api/vehicles/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/locations", "/api/locations/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
						.requestMatchers("/uploads/**").permitAll()
						// Formulario de contacto público — no requiere autenticación
						.requestMatchers(HttpMethod.POST, "/api/contact").permitAll()

						// --- RUTAS DE CLIENTE (USER/MANAGER/ADMIN) ---
						.requestMatchers(HttpMethod.GET, "/api/reservations/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/reservations/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/reservations/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/reservations/**").hasAnyRole("USER", "MANAGER", "ADMIN")

						.requestMatchers(HttpMethod.GET, "/api/reviews/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/reviews/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/reviews/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasAnyRole("USER", "MANAGER", "ADMIN")

						.requestMatchers(HttpMethod.GET, "/api/renters/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						// Endpoint idempotente: cualquier usuario logueado puede crear/recuperar su propio perfil de Renter
						.requestMatchers(HttpMethod.POST, "/api/renters/ensure").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/renters/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/renters/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/renters/**").hasAnyRole("MANAGER", "ADMIN")

						// --- COBERTURAS (USER+) ---
						.requestMatchers(HttpMethod.GET, "/api/coverages/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/coverages/**").hasAnyRole("USER", "MANAGER", "ADMIN")

						// --- RUTAS DE MANAGER ---
						.requestMatchers(HttpMethod.POST, "/api/vehicles/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/vehicles/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/vehicles/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasAnyRole("MANAGER", "ADMIN")

						.requestMatchers("/api/owners/**").hasAnyRole("MANAGER", "ADMIN")

						// --- ESTADÍSTICAS (MANAGER+) ---
						.requestMatchers("/api/stats/**").hasAnyRole("MANAGER", "ADMIN")

						// --- INFORMES DE DAÑOS (MANAGER+) ---
						.requestMatchers("/api/damage-reports/**").hasAnyRole("MANAGER", "ADMIN")

						// --- FACTURAS (USER puede ver la suya, MANAGER+ gestión completa) ---
						.requestMatchers(HttpMethod.GET, "/api/invoices/reservation/**").hasAnyRole("USER", "MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/invoices/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/invoices/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/invoices/**").hasAnyRole("MANAGER", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/invoices/**").hasRole("ADMIN")

						// --- RUTAS EXCLUSIVAS DE ADMIN ---
						.requestMatchers("/api/users/**").hasRole("ADMIN")
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