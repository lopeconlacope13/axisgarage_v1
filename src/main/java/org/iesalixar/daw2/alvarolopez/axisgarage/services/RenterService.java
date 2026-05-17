package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.RenterDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Renter;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.User;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.RenterMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.RenterRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio que gestiona el ciclo de vida de los huéspedes (clientes que realizan reservas).
 * Implementa las operaciones CRUD con paginación, garantizando la unicidad del DNI,
 * email y teléfono de cada cliente. Lanza excepciones descriptivas ante cualquier
 * conflicto de datos para que el controlador pueda devolver respuestas HTTP claras.
 */
@Service
public class RenterService {

    private static final Logger logger = LoggerFactory.getLogger(RenterService.class);

    @Autowired
    private RenterRepository renterRepository;

    @Autowired
    private RenterMapper renterMapper;

    @Autowired
    private UserRepository userRepository;

    // --- 1. LISTAR CON PAGINACIÓN ---

    /**
     * Obtiene una lista paginada de huéspedes, permitiendo filtrar por nombre y DNI.
     *
     * @param name Nombre del huésped (opcional).
     * @param dni DNI del huésped (opcional).
     * @param pageable Configuración de paginación y ordenación.
     * @return Page con los DTOs de los huéspedes encontrados.
     * @throws RuntimeException si ocurre un error interno al listar.
     */
    public Page<RenterDTO> getAllRenters(String name, String dni, Pageable pageable) {
        try {
            logger.info("Solicitando huéspedes con filtros -> Nombre: {}, DNI: {}", name, dni);

            Page<Renter> renters = renterRepository.findByFiltros(name, dni, pageable);
            logger.info("Se han encontrado {} huéspedes.", renters.getNumberOfElements());

            return renters.map(renterMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista de huéspedes: {}", e.getMessage());
            throw new RuntimeException("Error interno al listar los huéspedes", e);
        }
    }

    // --- 2. OBTENER UNO POR ID ---

    /**
     * Busca un huésped específico por su identificador único.
     *
     * @param id Identificador del huésped.
     * @return Optional con el DTO del huésped si existe, o vacío en caso contrario.
     * @throws RuntimeException si ocurre un error conectando con la base de datos.
     */
    public Optional<RenterDTO> getRenterById(Long id) {
        try {
            logger.info("Buscando huésped con ID {}", id);
            return renterRepository.findById(id).map(renterMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar el huésped con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar el huésped.", e);
        }
    }

    // --- 3. CREAR HUÉSPED ---

    /**
     * Registra un nuevo huésped verificando restricciones de unicidad en DNI, Email y Teléfono.
     * Si el DNI no es un placeholder, valida también la letra de control mediante el algoritmo oficial.
     *
     * @param renterDTO Datos enviados para el nuevo huésped.
     * @return RenterDTO con los datos guardados en base de datos.
     * @throws IllegalArgumentException si DNI, email o teléfono ya están registrados o el DNI es inválido.
     * @throws RuntimeException si ocurre un fallo general al guardar.
     */
    public RenterDTO createRenter(@Valid RenterDTO renterDTO) {
        try {
            logger.info("Creando nuevo huésped con DNI: {}", renterDTO.getDni());

            // Validación real del DNI (solo si no es placeholder de sistema)
            if (renterDTO.getDni() != null && !renterDTO.getDni().startsWith("PENDING-")
                    && !validarDNI(renterDTO.getDni())) {
                throw new IllegalArgumentException("El DNI proporcionado no es válido.");
            }

            // Reglas de negocio: unicidad
            if (renterRepository.findByDni(renterDTO.getDni()).isPresent()) {
                throw new IllegalArgumentException("El DNI ya está registrado.");
            }
            if (renterRepository.findByEmail(renterDTO.getEmail()).isPresent()) {
                throw new IllegalArgumentException("El email ya está registrado.");
            }
            if (renterRepository.findByPhone(renterDTO.getPhone()).isPresent()) {
                throw new IllegalArgumentException("El teléfono ya está registrado.");
            }

            Renter renter = renterMapper.toEntity(renterDTO);
            Renter savedRenter = renterRepository.save(renter);

            logger.info("Huésped creado exitosamente con ID {}", savedRenter.getId());
            return renterMapper.toDTO(savedRenter);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al crear huésped: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear el huésped: {}", e.getMessage());
            throw new RuntimeException("Error interno al crear el huésped.");
        }
    }

    // --- 4. ACTUALIZAR HUÉSPED ---

    /**
     * Actualiza un huésped, comprobando que las modificaciones de DNI/Email/Teléfono no colisionen con otros.
     * Si se proporciona un DNI real (no placeholder), valida la letra de control.
     *
     * @param id Identificador del huésped a actualizar.
     * @param renterDTO Nuevos datos del huésped.
     * @return RenterDTO con el registro actualizado.
     * @throws IllegalArgumentException si se detecta duplicado en campos únicos, DNI inválido o no existe el huésped.
     * @throws RuntimeException para cualquier otro error de proceso.
     */
    public RenterDTO updateRenter(Long id, @Valid RenterDTO renterDTO) {
        try {
            logger.info("Actualizando huésped con ID {}", id);

            Renter existente = renterRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Huésped no encontrado con ID: " + id));

            // Validación real del DNI (solo si no es placeholder de sistema)
            if (renterDTO.getDni() != null && !renterDTO.getDni().startsWith("PENDING-")
                    && !validarDNI(renterDTO.getDni())) {
                throw new IllegalArgumentException("El DNI proporcionado no es válido.");
            }

            // Lógica de exclusión para evitar falsos duplicados
            Optional<Renter> porDni = renterRepository.findByDni(renterDTO.getDni());
            if (porDni.isPresent() && !porDni.get().getId().equals(id)) {
                throw new IllegalArgumentException("El DNI ya pertenece a otro huésped.");
            }

            Optional<Renter> porEmail = renterRepository.findByEmail(renterDTO.getEmail());
            if (porEmail.isPresent() && !porEmail.get().getId().equals(id)) {
                throw new IllegalArgumentException("El email ya pertenece a otro huésped.");
            }

            Optional<Renter> porPhone = renterRepository.findByPhone(renterDTO.getPhone());
            if (porPhone.isPresent() && !porPhone.get().getId().equals(id)) {
                throw new IllegalArgumentException("El teléfono ya pertenece a otro huésped.");
            }

            existente.setName(renterDTO.getName());
            existente.setLastName(renterDTO.getLastName());
            existente.setEmail(renterDTO.getEmail());
            existente.setDni(renterDTO.getDni());
            existente.setPhone(renterDTO.getPhone());
            existente.setAddress(renterDTO.getAddress());

            Renter updatedRenter = renterRepository.save(existente);
            logger.info("Huésped actualizado con éxito (ID {})", id);
            return renterMapper.toDTO(updatedRenter);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al actualizar huésped: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el huésped: {}", e.getMessage());
            throw new RuntimeException("Error interno al actualizar.");
        }
    }

    // --- 5. BORRAR ---

    /**
     * Borra el registro de un huésped del sistema.
     *
     * @param id Identificador único del huésped.
     * @throws IllegalArgumentException si el ID indicado no corresponde a un huésped válido.
     * @throws RuntimeException ante fallos en la capa de persistencia.
     */
    public void deleteRenter(Long id) {
        try {
            logger.info("Borrando huésped con ID {}", id);
            if (!renterRepository.existsById(id)) {
                throw new IllegalArgumentException("Huésped no encontrado con ID: " + id);
            }
            renterRepository.deleteById(id);
            logger.info("Huésped borrado correctamente.");

        } catch (IllegalArgumentException e) {
            logger.warn("No se pudo borrar el huésped: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al borrar el huésped: {}", e.getMessage());
            throw new RuntimeException("Error interno al borrar el huésped.");
        }
    }

    // --- 6. ASEGURAR PERFIL DE HUÉSPED PARA UN USUARIO ---

    /**
     * Garantiza la existencia de un perfil de Renter asociado al usuario indicado.
     * Si ya existe un Renter con el email del User, lo devuelve (y opcionalmente
     * actualiza con los datos del DTO si éste no es null).
     * Si no existe, crea uno nuevo automáticamente reutilizando el nombre,
     * apellidos y email del User. Los campos obligatorios DNI y teléfono se
     * rellenan con valores placeholder únicos derivados del ID del usuario
     * a menos que el DTO traiga valores reales.
     * Este método se llama desde el flujo de checkout para que cualquier
     * usuario registrado pueda reservar. Si el checkout envía DNI/address,
     * se validan y guardan en el mismo paso.
     *
     * @param userId ID del usuario autenticado (extraído del JWT).
     * @param dto    Datos opcionales para actualizar/crear (puede ser null).
     * @return RenterDTO existente o recién creado.
     */
    public RenterDTO ensureRenterFromUser(Long userId, RenterDTO dto) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        Optional<Renter> existing = renterRepository.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            Renter renter = existing.get();
            // Si el checkout envía datos de facturación, los aplicamos aquí
            if (dto != null) {
                if (dto.getDni() != null && !dto.getDni().isBlank()) {
                    if (!validarDNI(dto.getDni())) {
                        throw new IllegalArgumentException("El DNI proporcionado no es válido.");
                    }
                    Optional<Renter> otro = renterRepository.findByDni(dto.getDni());
                    if (otro.isPresent() && !otro.get().getId().equals(renter.getId())) {
                        throw new IllegalArgumentException("El DNI ya pertenece a otro huésped.");
                    }
                    renter.setDni(dto.getDni());
                }
                if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
                    Optional<Renter> otro = renterRepository.findByPhone(dto.getPhone());
                    if (otro.isPresent() && !otro.get().getId().equals(renter.getId())) {
                        throw new IllegalArgumentException("El teléfono ya pertenece a otro huésped.");
                    }
                    renter.setPhone(dto.getPhone());
                }
                if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
                    renter.setAddress(dto.getAddress());
                }
                renter = renterRepository.save(renter);
                logger.info("Renter actualizado (id {}) con datos de facturación", renter.getId());
            }
            return renterMapper.toDTO(renter);
        }

        // No existe: lo creamos
        Renter nuevo = new Renter();
        nuevo.setName(user.getFirstName() != null ? user.getFirstName() : "Cliente");
        nuevo.setLastName(user.getLastName() != null ? user.getLastName() : "Axis");
        nuevo.setEmail(user.getEmail());

        if (dto != null && dto.getDni() != null && !dto.getDni().isBlank()) {
            if (!validarDNI(dto.getDni())) {
                throw new IllegalArgumentException("El DNI proporcionado no es válido.");
            }
            nuevo.setDni(dto.getDni());
        } else {
            nuevo.setDni("PENDING-" + userId);
        }

        if (dto != null && dto.getPhone() != null && !dto.getPhone().isBlank()) {
            nuevo.setPhone(dto.getPhone());
        } else {
            nuevo.setPhone(String.format("%09d", 900000000L + userId));
        }

        if (dto != null && dto.getAddress() != null && !dto.getAddress().isBlank()) {
            nuevo.setAddress(dto.getAddress());
        }

        Renter saved = renterRepository.save(nuevo);
        logger.info("Renter auto-creado (id {}) para el User id {}", saved.getId(), userId);
        return renterMapper.toDTO(saved);
    }

    /**
     * Valida un DNI español utilizando el algoritmo oficial de letra de control.
     * Formato esperado: 8 dígitos seguidos de una letra (ej: 12345678Z).
     *
     * @param dni DNI a validar.
     * @return true si el formato y la letra de control son correctos.
     */
    private boolean validarDNI(String dni) {
        if (dni == null || !dni.matches("^[0-9]{8}[A-Za-z]$")) {
            return false;
        }
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        int numero = Integer.parseInt(dni.substring(0, 8));
        char letraEsperada = letras.charAt(numero % 23);
        return letraEsperada == Character.toUpperCase(dni.charAt(8));
    }
}
