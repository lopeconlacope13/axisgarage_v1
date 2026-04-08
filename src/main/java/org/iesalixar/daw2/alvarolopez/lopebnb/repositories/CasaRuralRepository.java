package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CasaRuralRepository extends JpaRepository<CasaRural, Long>{

    //Buscamos por dueño
    List<CasaRural> findByPropietario_Id(Long propietarioId);

    //Buscamos por nombre
    boolean existsByNombre(String nombre);

    boolean existsByNombreAndPropietarioId(String nombre, Long propietarioId);

    // Magia JPA: ¿Existe una casa con este nombre, de este dueño, QUE NO SEA esta misma casa?
    boolean existsByNombreAndPropietarioIdAndIdNot(String nombre, Long propietarioId, Long idCasa);

}
