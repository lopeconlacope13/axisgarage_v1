package org.iesalixar.daw2.alvarolopez.lopebnb.repositories;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Busca un rol por su nombre.
     *
     * @param name el nombre del rol a buscar.
     * @return un Optional que contiene el rol si se encuentra, o vacío si no
    existe.
     */
    Optional<Role> findByName(String name);
}
