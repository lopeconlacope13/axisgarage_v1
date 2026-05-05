package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.RegisterRequestDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.UserDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.UserSummaryDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Role;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.User;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.UserMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.RoleRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio central de gestión de usuarios en Axis Garage.
 * <p>
 * Cubre el registro de nuevos usuarios, la consulta por ID o email,
 * el cambio de contraseña autenticado y el flujo completo de recuperación
 * de contraseña por email (forgot-password / reset-password).
 */
@Service
public class UserService {

	// ── Constantes ────────────────────────────────────────────────────────────

	/** Nombre del rol por defecto que se asigna a todo usuario que se registra. */
	private static final String DEFAULT_ROLE = "ROLE_USER";

	/**
	 * Tiempo de validez (en horas) del enlace de recuperación de contraseña.
	 * Después de este tiempo el token caduca y el usuario debe solicitar uno nuevo.
	 */
	private static final long PASSWORD_RESET_EXPIRY_HOURS = 1L;

	/**
	 * ID del administrador principal del sistema.
	 * Este usuario está protegido y no puede eliminarse desde ningún endpoint,
	 * para garantizar que siempre haya un admin activo en el sistema.
	 */
	private static final long ADMIN_PROTECTED_ID = 1L;

	/** Separador del email que se usa para derivar el username (parte antes del @). */
	private static final String EMAIL_USERNAME_SEPARATOR = "@";

	// ── Dependencias ─────────────────────────────────────────────────────────
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private EmailService emailService;

	@Autowired
	private FileStorageService fileStorageService;

	/**
	 * URL base del frontend Angular. Se inyecta desde application.properties
	 * (clave: frontend.url). Se usa para construir el enlace de recuperación
	 * de contraseña que se envía por correo al usuario.
	 */
	@Value("${frontend.url:http://localhost:4200}")
	private String frontendUrl;

	/**
	 * Obtiene el ID de un usuario a partir de su email.
	 *
	 * @param email Email del usuario.
	 * @return ID del usuario.
	 */
	public Long getIdByEmail(String email) {
		return userRepository.getIdByEmail(email);
	}

	/**
	 * Registra un nuevo usuario en el sistema con rol ROLE_USER.
	 * El username se deriva del email (parte antes del @).
	 *
	 * @param dto DTO con los datos de registro.
	 * @return UserDTO con los datos del usuario creado.
	 * @throws IllegalArgumentException si el email ya está en uso.
	 */
	public UserDTO registrarUsuario(RegisterRequestDTO dto) {
		if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
			throw new IllegalArgumentException("Ya existe un usuario registrado con ese email.");
		}

		// Buscamos el rol por defecto; si no existe en BD hay un problema de configuración
		Role rolUser = roleRepository.findByName(DEFAULT_ROLE)
				.orElseThrow(() -> new RuntimeException("El rol " + DEFAULT_ROLE + " no existe en la base de datos."));

		User nuevo = new User();
		// El username se genera tomando la parte del email antes del símbolo @
		nuevo.setUsername(dto.getEmail().split(EMAIL_USERNAME_SEPARATOR)[0]);
		nuevo.setEmail(dto.getEmail());
		nuevo.setPassword(passwordEncoder.encode(dto.getPassword()));
		nuevo.setFirstName(dto.getFirstName());
		nuevo.setLastName(dto.getLastName());
		nuevo.setEnabled(true);
		nuevo.setRoles(Set.of(rolUser));

		User guardado = userRepository.save(nuevo);

		// Enviamos el email de bienvenida; si falla, el registro ya está guardado.
		emailService.sendWelcomeEmail(dto.getEmail(), dto.getFirstName());

