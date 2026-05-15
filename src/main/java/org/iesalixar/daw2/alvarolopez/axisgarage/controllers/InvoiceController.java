package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.InvoiceDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que gestiona el ciclo de vida de las facturas generadas en Axis Garage.
 * <p>
 * Permite crear, consultar, actualizar y eliminar facturas, así como generar el PDF
 * descargable de cada una. Las facturas se generan automáticamente al confirmar una reserva
 * y se numeran con el formato AG-YYYY-NNNN para facilitar su identificación contable.
 * </p>
 */
@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Facturas", description = "Gestión de facturas y descarga de PDF para las reservas de Axis Garage")
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    private final InvoiceService invoiceService;

    // Fuente de mensajes i18n — lee de messages_en.properties o messages_es.properties
    private final MessageSource messageSource;

    public InvoiceController(InvoiceService invoiceService, MessageSource messageSource) {
        this.invoiceService = invoiceService;
        this.messageSource = messageSource;
    }

    /**
     * Devuelve el listado completo de facturas del sistema.
     * Acceso restringido a MANAGER y ADMIN.
     *
     * @return ResponseEntity con la lista de InvoiceDTO o error interno.
     */
    @Operation(summary = "Listar todas las facturas", description = "Devuelve todas las facturas registradas. Restringido a MANAGER y ADMIN.")
    @GetMapping
    public ResponseEntity<?> listarFacturas() {
        try {
            List<InvoiceDTO> facturas = invoiceService.obtenerTodasLasFacturas();
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            logger.error("Error al obtener las facturas", e);
            String msg = messageSource.getMessage("msg.invoice-controller.list.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

    /**
     * Obtiene una factura por su ID.
     *
     * @param id Identificador de la factura.
     * @return ResponseEntity con el InvoiceDTO o 404 si no existe.
     */
    @Operation(summary = "Obtener factura por ID", description = "Devuelve los detalles de una factura específica. Devuelve 404 si no existe.")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerFacturaPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(invoiceService.obtenerFacturaPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al obtener la factura con ID: {}", id, e);
            String msg = messageSource.getMessage("msg.invoice-controller.fetch.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

    /**
     * Obtiene la factura asociada a una reserva concreta.
     * El cliente autenticado puede consultar la factura de su propia reserva.
     *
     * @param reservationId ID de la reserva.
     * @return ResponseEntity con el InvoiceDTO o 404 si no existe.
     */
    @Operation(summary = "Obtener factura por ID de reserva", description = "Devuelve la factura vinculada a una reserva concreta. El cliente solo puede ver la suya.")
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<?> obtenerFacturaPorReserva(@PathVariable Long reservationId) {
        try {
            return ResponseEntity.ok(invoiceService.obtenerFacturaPorReserva(reservationId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al obtener la factura de la reserva con ID: {}", reservationId, e);
            String msg = messageSource.getMessage("msg.invoice-controller.fetch.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

    /**
     * Crea una nueva factura para la reserva indicada.
     * Genera el número de factura automáticamente (AG-YYYY-NNNN) y calcula el IVA al 21%.
     *
     * @param dto DTO con los datos de la factura (reservationId obligatorio).
     * @return ResponseEntity 201 con el InvoiceDTO creado o error.
     */
    @Operation(summary = "Crear nueva factura", description = "Genera una factura para la reserva indicada. El número se calcula automáticamente en formato AG-YYYY-NNNN con IVA al 21%.")
    @PostMapping
    public ResponseEntity<?> crearFactura(@RequestBody InvoiceDTO dto) {
        try {
            InvoiceDTO creada = invoiceService.crearFactura(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear la factura", e);
            String msg = messageSource.getMessage("msg.invoice-controller.insert.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

    /**
     * Actualiza el método de pago y las notas de una factura existente.
     * Los importes y la reserva asociada no son modificables.
     *
     * @param id  ID de la factura a actualizar.
     * @param dto DTO con los nuevos datos (paymentMethod, notes).
     * @return ResponseEntity con el InvoiceDTO actualizado o error.
     */
    @Operation(summary = "Actualizar factura", description = "Permite modificar el método de pago y las notas. Los importes y la reserva asociada son inmutables.")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarFactura(@PathVariable Long id, @RequestBody InvoiceDTO dto) {
        try {
            InvoiceDTO actualizada = invoiceService.actualizarFactura(id, dto);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al actualizar la factura con ID: {}", id, e);
            String msg = messageSource.getMessage("msg.invoice-controller.update.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

    /**
     * Genera y descarga el PDF de la factura asociada a una reserva.
     * Si la factura no existe todavía, se crea automáticamente antes de generar el PDF.
     * El navegador/cliente recibirá el archivo como descarga directa.
     *
     * @param reservationId ID de la reserva para la que se quiere el PDF.
     * @return ResponseEntity con los bytes del PDF y las cabeceras de descarga.
     */
    @Operation(summary = "Descargar PDF de factura por reserva",
               description = "Genera el PDF de la factura. Si aún no existe, la crea en el momento. El cliente recibe el archivo como descarga directa (Content-Disposition: attachment).")
    @GetMapping("/reservation/{reservationId}/pdf")
    public ResponseEntity<?> descargarPdfPorReserva(@PathVariable Long reservationId) {
        try {
            byte[] pdfBytes = invoiceService.generateInvoicePdfByReservation(reservationId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // inline = el navegador lo muestra; attachment = lo descarga directamente
            headers.setContentDispositionFormData("attachment", "invoice-reservation-" + reservationId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al generar el PDF de la reserva {}: {}", reservationId, e.getMessage());
            String msg = messageSource.getMessage("msg.invoice-controller.pdf.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }

    /**
     * Elimina una factura del sistema de forma permanente.
     * Acceso exclusivo para ADMIN.
     *
     * @param id ID de la factura a eliminar.
     * @return ResponseEntity 204 sin contenido o 404 si no existe.
     */
    @Operation(summary = "Eliminar factura", description = "Borra permanentemente una factura del sistema. Solo accesible para el rol ADMIN.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarFactura(@PathVariable Long id) {
        try {
            invoiceService.eliminarFactura(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar la factura con ID: {}", id, e);
            String msg = messageSource.getMessage("msg.invoice-controller.delete.error", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }
}
