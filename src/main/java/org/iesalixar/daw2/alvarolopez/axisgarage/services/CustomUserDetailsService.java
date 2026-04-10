package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private org.iesalixar.daw2.alvarolopez.axisgarage.repositories.UserRepository userRepository;

    /**
     * Carga los detalles del usuario a partir de su nombre de usuario
     *
     * @param username El nombre de usuario a buscar
     * @return Un objeto UserDetails con la informacion de autenticación del usuario.
     * @throws UsernameNotFoundException Si el usuario no se encuentra en la base de datos
     */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        //Convierte los roles de usuario en GrantedAuthority
        //Para llamar al User original de spring y no confundirlo con el nuestro se suele poner el paquete completo
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> {
                            String roleName = role.getName();
                            // Asegurar que el rol siempre comienza con "ROLE_" para compatibilidad con Spring Security
                            return roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                        })
                        .toList()
                        .toArray(new String[0]))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}


