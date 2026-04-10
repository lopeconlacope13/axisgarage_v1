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

@Service
public class DamageReportService {

    private static final Logger logger = LoggerFactory.getLogger(DamageReportService.class);

    @Autowired
    private DamageReportRepository damageReportRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private DamageReportMapper damageReportMapper;

    public List<DamageReportDTO> getReportsByReservationId(Long reservationId) {
        logger.info("Listando informes de daños para reserva {}", reservationId);
        return damageReportRepository.findByReservationId(reservationId)
                .stream()
                .map(damageReportMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<DamageReportDTO> getReportById(Long id) {
        return damageReportRepository.findById(id).map(damageReportMapper::toDTO);
    }

    public DamageReportDTO createReport(DamageReportDTO dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + dto.getReservationId()));

        DamageReport entity = damageReportMapper.toEntity(dto, reservation);
        DamageReport saved = damageReportRepository.save(entity);
        logger.info("Informe de daños creado con ID {} (tipo: {}) para reserva {}", saved.getId(), saved.getType(), reservation.getId());
        return damageReportMapper.toDTO(saved);
    }

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

    public void deleteReport(Long id) {
        if (!damageReportRepository.existsById(id)) {
            throw new IllegalArgumentException("Informe de daños no encontrado con ID: " + id);
        }
        damageReportRepository.deleteById(id);
        logger.info("Informe de daños eliminado con ID {}", id);
    }
}
