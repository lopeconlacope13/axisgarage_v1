package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long>{

    // ver las reservas de una casa
    List<Reserva> findByCasaRural_Id(Long casaRuralId);

    // ver las reservas de un cliente
    List<Reserva> findByHuesped_Id(Long huespedId);


    /**
     * Detectar si las fechas chocan con otra reserva (Para CREATE).
     * Devuelve la lista de reservas que chocan para poder mostrar las fechas en el error.
     */
    @Query("SELECT r FROM Reserva r WHERE r.casaRural.id = :casaId " +
            "AND :fechaEntrada < r.fechaSalida " +
            "AND :fechaSalida > r.fechaEntrada")
    List<Reserva> findReservasConflictivas(
            @Param("casaId") Long casaId,
            @Param("fechaEntrada") LocalDate fechaEntrada,
            @Param("fechaSalida") LocalDate fechaSalida);

    /**
     * Detectar si las fechas chocan con OTRA reserva distinta a la actual (Para UPDATE).
     */
    @Query("SELECT r FROM Reserva r WHERE r.casaRural.id = :casaId " +
            "AND r.id != :reservaId " +
            "AND :fechaEntrada < r.fechaSalida " +
            "AND :fechaSalida > r.fechaEntrada")
    List<Reserva> findReservasConflictivasExcludingId(
            @Param("casaId") Long casaId,
            @Param("fechaEntrada") LocalDate fechaEntrada,
            @Param("fechaSalida") LocalDate fechaSalida,
            @Param("reservaId") Long reservaId);

    // Filtrado dinámico por casaRural, huesped, fechas de entrada y salida
    @Query("SELECT r FROM Reserva r WHERE " +
            "(:casaRuralId IS NULL OR r.casaRural.id = :casaRuralId) AND " +
            "(:huespedId IS NULL OR r.huesped.id = :huespedId) AND " +
            "(:fechaDesde IS NULL OR r.fechaEntrada >= :fechaDesde) AND " +
            "(:fechaHasta IS NULL OR r.fechaEntrada <= :fechaHasta)")
    Page<Reserva> findByFiltros(
            @Param("casaRuralId") Long casaRuralId,
            @Param("huespedId") Long huespedId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable);

    // buscar las 5 primeras ordenadas por ID descendente, para encontrar
    // las últimas creadas y mostrarlas en el dashboard
    List<Reserva> findTop5ByOrderByIdDesc();
}


