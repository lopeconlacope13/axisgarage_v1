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
import java.security.cert.Certificate;

@Configuration
public class JwtConfig {

    @Value("${jwt.keystore.location}")
    private Resource keystoreLocation;

    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    @Value("${jwt.keystore.alias}")
    private String keyAlias;

    /**
     * Este Bean lee el archivo físico jwt-keystore.jks y extrae
     * la clave pública y privada (KeyPair) para dárselas a JwtUtil.
     */
    @Bean
    public KeyPair jwtKeyPair() {
        try (InputStream is = keystoreLocation.getInputStream()) {
            // Instanciamos el KeyStore tipo JKS
            KeyStore keyStore = KeyStore.getInstance("JKS");
            // Cargamos el archivo pasándole la contraseña ("changeit")
            keyStore.load(is, keystorePassword.toCharArray());

            // Extraemos la clave privada
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keystorePassword.toCharArray());
            // Extraemos la clave pública del certificado
            Certificate cert = keyStore.getCertificate(keyAlias);
            PublicKey publicKey = cert.getPublicKey();

            return new KeyPair(publicKey, privateKey);

        } catch (Exception e) {
            throw new RuntimeException("Error crítico: No se pudo cargar el archivo jwt-keystore.jks", e);
        }
    }
}