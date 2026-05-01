package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.RegisterRequestDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.UserDTO;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio central de gestión de usuarios en Axis Garage.
 * <p>
 * Cubre el registro de nuevos usuarios, la consulta por ID o email,
 * el cambio de contraseña autenticado y el flujo completo de recuperación
 * de contraseña por email (forgot-password / reset-password).
 */
@Service
public class UserService {
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

	/** URL base del frontend. Se usa para construir el enlace de recuperación de contraseña. */
	@Value("${FRONTEND_URL:http://localhost:4200}")
	private String frontendUrl;

	public Long getIdByUsername(String username) {
		return userRepository.getIdByUsername(username);
	}

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

		Role rolUser = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new RuntimeException("El rol ROLE_USER no existe en la base de datos."));

		User nuevo = new User();
		nuevo.setUsername(dto.getEmail().split("@")[0]);
		nuevo.setEmail(dto.getEmail());
		nuevo.setPassword(passwordEncoder.encode(dto.getPassword()));
		nuevo.setFirstName(dto.getFirstName());
		nuevo.setLastName(dto.getLastName());
		nuevo.setEnabled(true);
		nuevo.setRoles(Set.of(rolUser));

		User guardado = userRepository.save(nuevo);
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
		user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
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

		// Asociamos el nombre de archivo al usuario y lo persistimos
		user.setImage(filename);
		userRepository.save(user);

		return userMapper.toDTO(user);
	}

}
