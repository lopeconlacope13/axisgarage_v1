package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PropietarioRepository extends JpaRepository<Propietario, Long> {

    // validar que no se repita el email
    Optional<Propietario> findByEmail(String email);

    // validar que no se repita el teléfono
    Optional<Propietario> findByTelefono(String telefono);

    // Filtrado dinámico por nombre o email
    @Query("SELECT p FROM Propietario p WHERE " +
            "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:email IS NULL OR LOWER(p.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Propietario> findByFiltros(
            @Param("nombre") String nombre,
            @Param("email") String email,
            Pageable pageable);
}
