package org.iesalixar.daw2.alvarolopez.lopebnb.services;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.OpinionDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Huesped;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Opinion;
import org.iesalixar.daw2.alvarolopez.lopebnb.mappers.OpinionMapper;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.CasaRuralRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.HuespedRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.OpinionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class OpinionService {

    private static final Logger logger = LoggerFactory.getLogger(OpinionService.class);

    @Autowired
    private OpinionRepository opinionRepository;

    @Autowired
    private CasaRuralRepository casaRuralRepository;

    @Autowired
    private HuespedRepository huespedRepository;

    @Autowired
    private OpinionMapper opinionMapper;

    // --- 1. LISTAR CON PAGINACIÓN ---

    /**
     * Recupera una lista paginada de todas las opiniones registradas.
     *
     * @param pageable Objeto inyectado con los parámetros de paginación (tamaño, orden, página).
     * @return {@link Page} de {@link OpinionDTO} con las opiniones.
     */
    public Page<OpinionDTO> getAllOpiniones(Integer puntuacionMinima, Long casaRuralId, Pageable pageable) {
        try {
            logger.info("Solicitando opiniones con filtros -> Puntuación Mínima: {}, Casa ID: {}",
                    puntuacionMinima, casaRuralId);

            Page<Opinion> opiniones = opinionRepository.findByFiltros(puntuacionMinima, casaRuralId, pageable);
            logger.info("Se han encontrado {} opiniones.", opiniones.getTotalElements());

            return opiniones.map(opinionMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista paginada de opiniones: {}", e.getMessage());
            throw new RuntimeException("Error interno al listar las opiniones", e);
        }
    }

    // --- 2. OBTENER UNA POR ID ---

    /**
     * Busca una opinión específica por su identificador único.
     *
     * @param id Identificador de la opinión a buscar.
     * @return Un {@link Optional} que contiene el {@link OpinionDTO} si se encuentra, o vacío si no existe.
     */
    public Optional<OpinionDTO> getOpinionById(Long id) {
        try {
            logger.info("Buscando opinión con ID {}", id);
            return opinionRepository.findById(id).map(opinionMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar la opinión con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar la opinión.", e);
        }
    }

    // --- 3. LISTAR OPINIONES DE UNA CASA ---

    /**
     * Recupera todas las opiniones asociadas a una casa rural específica.
     *
     * @param casaRuralId Identificador de la casa rural.
     * @return Lista de {@link OpinionDTO} de esa casa.
     */
    public List<OpinionDTO> getOpinionesByCasaRural(Long casaRuralId) {
        try {
            logger.info("Buscando opiniones de la casa rural con ID {}", casaRuralId);

            if (!casaRuralRepository.existsById(casaRuralId)) {
                throw new IllegalArgumentException("Casa rural no encontrada con ID: " + casaRuralId);
            }

            List<Opinion> opiniones = opinionRepository.findByCasaRural_Id(casaRuralId);
            logger.info("Se encontraron {} opiniones para la casa {}", opiniones.size(), casaRuralId);

            return opiniones.stream().map(opinionMapper::toDTO).toList();
        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener opiniones de la casa {}: {}", casaRuralId, e.getMessage());
            throw new RuntimeException("Error al obtener las opiniones de la casa.", e);
        }
    }

    // --- 4. CREAR OPINIÓN ---

    /**
     * Registra una nueva opinión en el sistema tras validar sus datos.
     *
     * REGLAS DE NEGOCIO:
     * - La puntuación debe estar entre 1 y 5.
     * - La casa rural debe existir.
     * - El huésped debe existir.
     * - Un huésped no puede dejar más de una opinión para la misma casa.
     *
     * @param opinionDTO Objeto con los datos de la opinión a crear.
     * @return El {@link OpinionDTO} de la opinión recién guardada.
     * @throws IllegalArgumentException Si los datos son inválidos o violan reglas de negocio.
     */
    public OpinionDTO createOpinion(@Valid OpinionDTO opinionDTO) {
        try {
            logger.info("Creando nueva opinión para casa {} por huésped {}",
                    opinionDTO.getCasaRuralId(), opinionDTO.getHuespedId());

            // Validar que casa existe
            CasaRural casa = casaRuralRepository.findById(opinionDTO.getCasaRuralId())
                    .orElseThrow(() -> new IllegalArgumentException("Casa rural no encontrada con ID: " + opinionDTO.getCasaRuralId()));

            // Validar que huésped existe
            Huesped huesped = huespedRepository.findById(opinionDTO.getHuespedId())
                    .orElseThrow(() -> new IllegalArgumentException("Huésped no encontrado con ID: " + opinionDTO.getHuespedId()));

            // Validar que no existe ya una opinión de este huésped para esta casa
            List<Opinion> existentes = opinionRepository.findByCasaRural_Id(opinionDTO.getCasaRuralId());
            boolean yaExiste = existentes.stream()
                    .anyMatch(op -> op.getHuesped().getId().equals(opinionDTO.getHuespedId()));

            if (yaExiste) {
                throw new IllegalArgumentException("Este huésped ya ha dejado una opinión para esta casa rural.");
            }

            Opinion opinion = opinionMapper.toEntity(opinionDTO, casa, huesped);
            Opinion savedOpinion = opinionRepository.save(opinion);

            logger.info("Opinión creada correctamente con ID {}", savedOpinion.getId());
            return opinionMapper.toDTO(savedOpinion);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al crear opinión: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear la opinión: {}", e.getMessage());
            throw new RuntimeException("Error interno al crear la opinión.", e);
        }
    }

    // --- 5. ACTUALIZAR OPINIÓN ---

    /**
     * Actualiza una opinión existente, permitiendo cambiar puntuación, comentario, casa y huésped.
     *
     * @param id Identificador de la opinión a actualizar.
     * @param opinionDTO Objeto con los nuevos datos.
     * @return El {@link OpinionDTO} actualizado.
     * @throws IllegalArgumentException Si la opinión no existe o los datos son inválidos.
     */
    public OpinionDTO updateOpinion(Long id, @Valid OpinionDTO opinionDTO) {
        try {
            logger.info("Actualizando opinión con ID {}", id);

            Opinion existente = opinionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Opinión no encontrada con ID: " + id));

            // Validar que casa existe
            CasaRural casa = casaRuralRepository.findById(opinionDTO.getCasaRuralId())
                    .orElseThrow(() -> new IllegalArgumentException("Casa rural no encontrada con ID: " + opinionDTO.getCasaRuralId()));

            // Validar que huésped existe
            Huesped huesped = huespedRepository.findById(opinionDTO.getHuespedId())
                    .orElseThrow(() -> new IllegalArgumentException("Huésped no encontrado con ID: " + opinionDTO.getHuespedId()));

            // Validar unicidad (si cambian casa o huésped)
            if (!existente.getCasaRural().getId().equals(opinionDTO.getCasaRuralId()) ||
                !existente.getHuesped().getId().equals(opinionDTO.getHuespedId())) {

                List<Opinion> existentes = opinionRepository.findByCasaRural_Id(opinionDTO.getCasaRuralId());
                boolean yaExiste = existentes.stream()
                        .filter(op -> !op.getId().equals(id)) // Excluir esta misma opinión
                        .anyMatch(op -> op.getHuesped().getId().equals(opinionDTO.getHuespedId()));

                if (yaExiste) {
                    throw new IllegalArgumentException("Este huésped ya ha dejado una opinión para esta casa rural.");
                }
            }

            existente.setPuntuacion(opinionDTO.getPuntuacion());
            existente.setComentario(opinionDTO.getComentario());
            existente.setCasaRural(casa);
            existente.setHuesped(huesped);

            Opinion savedOpinion = opinionRepository.save(existente);
            logger.info("Opinión {} actualizada correctamente.", id);
            return opinionMapper.toDTO(savedOpinion);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al actualizar opinión: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar la opinión {}: {}", id, e.getMessage());
            throw new RuntimeException("Error interno al actualizar la opinión.", e);
        }
    }

    // --- 6. BORRAR OPINIÓN ---

    /**
     * Elimina una opinión de la base de datos.
     *
     * @param id Identificador de la opinión a eliminar.
     * @throws IllegalArgumentException Si la opinión no existe.
     */
    public void deleteOpinion(Long id) {
        try {
            if (!opinionRepository.existsById(id)) {
                throw new IllegalArgumentException("Opinión no encontrada con ID: " + id);
            }
            opinionRepository.deleteById(id);
            logger.info("Opinión {} borrada correctamente.", id);
        } catch (IllegalArgumentException e) {
            logger.warn("No se pudo borrar la opinión: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al borrar la opinión {}: {}", id, e.getMessage());
            throw new RuntimeException("Error interno al borrar la opinión.", e);
        }
    }
}
