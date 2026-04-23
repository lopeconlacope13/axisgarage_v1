package org.iesalixar.daw2.alvarolopez.axisgarage.repositories;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // listar los comentarios de una reserva/casa
    List<Review> findByReservation_Id(Long reservationId);

    // Filtrado dinámico por puntuación mínima
    @Query("SELECT r FROM Review r WHERE " +
            "(:puntuacionMinima IS NULL OR r.rating >= :puntuacionMinima) AND " +
            "(:reservationId IS NULL OR r.reservation.id = :reservationId)")
    Page<Review> findByFiltros(
            @Param("puntuacionMinima") Integer puntuacionMinima,
            @Param("reservationId") Long reservationId,
            Pageable pageable);
}
