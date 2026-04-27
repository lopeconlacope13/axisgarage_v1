package org.iesalixar.daw2.alvarolopez.axisgarage.repositories;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Buscamos por dueño
    List<Vehicle> findByOwner_Id(Long ownerId);

    // Buscamos por nombre (ahora referenciado por brand / modelo en app, mantengo
    // findByModel)
    boolean existsByModel(String model);

    boolean existsByModelAndOwnerId(String model, Long ownerId);

    // Existe una casa/vehiculo con este nombre, de este dueño, QUE NO SEA esta
    // mismo?
    boolean existsByModelAndOwnerIdAndIdNot(String model, Long ownerId, Long idVehicle);

    // Filtrado dinámico por marca, modelo, potencia mínima y categoría
    @Query("SELECT v FROM Vehicle v WHERE " +
            "(:brand IS NULL OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
            "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
            "(:horsePower IS NULL OR v.horsePower >= :horsePower) AND " +
            "(:categoryId IS NULL OR v.category.id = :categoryId)")
    Page<Vehicle> findByFiltros(
            @Param("brand") String brand,
            @Param("model") String model,
            @Param("horsePower") Integer horsePower,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

}
