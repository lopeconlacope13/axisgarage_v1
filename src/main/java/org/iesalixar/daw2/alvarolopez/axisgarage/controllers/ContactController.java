package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import org.iesalixar.daw2.alvarolopez.axisgarage.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para el formulario de contacto de la web pública.
 * Recibe el mensaje del usuario y lo reenvía por email al equipo interno.
 * No requiere autenticación — es de acceso público.
 */
@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final EmailService emailService;

    public ContactController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Recibe el mensaje del formulario de contacto y lo reenvía por email.
     * Espera un JSON con tres campos: name, email, message.
     *
     * @param body Map con los campos del formulario (name, email, message).
     * @return 200 OK si el mensaje se procesó, 400 si faltan campos.
     */
    @PostMapping
    public ResponseEntity<?> sendContactMessage(@RequestBody Map<String, String> body) {
        String name    = body.get("name");
        String email   = body.get("email");
        String message = body.get("message");

        // Validación básica: todos los campos deben estar presentes
        if (name == null || name.isBlank() || email == null || email.isBlank() || message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body("Todos los campos (name, email, message) son obligatorios.");
        }

        // Reenviar el mensaje por email (errores de envío no abortan la respuesta)
        emailService.sendContactEmail(name, email, message);

        return ResponseEntity.ok(Map.of("success", true, "message", "Message received. We will contact you within 24 hours."));
    }
}
