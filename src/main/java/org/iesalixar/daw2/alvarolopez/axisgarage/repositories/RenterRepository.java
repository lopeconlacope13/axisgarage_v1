package org.iesalixar.daw2.alvarolopez.axisgarage.repositories;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Renter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RenterRepository extends JpaRepository<Renter, Long> {

    // validar DNI duplicado
    Optional<Renter> findByDni(String dni);

    // validar email duplicado
    Optional<Renter> findByEmail(String email);

    // validar teléfono duplicado
    Optional<Renter> findByPhone(String phone);

    // Filtrado dinámico por nombre o email
    @Query("SELECT r FROM Renter r WHERE " +
            "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(r.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
            +
            "(:email IS NULL OR LOWER(r.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Renter> findByFiltros(
            @Param("name") String name,
            @Param("email") String email,
            Pageable pageable);
}
