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
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

//PENDIENTE REVISAR MESSAGE SOURCE PARA LOS ERRORES DE VALIDACIÓN DE NEGOCIO EN EL CREATE Y UPDATE (NOMBRE DUPLICADO PARA EL MISMO PROPIETARIO)


/**
 * Servicio que gestiona toda la lógica de negocio relacionada con la entidad CasaRural.
 * Actúa como intermediario entre el Controlador (CasaRuralController) y el acceso a datos (CasaRuralRepository).
 * Se encarga de validar reglas de negocio, procesar imágenes y coordinar los mapeos a DTOs.
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
     *
     * @param pageable Objeto que contiene la información de paginación (número de página, tamaño, orden).
     * @return Page<CasaRuralDTO> Página que contiene los DTOs de las casas rurales.
     */
    public Page<CasaRuralDTO> getAllCasasRurales(Pageable pageable) {
        logger.info("Solicitando todas las Casas Rurales con paginación: página {}, tamaño {}",
                pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<CasaRural> casasRurales = casaRuralRepository.findAll(pageable);
            logger.info("Se han encontrado {} Casas Rurales en la página actual.", casasRurales.getNumberOfElements());
            // Transformamos cada Entity de la página a DTO usando el Mapper
            return casasRurales.map(casaRuralMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista paginada de Casas Rurales: {}", e.getMessage());
            throw e;
        }
    }

// --- 2. OBTENER POR ID ---

    /**
     * Busca una casa rural específica por su identificador.
     *
     * @param id Identificador único de la casa rural.
     * @return Optional<CasaRuralDTO> Un Optional que contiene el DTO si se encuentra, o vacío si no existe.
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
     * Registra una nueva casa rural en la base de datos tras validar las reglas de negocio,
     * y gestiona el almacenamiento físico de su imagen asociada.
     *
     * @param casaRuralDTO Objeto con los datos de la casa rural a crear (incluye el archivo de imagen).
     * @param locale Idioma de la petición para obtener los mensajes de error internacionalizados.
     * @return CasaRuralDTO El DTO de la casa rural recién guardada, incluyendo su ID autogenerado.
     * @throws IllegalArgumentException Si el propietario no existe o si ya tiene otra casa con ese mismo nombre.
     */
    public CasaRuralDTO createCasaRural(CasaRuralDTO casaRuralDTO, Locale locale) {

        /** LOGICA PENDIENTE AL CREAR*/


        /*// 1. Validar nombre duplicado para este dueño
        if (casaRuralRepository.existsByNombreAndPropietarioId(casaRuralDTO.getNombre(), casaRuralDTO.getPropietarioDTO().getId())) {
            String errorMessage = getMessage("msg.casarural.insert.nameExistForOwner", null, locale);
            throw new IllegalArgumentException(errorMessage);
        }*/



        // 2. Buscamos al propietario para enlazar la relación
        Propietario propietario = propietarioRepository.findById(casaRuralDTO.getPropietarioDTO().getId())
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado"));

        // 3. Convertimos a entity pasándole el propietario atachado
        CasaRural casaRural = casaRuralMapper.toEntity(casaRuralDTO, propietario);

        // 4. Guardar imagen físicamente en el servidor
        if (casaRuralDTO.getImageFile() != null && !casaRuralDTO.getImageFile().isEmpty()) {
            String fileName = fileStorageService.saveFile(casaRuralDTO.getImageFile());
            if (fileName != null) {
                casaRural.setImagen(fileName); // Guardamos el nombre del archivo en la base de datos
            }
        } else {
            logger.warn("No se recibió ninguna imagen para la casa rural en la creación.");
        }

        // 5. Guardar en base de datos y devolver como DTO
        CasaRural savedCasaRural = casaRuralRepository.save(casaRural);
        return casaRuralMapper.toDTO(savedCasaRural);
    }

    // --- 4. ACTUALIZAR CASA RURAL ---

    /**
     * Actualiza los datos de una casa rural existente.
     * Aplica la exclusión del ID propio en la validación de nombres para permitir actualizaciones parciales
     * sin levantar falsos positivos de duplicidad.
     *
     * @param id Identificador de la casa rural que se desea actualizar.
     * @param casaRuralDTO Objeto con los nuevos datos y la posible nueva imagen.
     * @param locale Idioma de la petición para los mensajes de error.
     * @return CasaRuralDTO El DTO actualizado.
     * @throws IllegalArgumentException Si la casa no existe, si el propietario no existe, o si el nombre ya lo usa OTRA casa del mismo dueño.
     */
    public CasaRuralDTO updateCasaRural(Long id, CasaRuralDTO casaRuralDTO, Locale locale) {

        // 1. Verificar que la casa existe usando el ID de la URL
        CasaRural existente = casaRuralRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: Casa rural no encontrada con ID: " + id));

        // 2. Regla de Negocio: Exclusión de duplicados (La trampa del update)
        // Comprobamos si el dueño tiene OTRA casa distinta con el nombre propuesto.
        if (casaRuralRepository.existsByNombreAndPropietarioIdAndIdNot(
                casaRuralDTO.getNombre(),
                casaRuralDTO.getPropietarioDTO().getId(),
                id)) {
            //String errorMessage = messageSource.getMessage("msg.casarural.insert.nameExistForOwner", null, locale);
            throw new IllegalArgumentException();
        }

        // 3. Buscar al propietario
        Propietario propietario = propietarioRepository.findById(casaRuralDTO.getPropietarioDTO().getId())
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado"));

        // 4. Actualizar los datos sobre la entidad EXISTENTE (Sin crear un nuevo objeto)
        existente.setNombre(casaRuralDTO.getNombre());
        existente.setDireccion(casaRuralDTO.getDireccion());
        existente.setPrecioNoche(casaRuralDTO.getPrecioNoche());
        existente.setCapacidadPersonas(casaRuralDTO.getCapacidadPersonas());
        existente.setPropietario(propietario);

        // 5. Guardar imagen SOLO SI EL USUARIO HA SUBIDO UNA NUEVA
        if (casaRuralDTO.getImageFile() != null && !casaRuralDTO.getImageFile().isEmpty()) {
            String fileName = fileStorageService.saveFile(casaRuralDTO.getImageFile());
            if (fileName != null) {
                existente.setImagen(fileName); // Sobreescribe la referencia a la imagen vieja
            }
        } else {
            logger.info("No se recibió nueva imagen, se mantiene la actual.");
        }

        // 6. Guardar los cambios en BBDD y devolver
        CasaRural savedCasaRural = casaRuralRepository.save(existente);
        return casaRuralMapper.toDTO(savedCasaRural);
    }

    // --- 5. ELIMINAR CASA RURAL ---

    /**
     * Elimina una casa rural de la base de datos tras verificar su existencia.
     *
     * @param id Identificador de la casa rural a borrar.
     * @throws IllegalArgumentException Si no existe ninguna casa rural con el ID proporcionado.
     */
    public void deleteCasaRural(Long id) {
        if (!casaRuralRepository.existsById(id)) {
            throw new IllegalArgumentException("La casa rural no existe.");
        }

        casaRuralRepository.deleteById(id);
    }
}