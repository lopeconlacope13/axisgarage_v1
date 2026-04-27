package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.InvoiceDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de facturas de Axis Garage.
 * Expone los endpoints bajo /api/invoices.
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Devuelve el listado completo de facturas del sistema.
     * Acceso restringido a MANAGER y ADMIN.
     *
     * @return ResponseEntity con la lista de InvoiceDTO o error interno.
     */
    @GetMapping
    public ResponseEntity<?> listarFacturas() {
        try {
            List<InvoiceDTO> facturas = invoiceService.obtenerTodasLasFacturas();
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            logger.error("Error al obtener las facturas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener las facturas.");
        }
    }

    /**
     * Obtiene una factura por su ID.
     *
     * @param id Identificador de la factura.
     * @return ResponseEntity con el InvoiceDTO o 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerFacturaPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(invoiceService.obtenerFacturaPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al obtener la factura con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la factura.");
        }
    }

    /**
     * Obtiene la factura asociada a una reserva concreta.
     * El cliente autenticado puede consultar la factura de su propia reserva.
     *
     * @param reservationId ID de la reserva.
     * @return ResponseEntity con el InvoiceDTO o 404 si no existe.
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<?> obtenerFacturaPorReserva(@PathVariable Long reservationId) {
        try {
            return ResponseEntity.ok(invoiceService.obtenerFacturaPorReserva(reservationId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al obtener la factura de la reserva con ID: {}", reservationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la factura.");
        }
    }

    /**
     * Crea una nueva factura para la reserva indicada.
     * Genera el número de factura automáticamente (AG-YYYY-NNNN) y calcula el IVA al 21%.
     *
     * @param dto DTO con los datos de la factura (reservationId obligatorio).
     * @return ResponseEntity 201 con el InvoiceDTO creado o error.
     */
    @PostMapping
    public ResponseEntity<?> crearFactura(@RequestBody InvoiceDTO dto) {
        try {
            InvoiceDTO creada = invoiceService.crearFactura(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear la factura", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la factura.");
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
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarFactura(@PathVariable Long id, @RequestBody InvoiceDTO dto) {
        try {
            InvoiceDTO actualizada = invoiceService.actualizarFactura(id, dto);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al actualizar la factura con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar la factura.");
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al generar el PDF.");
        }
    }

    /**
     * Elimina una factura del sistema de forma permanente.
     * Acceso exclusivo para ADMIN.
     *
     * @param id ID de la factura a eliminar.
     * @return ResponseEntity 204 sin contenido o 404 si no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarFactura(@PathVariable Long id) {
        try {
            invoiceService.eliminarFactura(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar la factura con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la factura.");
        }
    }
}
