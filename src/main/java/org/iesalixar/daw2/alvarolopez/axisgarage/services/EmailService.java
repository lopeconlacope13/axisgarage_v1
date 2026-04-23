package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio de correo electrónico de Axis Garage.
 * <p>
 * Se apoya en Spring Mail (JavaMailSender) para comunicarse con el servidor SMTP de Gmail.
 * Está diseñado para enviar notificaciones automáticas cuando se produce una acción
 * relevante en el sistema, como la confirmación de una reserva.
 * <p>
 * IMPORTANTE: todos los métodos capturan sus excepciones internamente.
 * Esto garantiza que un fallo en el servidor de correo no cancele la operación
 * de negocio (p.ej. la creación de una reserva) que lo invocó.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Bean de Spring Mail que gestiona la conexión física con el servidor SMTP.
     * Spring lo autoconfigura usando los valores de application.properties (spring.mail.*).
     */
    @Autowired
    private JavaMailSender mailSender;

    /**
     * Dirección del remitente, leída desde la variable de entorno MAIL_USERNAME.
     * Se inyecta en tiempo de arranque con @Value para no hardcodear credenciales.
     */
    @Value("${spring.mail.username}")
    private String from;

    /**
     * Envía un correo electrónico de confirmación al cliente cuando su reserva
     * ha sido procesada correctamente por el sistema.
     *
     * @param toEmail       Dirección de email del destinatario (el cliente).
     * @param renterName    Nombre completo del cliente.
     * @param vehicleModel  Denominación del vehículo reservado (marca + modelo).
     * @param startDate     Fecha de inicio de la reserva en formato YYYY-MM-DD.
     * @param endDate       Fecha de fin de la reserva en formato YYYY-MM-DD.
     * @param totalPrice    Precio total calculado de la reserva en euros.
     */
    public void sendConfirmationEmail(String toEmail, String renterName,
                                      String vehicleModel, String startDate,
                                      String endDate, double totalPrice) {
        try {
            // Construimos el mensaje de correo con los datos de la reserva
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject("Axis Garage — Reservation Confirmed");
            message.setText(buildEmailBody(renterName, vehicleModel, startDate, endDate, totalPrice));

            // Enviamos el correo a través del servidor SMTP configurado en application.properties
            mailSender.send(message);
            logger.info("Correo de confirmación enviado correctamente a {}", toEmail);

        } catch (Exception e) {
            // Si el envío falla (sin conexión, credenciales incorrectas, etc.)
            // solo registramos el error. La reserva ya fue guardada, no la revertimos.
            logger.error("Error al enviar correo de confirmación a {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Construye el cuerpo del mensaje en texto plano con todos los datos
     * relevantes de la reserva para que el cliente tenga un resumen claro.
     *
     * @param renterName    Nombre del cliente.
     * @param vehicleModel  Vehículo reservado.
     * @param startDate     Fecha de inicio.
     * @param endDate       Fecha de fin.
     * @param totalPrice    Importe total en euros.
     * @return              String con el cuerpo del email formateado.
     */
    private String buildEmailBody(String renterName, String vehicleModel,
                                  String startDate, String endDate, double totalPrice) {
        return String.format(
            "Dear %s,\n\n" +
            "Your reservation at Axis Garage has been confirmed.\n\n" +
            "─────────────────────────────────\n" +
            "  Asset      : %s\n" +
            "  Pick-Up    : %s\n" +
            "  Drop-Off   : %s\n" +
            "  Investment : €%.0f\n" +
            "─────────────────────────────────\n\n" +
            "Our concierge team will contact you 24h before your pick-up date " +
            "to confirm the handover details.\n\n" +
            "Axis Garage — Private Atelier\n" +
            "axisgarage.com",
            renterName, vehicleModel, startDate, endDate, totalPrice
        );
    }
}
