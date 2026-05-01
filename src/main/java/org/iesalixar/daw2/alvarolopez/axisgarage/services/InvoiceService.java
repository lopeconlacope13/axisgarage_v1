package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.InvoiceDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Invoice;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.InvoiceMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.InvoiceRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.ReservationRepository;
import org.springframework.stereotype.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que gestiona la lógica de negocio para la emisión y consulta de facturas.
 * El número de factura y los importes fiscales se calculan automáticamente.
 */
@Service
public class InvoiceService {

    private static final BigDecimal IVA_ESPANA = new BigDecimal("0.21");

    private final InvoiceRepository invoiceRepository;
    private final ReservationRepository reservationRepository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ReservationRepository reservationRepository,
                          InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.reservationRepository = reservationRepository;
        this.invoiceMapper = invoiceMapper;
    }

    /**
     * Devuelve el listado completo de todas las facturas del sistema.
     *
     * @return Lista de InvoiceDTO con todas las facturas.
     */
    public List<InvoiceDTO> obtenerTodasLasFacturas() {
        return invoiceRepository.findAll().stream()
                .map(invoiceMapper::toDTO)
                .toList();
    }

    /**
     * Obtiene una factura por su identificador único.
     *
     * @param id Identificador de la factura.
     * @return InvoiceDTO con los datos de la factura.
     * @throws IllegalArgumentException si no existe ninguna factura con ese ID.
     */
    public InvoiceDTO obtenerFacturaPorId(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con ID: " + id));
        return invoiceMapper.toDTO(invoice);
    }

    /**
     * Obtiene la factura asociada a una reserva concreta.
     *
     * @param reservationId ID de la reserva.
     * @return InvoiceDTO con los datos de la factura.
     * @throws IllegalArgumentException si no existe factura para esa reserva.
     */
    public InvoiceDTO obtenerFacturaPorReserva(Long reservationId) {
        Invoice invoice = invoiceRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe factura para la reserva con ID: " + reservationId));
        return invoiceMapper.toDTO(invoice);
    }

    /**
     * Crea una nueva factura para la reserva indicada en el DTO.
     * Genera automáticamente el número de factura con formato AG-YYYY-NNNN,
     * calcula el IVA al 21% y el importe total.
     * Una reserva solo puede tener UNA factura.
     *
     * @param dto DTO con los datos de entrada (reservationId obligatorio).
     * @return InvoiceDTO con la factura creada y su número asignado.
     * @throws IllegalArgumentException si la reserva no existe o ya tiene factura.
     */
    public InvoiceDTO crearFactura(InvoiceDTO dto) {
        if (dto.getReservationId() == null) {
            throw new IllegalArgumentException("El ID de la reserva es obligatorio para emitir una factura.");
        }
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reserva no encontrada con ID: " + dto.getReservationId()));
        if (invoiceRepository.existsByReservationId(dto.getReservationId())) {
            throw new IllegalArgumentException(
                    "Ya existe una factura para la reserva con ID: " + dto.getReservationId());
        }

        // Si el cliente no proporciona baseAmount, se toma el totalPrice de la reserva
        BigDecimal base = dto.getBaseAmount() != null
                ? dto.getBaseAmount()
                : BigDecimal.valueOf(reservation.getTotalPrice());
        BigDecimal taxRate = dto.getTaxRate() != null ? dto.getTaxRate() : IVA_ESPANA;
        BigDecimal taxAmount = base.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        dto.setInvoiceNumber(generarNumeroFactura());
        dto.setIssueDate(LocalDate.now());
        dto.setBaseAmount(base);
        dto.setTaxRate(taxRate);
        dto.setTaxAmount(taxAmount);
        dto.setTotalAmount(total);

        Invoice invoice = invoiceMapper.toEntity(dto, reservation);
        return invoiceMapper.toDTO(invoiceRepository.save(invoice));
    }

    /**
     * Actualiza los campos editables de una factura: método de pago y notas.
     * Los importes y la reserva asociada no se pueden modificar una vez emitida.
     *
     * @param id  ID de la factura a actualizar.
     * @param dto DTO con los nuevos datos (paymentMethod, notes).
     * @return InvoiceDTO con los datos actualizados.
     * @throws IllegalArgumentException si no existe la factura.
     */
    public InvoiceDTO actualizarFactura(Long id, InvoiceDTO dto) {
        Invoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con ID: " + id));
        existing.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getNotes() != null) {
            existing.setNotes(dto.getNotes());
        }
        return invoiceMapper.toDTO(invoiceRepository.save(existing));
    }

    /**
     * Elimina una factura del sistema de forma permanente.
     *
     * @param id ID de la factura a eliminar.
     * @throws IllegalArgumentException si no existe la factura.
     */
    public void eliminarFactura(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new IllegalArgumentException("Factura no encontrada con ID: " + id);
        }
        invoiceRepository.deleteById(id);
    }

    /**
     * Genera el PDF de una factura buscándola por el ID de la reserva.
     * Si la factura no existe, la crea automáticamente antes de generar el PDF.
     * Devuelve los bytes del PDF para que el controlador los envíe como descarga.
     *
     * @param reservationId ID de la reserva cuya factura se quiere descargar.
     * @return Array de bytes con el contenido del PDF.
     */
    public byte[] generateInvoicePdfByReservation(Long reservationId) {
        // Obtener o crear la factura para esa reserva
        InvoiceDTO invoice;
        if (invoiceRepository.existsByReservationId(reservationId)) {
            invoice = obtenerFacturaPorReserva(reservationId);
        } else {
            InvoiceDTO newDto = new InvoiceDTO();
            newDto.setReservationId(reservationId);
            invoice = crearFactura(newDto);
        }

        // Crear el PDF en memoria con OpenPDF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 60, 60);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // --- Borde decorativo dorado alrededor de la página ---
            // Dibujamos un rectángulo fino en los márgenes exteriores para dar
            // sensación de documento sellado de lujo (estilo carta personalizada).
            PdfContentByte borderCb = writer.getDirectContent();
            borderCb.setColorStroke(new Color(201, 161, 74));
            borderCb.setLineWidth(1.2f);
            float borderMargin = 18;
            borderCb.rectangle(
                document.left() - borderMargin,
                document.bottom() - borderMargin,
                document.right() - document.left() + 2 * borderMargin,
                document.top() - document.bottom() + 2 * borderMargin
            );
            borderCb.stroke();

            // --- Fuentes del documento ---
            // Tipografía serif (TIMES_ROMAN) para la marca: más elegante y acorde al estilo Old Money
            Font brandFont    = new Font(Font.TIMES_ROMAN, 18, Font.BOLD,   new Color(201, 161, 74));
            Font subtitleFont = new Font(Font.TIMES_ROMAN,  8, Font.NORMAL, new Color(120, 120, 120));
            Font labelFont    = new Font(Font.HELVETICA,    8, Font.BOLD,   new Color(120, 120, 120));
            Font valueFont    = new Font(Font.HELVETICA,   10, Font.NORMAL, new Color( 30,  30,  30));
            Font totalFont    = new Font(Font.HELVETICA,   12, Font.BOLD,   new Color(201, 161, 74));

            // --- Cabecera de la factura ---
            try {
                // Intentamos cargar el logo proporcionado (Ruta estática para el entorno del TFG)
                Image logo = Image.getInstance("/Users/alvarolopez/TFG/AXISGARAGE_ANGULAR/axis-garage-app/public/assets/logo-completo.png");
                logo.scaleToFit(180, 180);
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.setSpacingAfter(10);
                document.add(logo);
            } catch (Exception e) {
                // Fallback elegante en caso de que no se encuentre la ruta
                Paragraph brand = new Paragraph("AXIS GARAGE", brandFont);
                brand.setAlignment(Element.ALIGN_CENTER);
                document.add(brand);
                
                Paragraph subtitle = new Paragraph("PRIVATE ATELIER — INVOICE", subtitleFont);
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(20);
                document.add(subtitle);
            }

            // Línea separadora dorada gruesa para marcar la cabecera con elegancia
            document.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(1.0f, 100, new Color(201, 161, 74), Element.ALIGN_CENTER, -2)));

            // Número de factura y fecha en dos columnas
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingBefore(16);
            headerTable.setSpacingAfter(16);
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell cell1 = new PdfPCell(new Phrase("Invoice No.\n" + invoice.getInvoiceNumber(), valueFont));
            cell1.setBorder(Rectangle.NO_BORDER);
            PdfPCell cell2 = new PdfPCell(new Phrase("Date: " + invoice.getIssueDate(), valueFont));
            cell2.setBorder(Rectangle.NO_BORDER);
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            headerTable.addCell(cell1);
            headerTable.addCell(cell2);
            document.add(headerTable);

            // --- Tabla de detalle de la factura ---
            document.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(0.3f, 100, new Color(200, 200, 200), Element.ALIGN_CENTER, -2)));

            PdfPTable detailTable = new PdfPTable(2);
            detailTable.setWidthPercentage(100);
            detailTable.setSpacingBefore(12);
            detailTable.setSpacingAfter(12);

            // Función auxiliar para añadir filas label + valor a la tabla
            java.util.function.BiConsumer<String, String> addRow = (label, value) -> {
                PdfPCell l = new PdfPCell(new Phrase(label, labelFont));
                l.setBorder(Rectangle.BOTTOM);
                l.setBorderColor(new Color(230, 230, 230));
                l.setPadding(8);
                PdfPCell v = new PdfPCell(new Phrase(value, valueFont));
                v.setBorder(Rectangle.BOTTOM);
                v.setBorderColor(new Color(230, 230, 230));
                v.setPadding(8);
                v.setHorizontalAlignment(Element.ALIGN_RIGHT);
                detailTable.addCell(l);
                detailTable.addCell(v);
            };

            addRow.accept("RESERVATION",     "#" + invoice.getReservationId());
            addRow.accept("PAYMENT METHOD",  invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "—");
            addRow.accept("BASE AMOUNT",     String.format("€%.2f", invoice.getBaseAmount()));
            addRow.accept("TAX RATE (IVA)",  String.format("%.0f%%", invoice.getTaxRate().multiply(new BigDecimal(100))));
            addRow.accept("TAX AMOUNT",      String.format("€%.2f", invoice.getTaxAmount()));

            // Fila del total con fuente dorada destacada
            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL", totalFont));
            totalLabel.setBorder(Rectangle.NO_BORDER);
            totalLabel.setPadding(10);
            PdfPCell totalValue = new PdfPCell(new Phrase(String.format("€%.2f", invoice.getTotalAmount()), totalFont));
            totalValue.setBorder(Rectangle.NO_BORDER);
            totalValue.setPadding(10);
            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            detailTable.addCell(totalLabel);
            detailTable.addCell(totalValue);

            document.add(detailTable);

            // --- Pie de página (Posición absoluta al final del documento) ---
            PdfContentByte cb = writer.getDirectContent();
            
            float bottomY = document.bottom() + 20; // Espacio desde abajo

            // Línea dorada separadora
            cb.setColorStroke(new Color(201, 161, 74)); // axis-gold
            cb.setLineWidth(0.5f);
            cb.moveTo(document.left(), bottomY + 25);
            cb.lineTo(document.right(), bottomY + 25);
            cb.stroke();

            // Textos del footer
            Font footerBold = new Font(Font.HELVETICA, 8, Font.BOLD, new Color(201, 161, 74));
            Font footerNormal = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(120, 120, 120));

            Phrase line1 = new Phrase();
            line1.add(new Chunk("AXIS GARAGE", footerBold));
            line1.add(new Chunk(" — Discreción garantizada. The Private Atelier.", footerNormal));

            Phrase line2 = new Phrase("Proyecto Académico TFG — Ficticio | Desarrollado por Álvaro López Pérez", footerNormal);

            float xCenter = (document.left() + document.right()) / 2;

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, line1, xCenter, bottomY + 10, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, line2, xCenter, bottomY, 0);

        } finally {
            // Siempre cerramos el documento para liberar recursos
            document.close();
        }

        return out.toByteArray();
    }

    /**
     * Genera el número correlativo de la factura en formato AG-YYYY-NNNN,
     * basado en el año actual y el número de facturas ya emitidas ese año.
     *
     * @return Número de factura único (p.ej. AG-2026-0001).
     */
    private String generarNumeroFactura() {
        int year = LocalDate.now().getYear();
        long count = invoiceRepository.countByYear(year);
        return String.format("AG-%d-%04d", year, count + 1);
    }
}
