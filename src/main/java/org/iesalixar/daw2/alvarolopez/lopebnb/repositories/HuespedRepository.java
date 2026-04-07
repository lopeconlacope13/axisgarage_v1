package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HuespedRepository extends JpaRepository<Huesped, Long> {

    // validar DNI duplicado
    Optional<Huesped> findByDni(String dni);

    // validar email duplicado
    Optional<Huesped> findByEmail(String email);

    // validar teléfono duplicado
    Optional<Huesped> findByTelefono(String telefono);
}