		return userMapper.toDTO(guardado);
	}

	public UserDTO getUserDTOById(Long id) {
		Optional<User> userOpt = userRepository.findById(id);
		if (userOpt.isPresent()) {
			return userMapper.toDTO(userOpt.get());
		}
		throw new RuntimeException("El usuario con identificador " + id + " no existe.");
	}

	public User getUserById(Long id) {
		Optional<User> userOpt = userRepository.findById(id);
		if (userOpt.isPresent()) {
			return userOpt.get();
		}
		throw new RuntimeException("El usuario con identificador " + id + " no existe.");
	}

	/**
	 * Cambia la contraseña del usuario tras verificar que la contraseña actual es correcta.
	 * La nueva contraseña se hashea con BCrypt antes de guardarla, igual que en el registro.
	 *
	 * @param userId          ID del usuario autenticado (extraído del JWT en el controlador).
	 * @param currentPassword Contraseña actual en texto plano para verificar contra el hash de la BD.
	 * @param newPassword     Nueva contraseña en texto plano que se hasheará y persistirá.
	 * @throws IllegalArgumentException si la contraseña actual introducida no coincide con el hash guardado.
	 */
	public void changePassword(Long userId, String currentPassword, String newPassword) {
		User user = getUserById(userId);
		// Comprobamos que la contraseña actual es correcta antes de permitir el cambio
		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new IllegalArgumentException("La contraseña actual no es correcta.");
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	/**
	 * Inicia el flujo de recuperación de contraseña.
	 * Genera un token UUID único, lo persiste con expiración de 1 hora
	 * y envía el enlace de recuperación al correo del usuario.
	 * <p>
	 * Si el email no existe, el método termina sin error para no revelar
	 * si una dirección está registrada (buena práctica de seguridad básica).
	 *
	 * @param email Correo del usuario que solicita recuperar su contraseña.
	 */
	public void forgotPassword(String email) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			// No revelamos si el email existe o no (seguridad)
			return;
		}
		User user = userOpt.get();

		// Generamos un token único e irrepetible
		String token = UUID.randomUUID().toString();
		user.setResetToken(token);
		// El token expira tras PASSWORD_RESET_EXPIRY_HOURS horas desde su generación
		user.setResetTokenExpiry(LocalDateTime.now().plusHours(PASSWORD_RESET_EXPIRY_HOURS));
		userRepository.save(user);

		// Construimos el enlace que recibirá el usuario en el correo
		String resetLink = frontendUrl + "/reset-password?token=" + token;
		emailService.sendPasswordResetEmail(email, resetLink);
	}

	/**
	 * Completa el flujo de recuperación: valida el token y actualiza la contraseña.
	 * Tras el cambio, el token se elimina para que no pueda reutilizarse.
	 *
	 * @param token       Token UUID recibido desde el enlace del correo.
	 * @param newPassword Nueva contraseña en texto plano (se hasheará con BCrypt).
	 * @throws IllegalArgumentException si el token es inválido o ha caducado.
	 */
	public void resetPassword(String token, String newPassword) {
		User user = userRepository.findByResetToken(token)
				.orElseThrow(() -> new IllegalArgumentException("El enlace de recuperación no es válido."));

		// Comprobamos que el token no ha caducado (válido durante 1 hora)
		if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("El enlace de recuperación ha caducado. Solicita uno nuevo.");
		}

		// Actualizamos la contraseña y limpiamos el token para evitar reutilización
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setResetToken(null);
		user.setResetTokenExpiry(null);
		userRepository.save(user);
	}

	/**
	 * Sube la imagen de perfil del usuario y guarda su nombre en la base de datos.
	 * Utiliza FileStorageService para guardar el archivo en disco y obtener el nombre único
	 * generado (UUID). Luego persiste ese nombre en el campo 'image' del usuario.
	 *
	 * @param userId ID del usuario autenticado (viene del JWT).
	 * @param file   Archivo de imagen enviado desde el frontend.
	 * @return UserDTO actualizado con el nuevo nombre de imagen.
	 */
	public UserDTO uploadAvatar(Long userId, MultipartFile file) {
		User user = getUserById(userId);

		// Guardamos el archivo en disco y obtenemos el nombre único generado
		String filename = fileStorageService.saveFile(file);

		// Si saveFile devuelve null (por ejemplo, por un IOException interno),
		// lanzamos una excepción clara en lugar de persistir null en base de datos.
		if (filename == null) {
			throw new RuntimeException("Error al guardar la imagen en el servidor");
		}

		// Asociamos el nombre de archivo al usuario y lo persistimos
		user.setImage(filename);
		userRepository.save(user);

		return userMapper.toDTO(user);
	}

	// ─── Métodos de administración de usuarios (solo ADMIN) ──────────────────

	/**
	 * Devuelve la lista completa de todos los usuarios registrados en el sistema.
	 * <p>
	 * Cada usuario se transforma a UserSummaryDTO para exponer solo los campos
	 * necesarios para la gestión desde el panel de administración.
	 * Los roles se mapean a sus nombres (String) para simplificar el JSON.
	 *
	 * @return Lista de UserSummaryDTO con todos los usuarios del sistema.
	 */
	public List<UserSummaryDTO> getAllUsers() {
		// Obtenemos todos los usuarios de la base de datos
		List<User> users = userRepository.findAll();

		// Convertimos cada usuario a su versión resumida (DTO)
		return users.stream()
				.map(this::toSummaryDTO)
				.collect(Collectors.toList());
	}

	/**
	 * Cambia el rol de un usuario: elimina todos los roles actuales y asigna el nuevo.
	 * <p>
	 * Solo acepta roles que existan en la tabla 'roles' de la base de datos.
	 * Si el nombre del rol no existe, lanza una excepción para informar al administrador.
	 *
	 * @param id       ID del usuario al que se cambia el rol.
	 * @param roleName Nombre del nuevo rol, por ejemplo "ROLE_USER" o "ROLE_MANAGER".
	 * @return UserSummaryDTO actualizado con el nuevo rol asignado.
	 * @throws RuntimeException         si el usuario no existe.
	 * @throws IllegalArgumentException si el nombre de rol no existe en la BD.
	 */
	public UserSummaryDTO changeUserRole(Long id, String roleName) {
		// Buscamos al usuario; si no existe lanzamos excepción
		User user = getUserById(id);

		// Buscamos el rol por nombre; si no existe no podemos asignarlo
		Role newRole = roleRepository.findByName(roleName)
				.orElseThrow(() -> new IllegalArgumentException(
						"El rol '" + roleName + "' no existe en la base de datos."));

		// Reemplazamos todos los roles actuales por el nuevo rol
		Set<Role> newRoles = new HashSet<>();
		newRoles.add(newRole);
		user.setRoles(newRoles);

		// Persistimos el cambio y devolvemos el DTO actualizado
		userRepository.save(user);
		return toSummaryDTO(user);
	}

	/**
	 * Elimina un usuario del sistema de forma permanente.
	 * <p>
	 * El usuario con ID=1 (administrador principal) está protegido y no puede
	 * eliminarse para evitar que el sistema quede sin administrador.
	 *
	 * @param id ID del usuario a eliminar.
	 * @throws IllegalArgumentException si se intenta eliminar al admin principal (id=1).
	 * @throws RuntimeException         si el usuario no existe.
	 */
	public void deleteUser(Long id) {
		// Protegemos al administrador principal: no puede eliminarse nunca
		if (id == ADMIN_PROTECTED_ID) {
			throw new IllegalArgumentException("No se puede eliminar al administrador principal del sistema.");
		}

		// Verificamos que el usuario existe antes de intentar borrarlo
		if (!userRepository.existsById(id)) {
			throw new RuntimeException("El usuario con ID " + id + " no existe.");
		}

		userRepository.deleteById(id);
	}

	/**
	 * Convierte una entidad User a UserSummaryDTO.
	 * <p>
	 * Método privado de apoyo que extrae solo los campos necesarios para el panel
	 * de administración. Los roles se transforman a sus nombres (String) en un Set.
	 *
	 * @param user Entidad User de JPA.
	 * @return UserSummaryDTO con los datos resumidos del usuario.
	 */
	private UserSummaryDTO toSummaryDTO(User user) {
		// Extraemos los nombres de los roles (ej: "ROLE_USER", "ROLE_ADMIN")
		Set<String> roleNames = user.getRoles().stream()
				.map(Role::getName)
				.collect(Collectors.toSet());

		return new UserSummaryDTO(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				user.isEnabled(),
				roleNames
		);
	}

}
