package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import jakarta.transaction.Transactional;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.VehicleDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Location;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Vehicle;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.VehicleCategory;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Owner;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.VehicleMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.VehicleRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.VehicleCategoryRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.LocationRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Servicio que gestiona toda la lógica de negocio relacionada con los vehículos de la flota.
 * Actúa como intermediario entre el controlador REST y el repositorio JPA, aplicando
 * las validaciones necesarias (duplicados de modelo por propietario, existencia de
 * relaciones) antes de persistir los datos. El almacenamiento físico de imágenes
 * se delega a {@link FileStorageService}.
 */
@Service
public class VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private VehicleCategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private FileStorageService fileStorageService;

    // --- 1. LISTAR CON PAGINACIÓN ---

    /**
     * Obtiene una lista paginada de vehículos catalogados, con filtros opcionales.
     * Cualquier filtro que llegue como null o vacío se ignora completamente.
     *
     * @param brand      Marca del vehículo a buscar (búsqueda parcial).
     * @param model      Modelo del vehículo a buscar (búsqueda parcial).
     * @param horsePower Potencia mínima en CV.
     * @param categoryId ID de la categoría a filtrar.
     * @param locationId ID de la localización a filtrar.
     * @param pageable   Configuración de paginación de Spring Data.
     * @return Page con listado de VehicleDTO.
     */
    public Page<VehicleDTO> getAllVehicles(String search, String brand, String model, Integer horsePower, Long categoryId, Long locationId, Pageable pageable) {
        logger.info("Solicitando Vehículos con filtros -> Búsqueda: {}, Marca: {}, Modelo: {}, CV mínimos: {}, Categoría: {}, Localización: {}", search, brand, model, horsePower, categoryId, locationId);
        try {
            // Convertimos strings vacíos a null para que la query JPQL los ignore con IS NULL
            String searchFilter = (search != null && !search.isBlank()) ? search : null;
            String brandFilter  = (brand  != null && !brand.isBlank())  ? brand  : null;
            String modelFilter  = (model  != null && !model.isBlank())  ? model  : null;
            Page<Vehicle> vehicles = vehicleRepository.findByFiltros(searchFilter, brandFilter, modelFilter, horsePower, categoryId, locationId, pageable);

            logger.info("Se han encontrado {} Vehículos en la base de datos.", vehicles.getTotalElements());

            return vehicles.map(vehicleMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista paginada de Vehículos: {}", e.getMessage());
            throw e;
        }
    }

    // --- 2. OBTENER POR ID ---

    /**
     * Devuelve el detalle de un vehículo específico.
     *
     * @param id Identificador del vehículo.
     * @return Optional con los datos del vehículo, si existe.
     * @throws RuntimeException en caso de error interno.
     */
    @Transactional
    public Optional<VehicleDTO> getVehicleById(Long id) {
        try {
            logger.info("Buscando Vehículo con ID {}", id);
            return vehicleRepository.findById(id).map(vehicleMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar el Vehículo con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar el Vehículo.", e);
        }
    }

    // --- 3. CREAR VEHÍCULO ---

    /**
     * Crea un vehículo nuevo en la base de datos subiendo sus archivos adjuntos.
     *
     * @param vehicleDTO Datos enviados para el vehículo.
     * @param locale Contexto de idioma actual.
     * @return VehicleDTO con los datos e ID asignado.
     * @throws IllegalArgumentException si el modelado ya pertenece a ese mismo owner.
     */
    public VehicleDTO createVehicle(VehicleDTO vehicleDTO, Locale locale) {

        // validar duplicado para este dueño
        if (vehicleRepository.existsByModelAndOwnerId(vehicleDTO.getModel(), vehicleDTO.getOwnerDTO().getId())) {
            throw new IllegalArgumentException("El propietario ya tiene un vehículo registrado con este modelo.");
        }

        // buscamos al propietario para enlazar la relación
        Owner owner = ownerRepository.findById(vehicleDTO.getOwnerDTO().getId())
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado"));

        VehicleCategory category = categoryRepository.findById(vehicleDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        Location location = locationRepository.findById(vehicleDTO.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));

        // convertimos a entity pasándole las relaciones atachadas
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDTO, owner, category, location);

        // guardar IMÁGENES múltiples iterando sobre la lista de MultipartFile
        List<String> nombresImagenesGuardadas = new ArrayList<>();

        if (vehicleDTO.getImageFiles() != null && !vehicleDTO.getImageFiles().isEmpty()) {
            for (MultipartFile file : vehicleDTO.getImageFiles()) {
                if (file != null && !file.isEmpty()) {
                    String fileName = fileStorageService.saveFile(file); // Delegamos la escritura física
                    if (fileName != null) {
                        nombresImagenesGuardadas.add(fileName);
                    }
                }
            }
            vehicle.setImages(nombresImagenesGuardadas);
        } else {
            logger.warn("No se recibió ninguna imagen para el vehículo en la creación.");
        }

        // guardar en BD (insert) y retornar DTO con el ID asignado por JPA
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(savedVehicle);
    }

    // --- 4. ACTUALIZAR VEHÍCULO ---

    /**
     * Edita los campos y sustituye/añade imágenes de un vehículo del registro.
     * <p>
     * Si el cuerpo de la petición no incluye un propietario (ownerDTO es null),
     * se conserva el propietario que ya tenía el vehículo en base de datos.
     * Esto evita un NullPointerException cuando el formulario de edición no
     * envía el campo del owner (por ejemplo, al editar solo datos técnicos).
     * </p>
     *
     * @param id ID original del vehículo.
     * @param vehicleDTO Contenido actualizado.
     * @param locale Contexto de idioma actual.
     * @return VehicleDTO resultante.
     * @throws IllegalArgumentException si los datos incumplen unicidad o no existe el vehículo.
     */
    public VehicleDTO updateVehicle(Long id, VehicleDTO vehicleDTO, Locale locale) {

        // Buscamos el vehículo existente; si no existe, cortamos aquí con excepción clara
        Vehicle existente = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: Vehículo no encontrado con ID: " + id));

        // Determinamos el propietario final:
        // - Si el DTO trae un ownerDTO con ID válido → usamos ese nuevo propietario
        // - Si ownerDTO es null (el frontend no lo envió) → conservamos el actual de la BD
        Owner owner;
        if (vehicleDTO.getOwnerDTO() != null && vehicleDTO.getOwnerDTO().getId() != null) {
            // El formulario envió un propietario: validamos unicidad modelo-owner y buscamos la entidad
            if (vehicleRepository.existsByModelAndOwnerIdAndIdNot(
                    vehicleDTO.getModel(),
                    vehicleDTO.getOwnerDTO().getId(),
                    id)) {
                throw new IllegalArgumentException("El propietario ya tiene OTRO vehículo con este modelo.");
            }
            owner = ownerRepository.findById(vehicleDTO.getOwnerDTO().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado"));
        } else {
            // ownerDTO no vino en el body → mantenemos el propietario que ya tenía el vehículo
            logger.info("ownerDTO no recibido en el body. Se conserva el propietario actual del vehículo con ID {}", id);
            owner = existente.getOwner();
        }

        VehicleCategory category = categoryRepository.findById(vehicleDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        Location location = locationRepository.findById(vehicleDTO.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));

        // mutar el estado de la entidad EXISTENTE
        existente.setBrand(vehicleDTO.getBrand());
        existente.setModel(vehicleDTO.getModel());
        existente.setProductionYear(vehicleDTO.getProductionYear());
        existente.setPricePerDay(vehicleDTO.getPricePerDay());
        existente.setEngineType(vehicleDTO.getEngineType());
        existente.setHorsePower(vehicleDTO.getHorsePower());
        existente.setTorqueNm(vehicleDTO.getTorqueNm());
        existente.setTransmission(vehicleDTO.getTransmission());
        existente.setDrivetrain(vehicleDTO.getDrivetrain());
        existente.setFuelType(vehicleDTO.getFuelType());
        existente.setZeroToHundred(vehicleDTO.getZeroToHundred());
        existente.setDescription(vehicleDTO.getDescription());
        existente.setAvailable(vehicleDTO.getAvailable());
        existente.setOwner(owner);
        existente.setCategory(category);
        existente.setLocation(location);

        // guardar imágenes NUEVAS y añadirlas a la galería
        List<MultipartFile> archivosNuevos = vehicleDTO.getImageFiles();

        if (archivosNuevos != null && !archivosNuevos.isEmpty()) {
            List<String> nombresNuevos = new ArrayList<>();
            for (MultipartFile archivo : archivosNuevos) {
                if (archivo != null && !archivo.isEmpty()) {
                    String fileName = fileStorageService.saveFile(archivo);
                    if (fileName != null) {
                        nombresNuevos.add(fileName);
                    }
                }
            }
            // agregamos las nuevas fotos
            existente.getImages().addAll(nombresNuevos);
        } else {
            logger.info("No se recibieron nuevas imágenes, se mantiene la galería actual.");
        }

        // guardar los cambios (update)
        Vehicle savedVehicle = vehicleRepository.save(existente);
        return vehicleMapper.toDTO(savedVehicle);
    }

    // --- 5. ELIMINAR VEHÍCULO ---

    /**
     * Borra permanentemente el vehículo y todas sus reservas asociadas del sistema.
     * <p>
     * Gracias a la anotación {@code @OneToMany(cascade = CascadeType.ALL)} definida
     * en la entidad {@link org.iesalixar.daw2.alvarolopez.axisgarage.entities.Vehicle},
     * JPA elimina automáticamente en cascada todas las reservas cuyo campo
     * {@code vehicle_id} apunte a este vehículo. No es necesario borrarlas a mano.
     * </p>
     * <p>
     * IMPORTANTE: primero se verifica que el vehículo exista. Si no existe, lanzamos
     * una excepción controlada para que el controlador devuelva un 404 limpio,
     * en lugar de dejar que JPA falle en silencio.
     * </p>
     *
     * @param id Identificador del coche a borrar.
     * @throws IllegalArgumentException si no se localiza el vehículo con el id entregado.
     */
    @Transactional
    public void deleteVehicle(Long id) {
        // Comprobamos primero que el vehículo existe para dar un error descriptivo
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("El vehículo no existe.");
        }
        // deleteById dispara el borrado en cascada de reservas asociadas (CascadeType.ALL)
        vehicleRepository.deleteById(id);
        logger.info("Vehículo con ID {} eliminado correctamente junto con sus reservas asociadas.", id);
    }

    // --- 6. AÑADIR IMAGEN A UN VEHÍCULO ---

    /**
     * Añade una imagen al vehículo indicado guardándola en disco y registrando
     * su nombre en la tabla vehicle_images.
     *
     * @param id   ID del vehículo al que se asocia la imagen.
     * @param file Archivo de imagen enviado desde el formulario.
     * @return VehicleDTO actualizado con la nueva imagen incluida.
     * @throws IllegalArgumentException si el vehículo no existe.
     */
    public VehicleDTO addImage(Long id, MultipartFile file) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
        // Guardamos el archivo en disco y obtenemos su nombre único
        String fileName = fileStorageService.saveFile(file);
        // Añadimos el nombre al array de imágenes existente (no reemplazamos)
        vehicle.getImages().add(fileName);
        return vehicleMapper.toDTO(vehicleRepository.save(vehicle));
    }

    // --- 7. ELIMINAR IMAGEN DE UN VEHÍCULO ---

    /**
     * Elimina una imagen concreta del vehículo: la borra del disco y la
     * elimina de la lista persistida en vehicle_images.
     *
     * @param id       ID del vehículo propietario de la imagen.
     * @param filename Nombre del archivo a eliminar.
     * @return VehicleDTO actualizado sin la imagen eliminada.
     * @throws IllegalArgumentException si el vehículo no existe.
     */
    public VehicleDTO deleteImage(Long id, String filename) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
        // Borramos el archivo físico del disco
        fileStorageService.deleteFile(filename);
        // Eliminamos la referencia de la lista en base de datos
        vehicle.getImages().remove(filename);
        return vehicleMapper.toDTO(vehicleRepository.save(vehicle));
    }

    // --- 8. REORDENAR IMÁGENES ---

    /**
     * Reemplaza el orden de las imágenes del vehículo por el nuevo orden recibido.
     * Valida que el listado enviado contenga exactamente los mismos nombres de archivo
     * que ya tiene el vehículo — no se pueden añadir ni eliminar imágenes aquí.
     *
     * @param id        ID del vehículo.
     * @param filenames Lista de nombres de archivo en el nuevo orden deseado.
     * @return VehicleDTO actualizado con las imágenes en el orden nuevo.
     * @throws IllegalArgumentException si el vehículo no existe o los nombres no coinciden.
     */
    public VehicleDTO reorderImages(Long id, List<String> filenames) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
        // Comprobamos que el nuevo listado sea exactamente el mismo conjunto (sin añadir ni borrar)
        List<String> actuales = new ArrayList<>(vehicle.getImages());
        if (!actuales.containsAll(filenames) || !filenames.containsAll(actuales)) {
            throw new IllegalArgumentException("La lista de imágenes no coincide con las existentes.");
        }
        // Vaciamos y rellenamos en el nuevo orden — JPA detecta el cambio y persiste
        vehicle.getImages().clear();
        vehicle.getImages().addAll(filenames);
        return vehicleMapper.toDTO(vehicleRepository.save(vehicle));
    }

    /**
     * Alterna el estado de disponibilidad de un vehículo (disponible ↔ no disponible).
     * Útil para el panel de gestión del MANAGER, sin necesidad de subir imágenes.
     *
     * @param id Identificador del vehículo.
     * @return DTO actualizado con el nuevo valor de 'available'.
     * @throws IllegalArgumentException si el vehículo no existe.
     */
    public VehicleDTO toggleAvailability(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
        vehicle.setAvailable(!vehicle.getAvailable());
        logger.info("Disponibilidad del vehículo {} cambiada a {}", id, vehicle.getAvailable());
        return vehicleMapper.toDTO(vehicleRepository.save(vehicle));
    }

}
