package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Reserva;
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


     //detectar si las fechas chocan con otra reserva.
     //Devuelve TRUE si hay solapamiento.
    // Devuelve la lista de reservas que chocan para poder mostrar las fechas en el error
    @Query("SELECT r FROM Reserva r WHERE r.casaRural.id = :casaId " +
            "AND :fechaEntrada < r.fechaSalida " +
            "AND :fechaSalida > r.fechaEntrada")
    List<Reserva> findReservasConflictivas(@Param("casaId") Long casaId,
                                           @Param("fechaEntrada") LocalDate fechaEntrada,
                                           @Param("fechaSalida") LocalDate fechaSalida);

    // buscar las 5 primeras ordenadas por ID descendente, para encontrar
    // las últimas creadas y mostrarlas en el dashboard
    List<Reserva> findTop5ByOrderByIdDesc();
}
