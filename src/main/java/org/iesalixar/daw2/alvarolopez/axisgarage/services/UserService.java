package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.RegisterRequestDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.UserDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Role;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.User;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.UserMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.RoleRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

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

}
