package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.ReviewDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Renter;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Review;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.ReviewMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.ReservationRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.RenterRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RenterRepository renterRepository;

    @Autowired
    private ReviewMapper reviewMapper;

    // --- 1. LISTAR CON PAGINACIÓN ---

    public Page<ReviewDTO> getAllReviews(Integer rating, Long reservationId, Pageable pageable) {
        try {
            logger.info("Solicitando opiniones con filtros -> Puntuación Mínima: {}, Reserva ID: {}",
                    rating, reservationId);

            Page<Review> reviews = reviewRepository.findByFiltros(rating, reservationId, pageable);
            logger.info("Se han encontrado {} opiniones.", reviews.getTotalElements());

            return reviews.map(reviewMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista paginada de opiniones: {}", e.getMessage());
            throw new RuntimeException("Error interno al listar las opiniones", e);
        }
    }

    // --- 2. OBTENER UNA POR ID ---

    public Optional<ReviewDTO> getReviewById(Long id) {
        try {
            logger.info("Buscando opinión con ID {}", id);
            return reviewRepository.findById(id).map(reviewMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar la opinión con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar la opinión.", e);
        }
    }

    // --- 3. LISTAR OPINIONES DE UNA RESERVA ---

    public List<ReviewDTO> getReviewsByReservation(Long reservationId) {
        try {
            logger.info("Buscando opiniones de la reserva con ID {}", reservationId);

            if (!reservationRepository.existsById(reservationId)) {
                throw new IllegalArgumentException("Reserva no encontrada con ID: " + reservationId);
            }

            List<Review> reviews = reviewRepository.findByReservation_Id(reservationId);
            logger.info("Se encontraron {} opiniones para la reserva {}", reviews.size(), reservationId);

            return reviews.stream().map(reviewMapper::toDTO).toList();
        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener opiniones de la reserva {}: {}", reservationId, e.getMessage());
            throw new RuntimeException("Error al obtener las opiniones de la reserva.", e);
        }
    }

    // --- 4. CREAR OPINIÓN ---

    public ReviewDTO createReview(@Valid ReviewDTO reviewDTO) {
        try {
            logger.info("Creando nueva opinión para reserva {} por huésped {}",
                    reviewDTO.getReservationId(), reviewDTO.getRenterId());

            // Validar que reserva existe
            Reservation reservation = reservationRepository.findById(reviewDTO.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Reserva no encontrada con ID: " + reviewDTO.getReservationId()));

            // Validar que huésped existe
            Renter renter = renterRepository.findById(reviewDTO.getRenterId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Huésped no encontrado con ID: " + reviewDTO.getRenterId()));

            // Validar que no existe ya una opinión de este huésped para esta reserva
            List<Review> existentes = reviewRepository.findByReservation_Id(reviewDTO.getReservationId());
            boolean yaExiste = existentes.stream()
                    .anyMatch(op -> op.getRenter().getId().equals(reviewDTO.getRenterId()));

            if (yaExiste) {
                throw new IllegalArgumentException("Este huésped ya ha dejado una opinión para esta reserva.");
            }

            Review review = reviewMapper.toEntity(reviewDTO, reservation, renter);
            Review savedReview = reviewRepository.save(review);

            logger.info("Opinión creada correctamente con ID {}", savedReview.getId());
            return reviewMapper.toDTO(savedReview);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al crear opinión: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear la opinión: {}", e.getMessage());
            throw new RuntimeException("Error interno al crear la opinión.", e);
        }
    }

    // --- 5. ACTUALIZAR OPINIÓN ---

    public ReviewDTO updateReview(Long id, @Valid ReviewDTO reviewDTO) {
        try {
            logger.info("Actualizando opinión con ID {}", id);

            Review existente = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Opinión no encontrada con ID: " + id));

            // Validar que reserva existe
            Reservation reservation = reservationRepository.findById(reviewDTO.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Reserva no encontrada con ID: " + reviewDTO.getReservationId()));

            // Validar que huésped existe
            Renter renter = renterRepository.findById(reviewDTO.getRenterId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Huésped no encontrado con ID: " + reviewDTO.getRenterId()));

            // Validar unicidad (si cambian reserva o huésped)
            if (!existente.getReservation().getId().equals(reviewDTO.getReservationId()) ||
                    !existente.getRenter().getId().equals(reviewDTO.getRenterId())) {

                List<Review> existentes = reviewRepository.findByReservation_Id(reviewDTO.getReservationId());
                boolean yaExiste = existentes.stream()
                        .filter(op -> !op.getId().equals(id)) // Excluir esta misma opinión
                        .anyMatch(op -> op.getRenter().getId().equals(reviewDTO.getRenterId()));

                if (yaExiste) {
                    throw new IllegalArgumentException("Este huésped ya ha dejado una opinión para esta reserva.");
                }
            }

            existente.setRating(reviewDTO.getRating());
            existente.setComment(reviewDTO.getComment());
            existente.setReservation(reservation);
            existente.setRenter(renter);

            Review savedReview = reviewRepository.save(existente);
            logger.info("Opinión {} actualizada correctamente.", id);
            return reviewMapper.toDTO(savedReview);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al actualizar opinión: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar la opinión {}: {}", id, e.getMessage());
            throw new RuntimeException("Error interno al actualizar la opinión.", e);
        }
    }

    // --- 6. BORRAR OPINIÓN ---

    public void deleteReview(Long id) {
        try {
            if (!reviewRepository.existsById(id)) {
                throw new IllegalArgumentException("Opinión no encontrada con ID: " + id);
            }
            reviewRepository.deleteById(id);
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
