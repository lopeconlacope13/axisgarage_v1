package org.iesalixar.daw2.alvarolopez.lopebnb.services;

import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.UserDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.User;
import org.iesalixar.daw2.alvarolopez.lopebnb.mappers.UserMapper;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
