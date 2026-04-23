package org.iesalixar.daw2.alvarolopez.axisgarage.repositories;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

    // validar que no se repita el email
    Optional<Owner> findByEmail(String email);

    // validar que no se repita el teléfono
    Optional<Owner> findByPhone(String phone);

    // Filtrado dinámico por nombre o email
    @Query("SELECT o FROM Owner o WHERE " +
            "(:name IS NULL OR LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
            +
            "(:email IS NULL OR LOWER(o.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Owner> findByFiltros(
            @Param("name") String name,
            @Param("email") String email,
            Pageable pageable);
}
