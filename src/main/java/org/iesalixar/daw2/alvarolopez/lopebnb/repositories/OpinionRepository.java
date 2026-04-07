package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Opinion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {
    // listar los comentarios de una casa
    List<Opinion> findByCasaRural_Id(Long casaRuralId);
}
