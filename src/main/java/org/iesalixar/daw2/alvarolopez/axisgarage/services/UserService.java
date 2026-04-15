package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.UserDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.User;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.UserMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserMapper userMapper;

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
