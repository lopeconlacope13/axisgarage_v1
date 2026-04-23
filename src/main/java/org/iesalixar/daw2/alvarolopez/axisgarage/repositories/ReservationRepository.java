package org.iesalixar.daw2.alvarolopez.axisgarage.repositories;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // ver las reservas de una casa
    List<Reservation> findByVehicle_Id(Long vehicleId);

    // ver las reservas de un cliente
    List<Reservation> findByRenter_Id(Long renterId);

    /**
     * Detectar si las fechas chocan con otra reserva (Para CREATE).
     * Devuelve la lista de reservas que chocan para poder mostrar las fechas en el
     * error.
     */
    @Query("SELECT r FROM Reservation r WHERE r.vehicle.id = :vehicleId " +
            "AND :startDate < r.endDate " +
            "AND :endDate > r.startDate")
    List<Reservation> findReservasConflictivas(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Detectar si las fechas chocan con OTRA reserva distinta a la actual (Para
     * UPDATE).
     */
    @Query("SELECT r FROM Reservation r WHERE r.vehicle.id = :vehicleId " +
            "AND r.id != :reservationId " +
            "AND :startDate < r.endDate " +
            "AND :endDate > r.startDate")
    List<Reservation> findReservasConflictivasExcludingId(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("reservationId") Long reservationId);

    // Filtrado dinámico por casaRural, huesped, fechas de entrada y salida
    @Query("SELECT r FROM Reservation r WHERE " +
            "(:vehicleId IS NULL OR r.vehicle.id = :vehicleId) AND " +
            "(:renterId IS NULL OR r.renter.id = :renterId) AND " +
            "(:fechaDesde IS NULL OR r.startDate >= :fechaDesde) AND " +
            "(:fechaHasta IS NULL OR r.startDate <= :fechaHasta)")
    Page<Reservation> findByFiltros(
            @Param("vehicleId") Long vehicleId,
            @Param("renterId") Long renterId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable);

    // buscar las 5 primeras ordenadas por ID descendente, para encontrar
    // las últimas creadas y mostrarlas en el dashboard
    List<Reservation> findTop5ByOrderByIdDesc();
}
