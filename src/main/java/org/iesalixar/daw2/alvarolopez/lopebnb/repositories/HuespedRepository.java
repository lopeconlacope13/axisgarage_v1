package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Huesped;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HuespedRepository extends JpaRepository<Huesped, Long> {

    // validar DNI duplicado
    Optional<Huesped> findByDni(String dni);

    // validar email duplicado
    Optional<Huesped> findByEmail(String email);

    // validar teléfono duplicado
    Optional<Huesped> findByTelefono(String telefono);

    // Filtrado dinámico por nombre o email
    @Query("SELECT h FROM Huesped h WHERE " +
           "(:nombre IS NULL OR LOWER(h.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(h.apellidos) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:email IS NULL OR LOWER(h.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Huesped> findByFiltros(
            @Param("nombre") String nombre,
            @Param("email") String email,
            Pageable pageable
    );
}

