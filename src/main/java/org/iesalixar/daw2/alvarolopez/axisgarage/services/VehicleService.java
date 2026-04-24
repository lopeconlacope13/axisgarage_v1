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
 * <p>
 * Actúa como intermediario entre el controlador REST y el repositorio JPA, aplicando
 * las validaciones necesarias (duplicados de modelo por propietario, existencia de
 * relaciones) antes de persistir los datos. El almacenamiento físico de imágenes
 * se delega a {@link FileStorageService}.
 * </p>
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
     *
     * @param model Modelo del vehículo a buscar.
     * @param horsePower Potencia mínima en CV.
     * @param pageable Configuración de paginación de Spring Data.
     * @return Page con listado de VehicleDTO.
     */
    public Page<VehicleDTO> getAllVehicles(String model, Integer horsePower, Pageable pageable) {
        logger.info("Solicitando Vehículos con filtros -> Modelo: {}, CV mínimos: {}", model, horsePower);
        try {
            Page<Vehicle> vehicles = vehicleRepository.findByFiltros(model, horsePower, pageable);

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

        // guardar en BD (insert) y retornar DTO
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(savedVehicle);
    }

    // --- 4. ACTUALIZAR VEHÍCULO ---

    /**
     * Edita los campos y sustituye/añade imágenes de un vehículo del registro.
     *
     * @param id ID original del vehículo.
     * @param vehicleDTO Contenido actualizado.
     * @param locale Contexto de idioma actual.
     * @return VehicleDTO resultante.
     * @throws IllegalArgumentException si los datos incumplen unicidad.
     */
    public VehicleDTO updateVehicle(Long id, VehicleDTO vehicleDTO, Locale locale) {

        // verificar que existe
        Vehicle existente = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: Vehículo no encontrado con ID: " + id));

        // regla de negocio: exclusión de duplicados
        if (vehicleRepository.existsByModelAndOwnerIdAndIdNot(
                vehicleDTO.getModel(),
                vehicleDTO.getOwnerDTO().getId(),
                id)) {
            throw new IllegalArgumentException("El propietario ya tiene OTRO vehículo con este modelo.");
        }

        // buscar al propietario
        Owner owner = ownerRepository.findById(vehicleDTO.getOwnerDTO().getId())
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado"));

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
     * Borra permanentemente el vehículo del sistema.
     *
     * @param id Identificador del coche a borrar.
     * @throws IllegalArgumentException si no se localiza el auto con el id entregado.
     */
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("El vehículo no existe.");
        }
        vehicleRepository.deleteById(id);
    }

}
