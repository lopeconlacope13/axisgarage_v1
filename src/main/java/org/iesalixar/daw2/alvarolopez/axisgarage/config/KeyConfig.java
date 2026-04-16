package org.iesalixar.daw2.alvarolopez.axisgarage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class KeyConfig {

    @Value("${jwt.keystore.location}")
    private Resource keystoreResource;

    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    @Value("${jwt.keystore.alias}")
    private String keystoreAlias;

    /**
     * Crea un bean que carga el par de claves (privada y pública) desde el keystore
     * ubicado en el classpath del proyecto.
     *
     * @return KeyPair con la clave privada y pública cargadas desde el keystore JKS.
     * @throws Exception Si ocurre un error al cargar el keystore o las claves.
     */
    @Bean
    public KeyPair jwtKeyPair() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream is = keystoreResource.getInputStream()) {
            keyStore.load(is, keystorePassword.toCharArray());
        }
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(keystoreAlias, keystorePassword.toCharArray());
        PublicKey publicKey = keyStore.getCertificate(keystoreAlias).getPublicKey();
        return new KeyPair(publicKey, privateKey);
    }
}
