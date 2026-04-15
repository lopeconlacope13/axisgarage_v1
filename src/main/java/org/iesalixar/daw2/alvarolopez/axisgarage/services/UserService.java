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

}
