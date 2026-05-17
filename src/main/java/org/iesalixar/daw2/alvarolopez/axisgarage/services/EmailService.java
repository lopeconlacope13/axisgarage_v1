package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio de correo electrónico de Axis Garage.
 * Se apoya en Spring Mail (JavaMailSender) para comunicarse con el servidor SMTP de Gmail.
 * Está diseñado para enviar notificaciones automáticas cuando se produce una acción
 * relevante en el sistema, como la confirmación de una reserva.
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
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart necesario tanto para HTML como para adjuntos inline (CID)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Axis Garage — Reservation Confirmed");
            helper.setText(buildEmailBody(renterName, vehicleModel, startDate, endDate, totalPrice), true);

            // Adjuntamos el logo como recurso inline con Content-ID "logo".
            // Los clientes de correo (Gmail, Outlook) bloquean base64 pero sí permiten CID.
            attachLogoInline(helper);

            mailSender.send(message);
            logger.info("Correo de confirmación enviado correctamente a {}", toEmail);

        } catch (MessagingException | RuntimeException e) {
            logger.error("Error al enviar correo de confirmación a {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Envía el email con el enlace para recuperar la contraseña.
     * El enlace incluye el token UUID y apunta al formulario del frontend.
     * Si el envío falla, solo se registra el error — no afecta al flujo de negocio.
     *
     * @param toEmail   Dirección de correo del usuario que olvidó su contraseña.
     * @param resetLink URL completa con el token incluido como parámetro (?token=...).
     */
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Axis Garage — Password Reset");

            // HTML sencillo para el email de recuperación de contraseña
            String html = "<div style=\"background:#0a0a0a;padding:40px 20px;font-family:Georgia,serif;\">" +
                "<div style=\"max-width:600px;margin:0 auto;background:#111111;border:1px solid rgba(201,161,74,0.2);padding:40px;\">" +
                "<h1 style=\"color:#C9A14A;font-size:1.4rem;letter-spacing:0.2em;margin:0 0 6px;\">AXIS GARAGE</h1>" +
                "<p style=\"color:#9a9a95;font-size:0.7rem;letter-spacing:0.3em;margin:0 0 28px;\">PRIVATE ATELIER</p>" +
                "<hr style=\"border:none;border-top:1px solid rgba(201,161,74,0.3);margin-bottom:28px;\">" +
                "<p style=\"color:#f5f5f0;font-size:0.9rem;line-height:1.7;\">You requested a password reset for your Axis Garage account.</p>" +
                "<p style=\"color:#f5f5f0;font-size:0.9rem;line-height:1.7;\">Click the button below to set a new password. This link expires in <strong style=\"color:#C9A14A;\">1 hour</strong>.</p>" +
                "<div style=\"text-align:center;margin:32px 0;\">" +
                "<a href=\"" + resetLink + "\" style=\"display:inline-block;background:#C9A14A;color:#0a0a0a;padding:12px 32px;font-size:0.75rem;letter-spacing:0.15em;text-decoration:none;border-radius:4px;font-family:Georgia,serif;\">RESET PASSWORD</a>" +
                "</div>" +
                "<p style=\"color:#9a9a95;font-size:0.8rem;\">If you did not request this, simply ignore this email.</p>" +
                "<hr style=\"border:none;border-top:1px solid rgba(201,161,74,0.15);margin-top:28px;margin-bottom:16px;\">" +
                "<p style=\"color:#9a9a95;font-size:0.75rem;margin:0;\">Axis Garage — Discreción garantizada.</p>" +
                "</div></div>";

            helper.setText(html, true);
            mailSender.send(message);
            logger.info("Email de recuperación enviado correctamente a {}", toEmail);
        } catch (MessagingException | RuntimeException e) {
            logger.error("Error al enviar email de recuperación a {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Envía un correo de bienvenida al usuario recién registrado.
     * Incluye el logo de la marca como imagen base64 embebida, que no depende
     * de ningún servidor externo para mostrarse en el cliente de correo.
     *
     * @param toEmail   Dirección de email del nuevo usuario.
     * @param firstName Nombre de pila para el saludo personalizado.
     */
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Axis Garage — Welcome to the Atelier");
            helper.setText(buildWelcomeEmailBody(firstName), true);
            attachLogoInline(helper);
            mailSender.send(message);
            logger.info("Email de bienvenida enviado correctamente a {}", toEmail);
        } catch (MessagingException | RuntimeException e) {
            logger.error("Error al enviar email de bienvenida a {}: {}", toEmail, e.getMessage());
        }
    }


    /**
     * Construye el cuerpo HTML del email de confirmación con estilo Old Money.
     * Fondo oscuro, tipografía serif dorada, tabla de detalles de la reserva.
     *
     * @param renterName    Nombre del cliente.
     * @param vehicleModel  Vehículo reservado (marca + modelo).
     * @param startDate     Fecha de inicio de la reserva.
     * @param endDate       Fecha de fin de la reserva.
     * @param totalPrice    Importe total en euros.
     * @return              String HTML listo para enviar por email.
     */
    private String buildEmailBody(String renterName, String vehicleModel,
                                  String startDate, String endDate, double totalPrice) {
        String formattedPrice = String.format("€%.0f", totalPrice);
        String logoHtml = buildLogoHtml();

        return "<!-- Proyecto académico TFG — ficticio -->" +
            "<div style=\"background:#0a0a0a;padding:40px 20px;font-family:Georgia,serif;\">" +
            "<div style=\"max-width:600px;margin:0 auto;background:#111111;border:1px solid rgba(201,161,74,0.2);padding:40px;\">" +

            // Logo de la marca embebido como base64
            logoHtml +

            // Nombre de la marca y subtítulo
            "<h1 style=\"color:#C9A14A;font-size:1.5rem;letter-spacing:0.2em;margin:0 0 8px;\">AXIS GARAGE</h1>" +
            "<p style=\"color:#9a9a95;font-size:0.7rem;letter-spacing:0.3em;margin:0 0 32px;\">PRIVATE ATELIER</p>" +
            "<hr style=\"border:none;border-top:1px solid rgba(201,161,74,0.3);margin-bottom:32px;\">" +

            // Saludo personalizado con el nombre del cliente
            "<p style=\"color:#f5f5f0;font-size:0.9rem;line-height:1.7;margin-bottom:24px;\">Dear " + renterName + ",</p>" +
            "<p style=\"color:#f5f5f0;font-size:0.9rem;line-height:1.7;margin-bottom:32px;\">Your reservation at Axis Garage has been confirmed. Below you will find a summary of your booking.</p>" +

            // Tabla de detalles de la reserva
            "<table style=\"width:100%;border-collapse:collapse;margin-bottom:32px;\">" +
            "<tr style=\"border-bottom:1px solid rgba(245,245,240,0.05);\">" +
            "<td style=\"padding:12px 0;color:#9a9a95;font-size:0.75rem;letter-spacing:0.1em;\">ASSET</td>" +
            "<td style=\"padding:12px 0;color:#f5f5f0;font-size:0.85rem;text-align:right;\">" + vehicleModel + "</td>" +
            "</tr>" +
            "<tr style=\"border-bottom:1px solid rgba(245,245,240,0.05);\">" +
            "<td style=\"padding:12px 0;color:#9a9a95;font-size:0.75rem;letter-spacing:0.1em;\">PICK-UP</td>" +
            "<td style=\"padding:12px 0;color:#f5f5f0;font-size:0.85rem;text-align:right;\">" + startDate + "</td>" +
            "</tr>" +
            "<tr style=\"border-bottom:1px solid rgba(245,245,240,0.05);\">" +
            "<td style=\"padding:12px 0;color:#9a9a95;font-size:0.75rem;letter-spacing:0.1em;\">DROP-OFF</td>" +
            "<td style=\"padding:12px 0;color:#f5f5f0;font-size:0.85rem;text-align:right;\">" + endDate + "</td>" +
            "</tr>" +
            "<tr>" +
            "<td style=\"padding:12px 0;color:#9a9a95;font-size:0.75rem;letter-spacing:0.1em;\">INVESTMENT</td>" +
            "<td style=\"padding:12px 0;color:#C9A14A;font-size:1.1rem;font-weight:700;text-align:right;\">" + formattedPrice + "</td>" +
            "</tr>" +
            "</table>" +

            // Agradecimiento premium con tipografía serif dorada — detalle Old Money
            "<div style=\"text-align:center;margin:28px 0;\">" +
            "<p style=\"color:#C9A14A;font-family:Georgia,serif;font-size:1.0rem;letter-spacing:0.15em;margin:0 0 8px;\">Thank you</p>" +
            "<p style=\"color:#9a9a95;font-size:0.75rem;letter-spacing:0.1em;margin:0;\">for choosing Axis Garage</p>" +
            "<hr style=\"border:none;border-top:1px solid rgba(201,161,74,0.25);width:80px;margin:16px auto 0;\">" +
            "</div>" +

            // Nota de seguimiento
            "<p style=\"color:#9a9a95;font-size:0.8rem;line-height:1.7;margin-bottom:32px;\">Our concierge team will contact you 24 hours before your pick-up date to confirm the handover details.</p>" +

            // Pie de firma
            "<hr style=\"border:none;border-top:1px solid rgba(201,161,74,0.2);margin-bottom:24px;\">" +
            "<p style=\"color:#9a9a95;font-size:0.75rem;margin:0;\">Axis Garage — Discreción garantizada.</p>" +

            "</div></div>";
    }

    /**
     * Construye el cuerpo HTML del email de bienvenida para nuevos usuarios.
     * Mismo estilo Old Money que el resto de comunicaciones de la marca.
     *
     * @param firstName Nombre de pila del nuevo usuario.
     * @return String HTML listo para enviar.
     */
    private String buildWelcomeEmailBody(String firstName) {
        String logoHtml = buildLogoHtml();

        return "<!-- Proyecto académico TFG — ficticio -->" +
            "<div style=\"background:#0a0a0a;padding:40px 20px;font-family:Georgia,serif;\">" +
            "<div style=\"max-width:600px;margin:0 auto;background:#111111;border:1px solid rgba(201,161,74,0.2);padding:40px;\">" +
            logoHtml +
            "<h1 style=\"color:#C9A14A;font-size:1.5rem;letter-spacing:0.2em;margin:0 0 8px;\">AXIS GARAGE</h1>" +
            "<p style=\"color:#9a9a95;font-size:0.7rem;letter-spacing:0.3em;margin:0 0 32px;\">PRIVATE ATELIER</p>" +
            "<hr style=\"border:none;border-top:1px solid rgba(201,161,74,0.3);margin-bottom:32px;\">" +
            "<p style=\"color:#f5f5f0;font-size:0.9rem;line-height:1.7;margin-bottom:24px;\">Welcome, " + firstName + ".</p>" +
            "<p style=\"color:#f5f5f0;font-size:0.9rem;line-height:1.7;margin-bottom:24px;\">Your Axis Garage account has been created. You now have access to our exclusive collection of hypersports and classic collector vehicles.</p>" +
            "<p style=\"color:#9a9a95;font-size:0.85rem;line-height:1.7;margin-bottom:32px;\">Browse the catalog, select your vehicle, and our concierge team will handle the rest.</p>" +
            "<hr style=\"border:none;border-top:1px solid rgba(201,161,74,0.2);margin-bottom:24px;\">" +
            "<p style=\"color:#9a9a95;font-size:0.75rem;margin:0;\">Axis Garage — Discreción garantizada.</p>" +
            "</div></div>";
    }

    /**
     * Devuelve el bloque HTML con el logo referenciado mediante Content-ID (CID).
     * El CID "logo" debe coincidir exactamente con el nombre usado en attachLogoInline().
     * Gmail, Outlook y Apple Mail soportan CID — a diferencia de base64 inline que bloquean.
     *
     * @return String HTML con el tag img referenciando el adjunto inline.
     */
    private String buildLogoHtml() {
        return "<div style=\"margin-bottom:20px;\">" +
               "<img src=\"cid:logo\" alt=\"Axis Garage\" style=\"width:55px;height:auto;\"/>" +
               "</div>";
    }

    /**
     * Adjunta el logo como recurso inline al mensaje con Content-ID "logo".
     * Si el archivo no existe en el classpath, registra un aviso y continúa sin logo
     * (el email se envía igualmente, solo sin imagen).
     *
     * @param helper El MimeMessageHelper del mensaje que se está construyendo.
     */
    private void attachLogoInline(MimeMessageHelper helper) {
        try {
            ClassPathResource logo = new ClassPathResource("logo/Logo Compacto.png");
            if (logo.exists()) {
                // El primer parámetro ("logo") es el Content-ID — debe coincidir con cid:logo en el HTML
                helper.addInline("logo", logo);
            } else {
                logger.warn("Logo no encontrado en el classpath: logo/Logo Compacto.png");
            }
        } catch (Exception e) {
            logger.warn("No se pudo adjuntar el logo al email: {}", e.getMessage());
        }
    }
}
