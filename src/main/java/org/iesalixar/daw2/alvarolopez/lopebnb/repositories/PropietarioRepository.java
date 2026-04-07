package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PropietarioRepository extends JpaRepository<Propietario, Long> {

    // validar que no se repita el email
    Optional<Propietario> findByEmail(String email);

    // validar que no se repita el teléfono
    Optional<Propietario> findByTelefono(String telefono);

}
