package org.iesalixar.daw2.alvarolopez.axisgarage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * Clase principal de la aplicación Axis Garage.
 * Punto de entrada de Spring Boot. Activa la serialización segura de páginas
 * paginadas mediante el modo VIA_DTO para evitar la inestabilidad del JSON
 * que genera PageImpl de forma nativa.
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class AxisGarageApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxisGarageApplication.class, args);
    }

}
