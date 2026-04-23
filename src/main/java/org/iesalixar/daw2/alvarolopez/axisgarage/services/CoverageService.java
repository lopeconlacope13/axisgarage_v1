package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.CoverageDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Coverage;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.CoverageMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.CoverageRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CoverageService {

    private static final Logger logger = LoggerFactory.getLogger(CoverageService.class);

    @Autowired
    private CoverageRepository coverageRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CoverageMapper coverageMapper;

    public Optional<CoverageDTO> getCoverageByReservationId(Long reservationId) {
        return coverageRepository.findByReservationId(reservationId).map(coverageMapper::toDTO);
    }

    public Optional<CoverageDTO> getCoverageById(Long id) {
        return coverageRepository.findById(id).map(coverageMapper::toDTO);
    }

    public CoverageDTO createStandardCoverage(Reservation reservation) {
        Coverage coverage = new Coverage();
        coverage.setType("STANDARD");
        coverage.setTotalPrice(0.00);
        coverage.setReservation(reservation);
        Coverage saved = coverageRepository.save(coverage);
        logger.info("Cobertura STANDARD creada para reserva {}", reservation.getId());
        return coverageMapper.toDTO(saved);
    }

    public CoverageDTO updateCoverage(Long id, CoverageDTO dto) {
        Coverage existing = coverageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cobertura no encontrada con ID: " + id));

        existing.setType(dto.getType());
        existing.setTotalPrice(dto.getTotalPrice());

        Coverage saved = coverageRepository.save(existing);
        logger.info("Cobertura actualizada con ID {} -> tipo: {}, precio: {}", saved.getId(), saved.getType(), saved.getTotalPrice());
        return coverageMapper.toDTO(saved);
    }
}
