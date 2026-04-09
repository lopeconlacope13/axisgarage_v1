package org.iesalixar.daw2.alvarolopez.lopebnb.services;

import jakarta.transaction.Transactional;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.CasaRuralDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
import org.iesalixar.daw2.alvarolopez.lopebnb.mappers.CasaRuralMapper;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.CasaRuralRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.PropietarioRepository;
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
 * Servicio que gestiona toda la lógica de negocio (Business Logic) relacionada con la entidad {@link CasaRural}.
 * * Actúa como intermediario (Capa de Servicio) entre el Controlador (que recibe peticiones HTTP)
 * y los Repositorios (que acceden a la Base de Datos). Se encarga de validar reglas estrictas de negocio,
 * procesar y coordinar el guardado de múltiples imágenes, y orquestar el mapeo entre Entidades y DTOs.
 * * @author Alvaro Lopez
 */
@Service
public class CasaRuralService {

    private static final Logger logger = LoggerFactory.getLogger(CasaRuralService.class);

    @Autowired
    private CasaRuralRepository casaRuralRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private CasaRuralMapper casaRuralMapper;

    @Autowired
    private FileStorageService fileStorageService;

    // --- 1. LISTAR CON PAGINACIÓN ---

    /**
     * Recupera una lista paginada de todas las casas rurales registradas en el sistema.
     * Utiliza el {@link CasaRuralMapper} para transformar limpiamente la página de Entidades a una página de DTOs.
     *
     * @param pageable Objeto inyectado por Spring que contiene la información de paginación (página, tamaño, orden).
     * @return {@link Page} de {@link CasaRuralDTO} lista para ser enviada al cliente.
     */
    public Page<CasaRuralDTO> getAllCasasRurales(Pageable pageable) {
        logger.info("Solicitando todas las Casas Rurales con paginación: página {}, tamaño {}",
                pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<CasaRural> casasRurales = casaRuralRepository.findAll(pageable);
            logger.info("Se han encontrado {} Casas Rurales en la página actual.", casasRurales.getNumberOfElements());

            // Transformamos cada Entity de la página a DTO usando una referencia a método (method reference)
            return casasRurales.map(casaRuralMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista paginada de Casas Rurales: {}", e.getMessage());
            throw e;
        }
    }

    // --- 2. OBTENER POR ID ---

    /**
     * Busca una casa rural específica por su identificador en la base de datos.
     * Se marca como {@link Transactional} para asegurar que las colecciones perezosas (Lazy Loading)
     * se inicialicen correctamente dentro de una transacción activa antes de mapear a DTO.
     *
     * @param id Identificador único de la casa rural.
     * @return Un {@link Optional} que contiene el {@link CasaRuralDTO} si se encuentra, o vacío si no existe.
     */
    @Transactional
    public Optional<CasaRuralDTO> getCasaRuralById(Long id) {
        try {
            logger.info("Buscando Casa Rural con ID {}", id);
            return casaRuralRepository.findById(id).map(casaRuralMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar la Casa Rural con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar la Casa Rural.", e);
        }
    }

    // --- 3. CREAR CASA RURAL ---

    /**
     * Registra una nueva casa rural aplicando reglas de negocio estrictas y delegando el guardado
     * de archivos físicos al {@link FileStorageService}.
     * * REGLA DE NEGOCIO 1: Un propietario no puede registrar dos casas con el mismo nombre exacto.
     * REGLA DE NEGOCIO 2: Se debe asociar una entidad {@link Propietario} válida existente en BD.
     *
     * @param casaRuralDTO Objeto con los datos de la casa rural a crear (incluye la lista de MultipartFile).
     * @param locale Idioma de la petición para posibles mensajes de error internacionalizados.
     * @return El {@link CasaRuralDTO} resultante con su ID autogenerado y su galería de imágenes actualizada.
     * @throws IllegalArgumentException Si el propietario no existe o si se viola la regla de nombres duplicados.
     */
    public CasaRuralDTO createCasaRural(CasaRuralDTO casaRuralDTO, Locale locale) {

        // 1. Validar nombre duplicado para este dueño
        if (casaRuralRepository.existsByNombreAndPropietarioId(casaRuralDTO.getNombre(), casaRuralDTO.getPropietarioDTO().getId())) {
            throw new IllegalArgumentException("El propietario ya tiene una casa rural registrada con este nombre.");
        }

        // 2. Buscamos al propietario para enlazar la relación (Atachar a la sesión de Hibernate)
        Propietario propietario = propietarioRepository.findById(casaRuralDTO.getPropietarioDTO().getId())
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado"));

        // 3. Convertimos a entity pasándole el propietario atachado
        CasaRural casaRural = casaRuralMapper.toEntity(casaRuralDTO, propietario);

        // 4. Guardar IMÁGENES múltiples iterando sobre la lista de MultipartFile
        List<String> nombresImagenesGuardadas = new ArrayList<>();

        if (casaRuralDTO.getImageFiles() != null && !casaRuralDTO.getImageFiles().isEmpty()) {
            for (MultipartFile file : casaRuralDTO.getImageFiles()) {
                if (file != null && !file.isEmpty()) {
                    String fileName = fileStorageService.saveFile(file); // Delegamos la escritura física
                    if (fileName != null) {
                        nombresImagenesGuardadas.add(fileName);
                    }
                }
            }
            // Asignamos la lista final de UUIDs a la entidad (@ElementCollection lo mapeará a su tabla)
            casaRural.setImagenes(nombresImagenesGuardadas);
        } else {
            logger.warn("No se recibió ninguna imagen para la casa rural en la creación.");
        }

        // 5. Guardar en BD (insert) y retornar DTO
        CasaRural savedCasaRural = casaRuralRepository.save(casaRural);
        return casaRuralMapper.toDTO(savedCasaRural);
    }

    // --- 4. ACTUALIZAR CASA RURAL ---

    /**
     * Actualiza los datos de una casa rural existente.
     * Incluye una validación avanzada para evitar falsos positivos de duplicidad ("La trampa del Update")
     * y gestiona la adición de nuevas imágenes a la galería actual sin borrar las anteriores.
     *
     * @param id Identificador de la casa rural que se desea actualizar.
     * @param casaRuralDTO DTO con los nuevos datos y la posible nueva remesa de archivos de imagen.
     * @param locale Idioma de la petición para los mensajes de error.
     * @return El {@link CasaRuralDTO} con los datos y la galería actualizados.
     * @throws IllegalArgumentException Si se viola la exclusión de ID en nombres duplicados o entidades no existen.
     */
    public CasaRuralDTO updateCasaRural(Long id, CasaRuralDTO casaRuralDTO, Locale locale) {

        // 1. Verificar que la casa existe
        CasaRural existente = casaRuralRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: Casa rural no encontrada con ID: " + id));

        // 2. Regla de Negocio: Exclusión de duplicados (La trampa del update)
        // Busca si este dueño tiene OTRA casa (IdNot) que se llame igual que el nuevo nombre propuesto.
        if (casaRuralRepository.existsByNombreAndPropietarioIdAndIdNot(
                casaRuralDTO.getNombre(),
                casaRuralDTO.getPropietarioDTO().getId(),
                id)) {
            throw new IllegalArgumentException("El propietario ya tiene OTRA casa rural con este nombre.");
        }

        // 3. Buscar al propietario
        Propietario propietario = propietarioRepository.findById(casaRuralDTO.getPropietarioDTO().getId())
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado"));

        // 4. Mutar el estado de la entidad EXISTENTE (En lugar de crear un new CasaRural())
        existente.setNombre(casaRuralDTO.getNombre());
        existente.setDireccion(casaRuralDTO.getDireccion());
        existente.setPrecioNoche(casaRuralDTO.getPrecioNoche());
        existente.setCapacidadPersonas(casaRuralDTO.getCapacidadPersonas());
        existente.setPropietario(propietario);

        // 5. Guardar imágenes NUEVAS y añadirlas a la galería
        List<MultipartFile> archivosNuevos = casaRuralDTO.getImageFiles();

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
            // Agregamos las nuevas fotos a la colección de Hibernate existente
            existente.getImagenes().addAll(nombresNuevos);
        } else {
            logger.info("No se recibieron nuevas imágenes, se mantiene la galería actual.");
        }

        // 6. Guardar los cambios (update)
        CasaRural savedCasaRural = casaRuralRepository.save(existente);
        return casaRuralMapper.toDTO(savedCasaRural);
    }

    // --- 5. ELIMINAR CASA RURAL ---

    /**
     * Elimina una casa rural de la base de datos de manera física.
     * Gracias a la configuración ON DELETE CASCADE en la BD, la eliminación de la casa
     * también limpiará sus registros asociados en la tabla de imágenes (casa_rural_imagenes).
     *
     * @param id Identificador de la casa rural a borrar.
     * @throws IllegalArgumentException Si no se encuentra el ID en la base de datos antes de borrar.
     */
    public void deleteCasaRural(Long id) {
        if (!casaRuralRepository.existsById(id)) {
            throw new IllegalArgumentException("La casa rural no existe.");
        }
        casaRuralRepository.deleteById(id);
    }
}