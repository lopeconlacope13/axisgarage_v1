package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.InvoiceDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Invoice;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Renter;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Vehicle;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.InvoiceMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.InvoiceRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
     * <p>
     * Diseño visual premium coherente con la identidad de Axis Garage:
     * banda oscura en cabecera, borde dorado exterior, tabla de detalles
     * con fila de cabecera oscura y total en dorado.
     * <p>
     * Es transaccional porque necesitamos acceder a las relaciones LAZY
     * (Renter y Vehicle de la reserva) sin que JPA cierre el EntityManager.
     *
     * @param reservationId ID de la reserva cuya factura se quiere descargar.
     * @return Array de bytes con el contenido del PDF.
     */
    @Transactional
    public byte[] generateInvoicePdfByReservation(Long reservationId) {
        // Cargamos la reserva completa en la misma transacción para poder
        // acceder a sus relaciones LAZY (renter y vehicle) sin excepciones.
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reserva no encontrada con ID: " + reservationId));

        // Obtener o crear la factura para esa reserva
        InvoiceDTO invoice;
        if (invoiceRepository.existsByReservationId(reservationId)) {
            invoice = obtenerFacturaPorReserva(reservationId);
        } else {
            InvoiceDTO newDto = new InvoiceDTO();
            newDto.setReservationId(reservationId);
            invoice = crearFactura(newDto);
        }

        // --- Crear el PDF en memoria con OpenPDF ---
        // Los márgenes top=120 dejan espacio a la banda oscura de cabecera
        // que se pinta como gráfico directo (no como contenido de flujo).
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 120, 60);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // --- Constantes de color de la paleta Axis Garage ---
            // Se definen aquí para reutilizarlas en todos los elementos del PDF.
            Color GOLD  = new Color(201, 161,  74); // Dorado característico
            Color DARK  = new Color( 10,  10,  10); // Fondo banda cabecera
            Color DARK2 = new Color( 20,  20,  20); // Fondo fila cabecera tabla
            Color GRAY1 = new Color(150, 150, 150); // Texto secundario
            Color GRAY2 = new Color(200, 200, 200); // Líneas divisorias
            Color BG    = new Color(244, 244, 244); // Fondo tarjetas info

            // Obtenemos dimensiones reales de la página A4
            float pageWidth  = document.getPageSize().getWidth();
            float pageHeight = document.getPageSize().getHeight();

            // --- Capa de fondo (getDirectContentUnder) ---
            // Usamos la capa inferior para que la banda quede por debajo del texto.
            PdfContentByte fgLayer = writer.getDirectContent();
            PdfContentByte bgLayer = writer.getDirectContentUnder();

            // Banda oscura de cabecera: ocupa el 100% del ancho × 90pt de alto
            bgLayer.setColorFill(DARK);
            bgLayer.rectangle(0, pageHeight - 90, pageWidth, 90);
            bgLayer.fill();

            // Línea dorada separadora bajo la banda (marca visual premium)
            bgLayer.setColorStroke(GOLD);
            bgLayer.setLineWidth(1.5f);
            bgLayer.moveTo(0, pageHeight - 90);
            bgLayer.lineTo(pageWidth, pageHeight - 90);
            bgLayer.stroke();

            // --- Borde exterior dorado de página ---
            // Rectángulo sin relleno, a 15pt del borde físico del papel.
            // El alpha (80) lo hace semitransparente para no sobrecargar visualmente.
            bgLayer.setColorStroke(new Color(201, 161, 74, 80));
            bgLayer.setLineWidth(0.8f);
            bgLayer.rectangle(15, 15, pageWidth - 30, pageHeight - 30);
            bgLayer.stroke();

            // --- Textos de la banda de cabecera (capa superior) ---
            // Se posicionan de forma absoluta sobre la banda oscura ya pintada.
            float xCenter = pageWidth / 2f;

            // "AXIS GARAGE" en serif dorado: el nombre de la marca centrado
            Font brandFont = new Font(Font.TIMES_ROMAN, 22, Font.BOLD, GOLD);
            ColumnText.showTextAligned(fgLayer, Element.ALIGN_CENTER,
                    new Phrase("AXIS GARAGE", brandFont),
                    xCenter, pageHeight - 48, 0);

            // "PRIVATE ATELIER" subtítulo en gris claro bajo el nombre
            Font subtitleFont = new Font(Font.HELVETICA, 7, Font.NORMAL, GRAY1);
            ColumnText.showTextAligned(fgLayer, Element.ALIGN_CENTER,
                    new Phrase("PRIVATE ATELIER", subtitleFont),
                    xCenter, pageHeight - 62, 0);

            // "INVOICE" etiqueta en la esquina superior derecha
            Font invoiceLabelFont = new Font(Font.HELVETICA, 8, Font.BOLD, GRAY1);
            ColumnText.showTextAligned(fgLayer, Element.ALIGN_RIGHT,
                    new Phrase("INVOICE", invoiceLabelFont),
                    pageWidth - 50, pageHeight - 47, 0);

            // Número de factura bajo la etiqueta INVOICE, en blanco destacado
            Font invoiceNumFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            ColumnText.showTextAligned(fgLayer, Element.ALIGN_RIGHT,
                    new Phrase(invoice.getInvoiceNumber(), invoiceNumFont),
                    pageWidth - 50, pageHeight - 62, 0);

            // --- Logo de la marca (capa de fondo, posición absoluta) ---
            // Si la imagen no se puede cargar (por ruta rota), el bloque catch
            // no hace nada: los textos de marca pintados arriba actúan como fallback.
            try {
                Image logo = Image.getInstance(
                    "/Users/alvarolopez/TFG/AXISGARAGE_ANGULAR/axis-garage-app/public/assets/logo-completo.png");
                // Escalamos y posicionamos el logo dentro de la banda oscura
                logo.scaleToFit(120, 70);
                logo.setAbsolutePosition(
                    (pageWidth - logo.getScaledWidth()) / 2f,
                    pageHeight - 85
                );
                fgLayer.addImage(logo);
            } catch (Exception ignored) {
                // Fallback: los textos de cabecera ya están pintados arriba — no se necesita nada más.
            }

            // --- Fuentes reutilizables para el cuerpo del documento ---
            Font labelFont = new Font(Font.HELVETICA,  7, Font.BOLD,   GRAY1);
            Font valueFont = new Font(Font.HELVETICA,  9, Font.NORMAL, new Color(30, 30, 30));
            Font totalFont = new Font(Font.TIMES_ROMAN, 12, Font.BOLD,  GOLD);

            // --- Tabla de información (3 columnas: cliente / vehículo / período) ---
            // Cada celda tiene fondo gris claro y sin borde para un look limpio y moderno.
            Renter renter   = reservation.getRenter();
            Vehicle vehicle = reservation.getVehicle();
            long dias = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());

            PdfPTable infoTable = new PdfPTable(3);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1f, 1f, 1f});
            infoTable.setSpacingBefore(14);
            infoTable.setSpacingAfter(14);

            // Columna 1 — Datos del cliente (BILLED TO)
            Phrase billedTo = new Phrase();
            billedTo.add(new Chunk("BILLED TO\n", labelFont));
            billedTo.add(new Chunk(renter.getName() + " " + renter.getLastName() + "\n", valueFont));
            billedTo.add(new Chunk(renter.getEmail() + "\n", valueFont));
            billedTo.add(new Chunk("DNI: " + renter.getDni() + "\n", valueFont));
            billedTo.add(new Chunk("Tel: " + renter.getPhone(), valueFont));
            if (renter.getAddress() != null && !renter.getAddress().isBlank()) {
                billedTo.add(new Chunk("\n" + renter.getAddress(), valueFont));
            }
            PdfPCell cellBilled = new PdfPCell(billedTo);
            cellBilled.setBackgroundColor(BG);
            cellBilled.setBorder(Rectangle.NO_BORDER);
            cellBilled.setPadding(10);
            infoTable.addCell(cellBilled);

            // Columna 2 — Datos del vehículo (ASSET)
            Phrase asset = new Phrase();
            asset.add(new Chunk("ASSET\n", labelFont));
            asset.add(new Chunk(vehicle.getBrand() + " " + vehicle.getModel() + "\n", valueFont));
            asset.add(new Chunk("Year: " + vehicle.getProductionYear(), valueFont));
            PdfPCell cellAsset = new PdfPCell(asset);
            cellAsset.setBackgroundColor(BG);
            cellAsset.setBorder(Rectangle.NO_BORDER);
            cellAsset.setPadding(10);
            infoTable.addCell(cellAsset);

            // Columna 3 — Período de alquiler (RENTAL PERIOD)
            Phrase rental = new Phrase();
            rental.add(new Chunk("RENTAL PERIOD\n", labelFont));
            rental.add(new Chunk(reservation.getStartDate() + "  →  " + reservation.getEndDate() + "\n", valueFont));
            rental.add(new Chunk(dias + " day" + (dias != 1 ? "s" : ""), valueFont));
            PdfPCell cellRental = new PdfPCell(rental);
            cellRental.setBackgroundColor(BG);
            cellRental.setBorder(Rectangle.NO_BORDER);
            cellRental.setPadding(10);
            infoTable.addCell(cellRental);

            document.add(infoTable);

            // --- Línea divisoria gris entre info y detalle ---
            document.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(
                    0.5f, 100, GRAY2, Element.ALIGN_CENTER, -2)));

            // --- Tabla de detalle de la factura ---
            PdfPTable detailTable = new PdfPTable(2);
            detailTable.setWidthPercentage(100);
            detailTable.setSpacingBefore(12);
            detailTable.setSpacingAfter(12);

            // Fila de cabecera con fondo oscuro y texto blanco
            // Da jerarquía visual clara entre el encabezado y los datos.
            PdfPCell headerDesc = new PdfPCell(new Phrase("DESCRIPTION",
                    new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE)));
            headerDesc.setBackgroundColor(DARK2);
            headerDesc.setBorder(Rectangle.NO_BORDER);
            headerDesc.setPadding(9);

            PdfPCell headerAmount = new PdfPCell(new Phrase("AMOUNT",
                    new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE)));
            headerAmount.setBackgroundColor(DARK2);
            headerAmount.setBorder(Rectangle.NO_BORDER);
            headerAmount.setPadding(9);
            headerAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);

            detailTable.addCell(headerDesc);
            detailTable.addCell(headerAmount);

            // Función auxiliar para añadir filas de datos con borde inferior gris
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

            addRow.accept("Reservation",    "#" + invoice.getReservationId());
            addRow.accept("Payment Method", invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "—");
            addRow.accept("Base Amount",    String.format("€%.2f", invoice.getBaseAmount()));
            addRow.accept("Tax Rate (IVA)", String.format("%.0f%%", invoice.getTaxRate().multiply(new BigDecimal(100))));
            addRow.accept("Tax Amount",     String.format("€%.2f", invoice.getTaxAmount()));

            // Fila del total con tipografía serif dorada en 12pt — el importe final protagonista
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

            // --- Pie de página (posición absoluta al final del documento) ---
            // Se usa getDirectContent() y ColumnText para colocar el footer
            // exactamente donde queremos, independientemente del flujo del documento.
            float bottomY = document.bottom() + 20;

            // Línea dorada fina separadora del footer
            fgLayer.setColorStroke(GOLD);
            fgLayer.setLineWidth(0.5f);
            fgLayer.moveTo(document.left(), bottomY + 28);
            fgLayer.lineTo(document.right(), bottomY + 28);
            fgLayer.stroke();

            // Línea 1 del footer centrada: nombre de marca + eslogan
            Font footerBold   = new Font(Font.HELVETICA, 8, Font.BOLD,   GOLD);
            Font footerNormal = new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY1);

            Phrase footerLine1 = new Phrase();
            footerLine1.add(new Chunk("AXIS GARAGE", footerBold));
            footerLine1.add(new Chunk(" — Discreción garantizada. The Private Atelier.", footerNormal));
            ColumnText.showTextAligned(fgLayer, Element.ALIGN_CENTER,
                    footerLine1, xCenter, bottomY + 14, 0);

            // Línea 2 del footer a la derecha: número de factura como referencia
            Phrase footerLine2 = new Phrase(invoice.getInvoiceNumber(), footerNormal);
            ColumnText.showTextAligned(fgLayer, Element.ALIGN_RIGHT,
                    footerLine2, document.right(), bottomY + 2, 0);

        } finally {
            // Siempre cerramos el documento para liberar recursos,
            // incluso si se ha producido algún error durante la generación.
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
