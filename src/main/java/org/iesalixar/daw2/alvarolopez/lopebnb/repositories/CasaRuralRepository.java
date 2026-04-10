package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CasaRuralRepository extends JpaRepository<CasaRural, Long>{

    //Buscamos por dueño
    List<CasaRural> findByPropietario_Id(Long propietarioId);

    //Buscamos por nombre
    boolean existsByNombre(String nombre);

    boolean existsByNombreAndPropietarioId(String nombre, Long propietarioId);

    // Existe una casa con este nombre, de este dueño, QUE NO SEA esta misma casa?
    boolean existsByNombreAndPropietarioIdAndIdNot(String nombre, Long propietarioId, Long idCasa);

    // Filtrado dinámico por nombre o capacidad
    @Query("SELECT c FROM CasaRural c WHERE " +
            "(:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:capacidad IS NULL OR c.capacidadPersonas >= :capacidad)")
    Page<CasaRural> findByFiltros(
            @Param("nombre") String nombre,
            @Param("capacidad") Long capacidad,
            Pageable pageable);

}

