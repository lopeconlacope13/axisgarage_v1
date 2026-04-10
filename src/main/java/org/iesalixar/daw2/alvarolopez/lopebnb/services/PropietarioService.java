package org.iesalixar.daw2.alvarolopez.lopebnb.services;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.PropietarioDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
import org.iesalixar.daw2.alvarolopez.lopebnb.mappers.PropietarioMapper;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.PropietarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PropietarioService {

	private static final Logger logger = LoggerFactory.getLogger(PropietarioService.class);

	@Autowired
	private PropietarioRepository propietarioRepository;

	@Autowired
	private PropietarioMapper propietarioMapper;

	// --- 1. LISTAR CON PAGINACIÓN ---

	/**
	 * a l     * Recupera una lista paginada de todos los propietarios registrados con filtros opcionales.
	 * Permite buscar por nombre (incluyendo apellidos) y email de forma parcial (insensible a mayúsculas).
	 *
	 * @param nombre   Filtro parcial por nombre o apellidos (opcional, null para no filtrar).
	 * @param email    Filtro parcial por email (opcional, null para no filtrar).
	 * @param pageable Objeto inyectado con los parámetros de paginación (tamaño, orden, página).
	 * @return {@link Page} de {@link PropietarioDTO} con los datos de los propietarios.
	 * @throws RuntimeException Si ocurre un error inesperado al consultar la base de datos.
	 */
	public Page<PropietarioDTO> getAllPropietarios(String nombre, String email, Pageable pageable) {
		try {
			logger.info("Solicitando propietarios con filtros -> Nombre: {}, Email: {}", nombre, email);

			Page<Propietario> propietarios = propietarioRepository.findByFiltros(nombre, email, pageable);
			logger.info("Se han encontrado {} propietarios.", propietarios.getNumberOfElements());

			return propietarios.map(propietarioMapper::toDTO);
		} catch (Exception e) {
			logger.error("Error al obtener la lista de propietarios: {}", e.getMessage());
			throw new RuntimeException("Error interno al listar los propietarios", e);
		}
	}

	// --- 2. OBTENER UNO POR ID ---

	/**
	 * Busca un propietario específico por su identificador único.
	 *
	 * @param id Identificador del propietario a buscar.
	 * @return Un {@link Optional} que contiene el {@link PropietarioDTO} si se encuentra, o vacío si no existe.
	 * @throws RuntimeException Si falla la conexión o consulta a la base de datos.
	 */
	public Optional<PropietarioDTO> getPropietarioById(Long id) {
		try {
			logger.info("Buscando propietario con ID {}", id);
			return propietarioRepository.findById(id).map(propietarioMapper::toDTO);
		} catch (Exception e) {
			logger.error("Error al buscar el propietario con ID {}: {}", id, e.getMessage());
			throw new RuntimeException("Error al buscar el propietario.", e);
		}
	}

	// --- 3. CREAR PROPIETARIO ---

	/**
	 * Registra un nuevo propietario en el sistema tras validar sus datos de contacto.
	 * * REGLA DE NEGOCIO: No pueden existir dos propietarios con el mismo email ni el mismo teléfono.
	 *
	 * @param propietarioDTO Objeto con los datos del propietario a crear. Validado previamente por Spring.
	 * @return El {@link PropietarioDTO} del propietario recién persistido (con su ID generado).
	 * @throws IllegalArgumentException Si el email o el teléfono ya existen en la base de datos.
	 */
	public PropietarioDTO createPropietario(@Valid PropietarioDTO propietarioDTO) {
		try {
			logger.info("Creando nuevo propietario con email: {}", propietarioDTO.getEmail());

			// Comprobamos que el email y el teléfono deben ser únicos
			if (propietarioRepository.findByEmail(propietarioDTO.getEmail()).isPresent()) {
				throw new IllegalArgumentException("Error: El email '" + propietarioDTO.getEmail() + "' ya está registrado.");
			}
			if (propietarioRepository.findByTelefono(propietarioDTO.getTelefono()).isPresent()) {
				throw new IllegalArgumentException("Error: El teléfono '" + propietarioDTO.getTelefono() + "' ya está registrado.");
			}

			Propietario propietario = propietarioMapper.toEntity(propietarioDTO);
			Propietario savedPropietario = propietarioRepository.save(propietario);

			logger.info("Propietario creado exitosamente con ID {}", savedPropietario.getId());
			return propietarioMapper.toDTO(savedPropietario);

		} catch (IllegalArgumentException e) {
			// Si es un error de validación propio, se relanza tal cual para que el Controlador devuelva un 400 Bad Request
			logger.warn("Validación fallida al crear: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error inesperado al crear el propietario: {}", e.getMessage());
			throw new RuntimeException("Error interno al crear el propietario.");
		}
	}

	// --- 4. ACTUALIZAR PROPIETARIO ---

	/**
	 * Actualiza la información de un propietario existente, garantizando la unicidad de sus
	 * nuevos datos de contacto frente al resto de usuarios.
	 * * Aplica la lógica de exclusión de ID ("trampa del update") para permitir que el propietario
	 * mantenga su propio email/teléfono al actualizar otros campos como el nombre.
	 *
	 * @param id             Identificador del propietario a modificar.
	 * @param propietarioDTO DTO con los nuevos datos a aplicar.
	 * @return El {@link PropietarioDTO} actualizado.
	 * @throws IllegalArgumentException Si no existe el propietario, o si el nuevo email/teléfono ya pertenece a OTRO usuario.
	 */
	public PropietarioDTO updatePropietario(Long id, @Valid PropietarioDTO propietarioDTO) {
		try {
			logger.info("Actualizando propietario con ID {}", id);

			// 1. Comprobamos que el propietario existe
			Propietario existente = propietarioRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Error: El propietario con ID " + id + " no existe."));

			// 2. LÓGICA DE NEGOCIO (Excluyendo al propio usuario)
			Optional<Propietario> porEmail = propietarioRepository.findByEmail(propietarioDTO.getEmail());
			if (porEmail.isPresent() && !porEmail.get().getId().equals(id)) {
				throw new IllegalArgumentException("Error: El email ya pertenece a otro propietario.");
			}

			Optional<Propietario> porTelefono = propietarioRepository.findByTelefono(propietarioDTO.getTelefono());
			if (porTelefono.isPresent() && !porTelefono.get().getId().equals(id)) {
				throw new IllegalArgumentException("Error: El teléfono ya pertenece a otro propietario.");
			}

			// 3. Actualizamos los datos
			existente.setNombre(propietarioDTO.getNombre());
			existente.setApellidos(propietarioDTO.getApellidos());
			existente.setEmail(propietarioDTO.getEmail());
			existente.setTelefono(propietarioDTO.getTelefono());

			// 4. Guardamos y devolvemos mapeado
			Propietario updatedPropietario = propietarioRepository.save(existente);
			logger.info("Propietario actualizado con éxito (ID {})", id);
			return propietarioMapper.toDTO(updatedPropietario);

		} catch (IllegalArgumentException e) {
			logger.warn("Validación fallida al actualizar: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error inesperado al actualizar el propietario: {}", e.getMessage());
			throw new RuntimeException("Error interno al actualizar.");
		}
	}

	// --- 5. BORRAR ---

	/**
	 * Elimina a un propietario del sistema tras comprobar que existe.
	 * Si este propietario tiene casas rurales asociadas, la base de datos (según su configuración
	 * de restricciones) impedirá el borrado o borrará en cascada.
	 *
	 * @param id Identificador del propietario a eliminar.
	 * @throws IllegalArgumentException Si el identificador no corresponde a ningún propietario registrado.
	 */
	public void deletePropietario(Long id) {
		try {
			logger.info("Borrando propietario con ID {}", id);
			if (!propietarioRepository.existsById(id)) {
				throw new IllegalArgumentException("Propietario no encontrado con ID: " + id);
			}
			propietarioRepository.deleteById(id);
			logger.info("Propietario borrado correctamente.");

		} catch (IllegalArgumentException e) {
			logger.warn("No se pudo borrar el propietario: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error al borrar el propietario: {}", e.getMessage());
			throw new RuntimeException("Error interno al borrar el propietario.");
		}
	}
}