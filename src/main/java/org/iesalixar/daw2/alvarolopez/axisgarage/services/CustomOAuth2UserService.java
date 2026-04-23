package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.AuthProvider;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Role;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.User;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.RoleRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio personalizado de OAuth2 que extiende DefaultOAuth2UserService.
 * Se encarga de interceptar la autenticación extraída de proveedores externos
 * como Google o Facebook, capturar el email del cliente y registrar o enlazar
 * automáticamente a los usuarios en la base de datos de Axis Garage bajo el perfil ROLE_USER.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Carga el perfil del usuario validado extraído del proveedor de OAuth2 y lo 
     * procesa para crearle una cuenta o sincronizarlo con una existente.
     *
     * @param userRequest Objeto con la información del cliente OAuth2 y los tokens.
     * @return El usuario OAuth2 cargado.
     * @throws OAuth2AuthenticationException En caso de no ser capaz de procesarlo o no existir email.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider authProvider = registrationId.equalsIgnoreCase("google") ? AuthProvider.GOOGLE : AuthProvider.FACEBOOK;
        
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException("Email no disponible desde el proveedor de OAuth2");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Optional: update provider or name
            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                // If it was local, we optionally could link it, or just leave it.
                user.setAuthProvider(authProvider);
                userRepository.save(user);
            }
        } else {
            // Register new user automatically
            User newUser = new User();
            newUser.setEmail(email);
            
            // Try to set some values, as the db constraints are NOT NULL
            String name = oAuth2User.getAttribute("name");
            if (name == null) {
                name = email.split("@")[0];
            }
            String firstName = oAuth2User.getAttribute("given_name");
            String lastName = oAuth2User.getAttribute("family_name");
            if (firstName == null) firstName = name;
            if (lastName == null) lastName = "OAuth";

            newUser.setUsername(email); // Use email as username mostly or extract
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setPassword(null);
            newUser.setEnabled(true);
            newUser.setAuthProvider(authProvider);

            Optional<Role> roleOpt = roleRepository.findByName("USER");
            Set<Role> roles = new HashSet<>();
            roleOpt.ifPresent(roles::add);
            newUser.setRoles(roles);

            userRepository.save(newUser);
        }

        return oAuth2User;
    }
}
