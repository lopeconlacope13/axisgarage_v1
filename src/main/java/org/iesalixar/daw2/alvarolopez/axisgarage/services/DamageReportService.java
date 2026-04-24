package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.DamageReportDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.DamageReport;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.DamageReportMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.DamageReportRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona los informes de daños (partes) vinculados a reservas.
 * <p>
 * Un parte de daños se registra cuando el vehículo es devuelto con algún desperfecto.
 * Cada informe queda ligado a la reserva que lo originó, proporcionando trazabilidad
 * completa del historial de averías de cada unidad de la flota.
 * </p>
 */
@Service
public class DamageReportService {

    private static final Logger logger = LoggerFactory.getLogger(DamageReportService.class);

    @Autowired
    private DamageReportRepository damageReportRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private DamageReportMapper damageReportMapper;

    /**
     * Lista todos los informes de daños asociados a una reserva.
     *
     * @param reservationId Identificador de la reserva.
     * @return Lista de DamageReportDTO con los reportes de la reserva.
     */
    public List<DamageReportDTO> getReportsByReservationId(Long reservationId) {
        logger.info("Listando informes de daños para reserva {}", reservationId);
        return damageReportRepository.findByReservationId(reservationId)
                .stream()
                .map(damageReportMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un informe de daños por su identificador.
     *
     * @param id Identificador del reporte.
     * @return Optional con el contenido del reporte.
     */
    public Optional<DamageReportDTO> getReportById(Long id) {
        return damageReportRepository.findById(id).map(damageReportMapper::toDTO);
    }

    /**
     * Crea un nuevo registro de daños reportados.
     *
     * @param dto DTO del daño.
     * @return DTO del reporte guardado.
     * @throws IllegalArgumentException si la reserva asociada no se encuentra en el sistema.
     */
    public DamageReportDTO createReport(DamageReportDTO dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + dto.getReservationId()));

        DamageReport entity = damageReportMapper.toEntity(dto, reservation);
        DamageReport saved = damageReportRepository.save(entity);
        logger.info("Informe de daños creado con ID {} (tipo: {}) para reserva {}", saved.getId(), saved.getType(), reservation.getId());
        return damageReportMapper.toDTO(saved);
    }

    /**
     * Edita un informe de daños ya preexistente.
     *
     * @param id del parte reportado.
     * @param dto Contenido actualizado del parte.
     * @return DTO del parte tras su actualización de estado.
     * @throws IllegalArgumentException si el parte original ya no existe.
     */
    public DamageReportDTO updateReport(Long id, DamageReportDTO dto) {
        DamageReport existing = damageReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Informe de daños no encontrado con ID: " + id));

        existing.setType(dto.getType());
        existing.setDescription(dto.getDescription());
        existing.setReportedDate(dto.getReportedDate());
        existing.setImageUrl(dto.getImageUrl());

        DamageReport saved = damageReportRepository.save(existing);
        logger.info("Informe de daños actualizado con ID {}", saved.getId());
        return damageReportMapper.toDTO(saved);
    }

    /**
     * Borrado lógico o destrucción del informe de daños.
     *
     * @param id Clave primaria interna.
     * @throws IllegalArgumentException ante la falta de correspondencia.
     */
    public void deleteReport(Long id) {
        if (!damageReportRepository.existsById(id)) {
            throw new IllegalArgumentException("Informe de daños no encontrado con ID: " + id);
        }
        damageReportRepository.deleteById(id);
        logger.info("Informe de daños eliminado con ID {}", id);
    }
}
