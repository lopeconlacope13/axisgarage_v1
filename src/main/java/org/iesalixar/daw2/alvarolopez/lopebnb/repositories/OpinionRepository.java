package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Opinion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {
    // listar los comentarios de una casa
    List<Opinion> findByCasaRural_Id(Long casaRuralId);

    // Filtrado dinámico por puntuación mínima y casaRural
    @Query("SELECT o FROM Opinion o WHERE " +
            "(:puntuacionMinima IS NULL OR o.puntuacion >= :puntuacionMinima) AND " +
            "(:casaRuralId IS NULL OR o.casaRural.id = :casaRuralId)")
    Page<Opinion> findByFiltros(
            @Param("puntuacionMinima") Integer puntuacionMinima,
            @Param("casaRuralId") Long casaRuralId,
            Pageable pageable);
}
