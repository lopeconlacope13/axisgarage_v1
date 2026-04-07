package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CasaRuralRepository extends JpaRepository<CasaRural, Long>{

    //Buscamos por dueño
    List<CasaRural> findByPropietario_Id(Long propietarioId);

    //Buscamos por nombre
    boolean existsByNombre(String nombre);


}
