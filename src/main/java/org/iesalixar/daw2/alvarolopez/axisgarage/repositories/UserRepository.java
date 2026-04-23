package org.iesalixar.daw2.alvarolopez.axisgarage.repositories;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username el nombre de usuario a buscar.
     * @return un Optional que contiene el usuario si se encuentra, o vacío si
    no existe.
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su email.
     *
     * @param email el email del usuario.
     * @return un Optional con el usuario o vacío.
     */
    Optional<User> findByEmail(String email);

    @Query("SELECT u.id FROM User u WHERE u.username= :username")
    Long getIdByUsername(String username);

    @Query("SELECT u.id FROM User u WHERE u.email= :email")
    Long getIdByEmail(String email);
}
