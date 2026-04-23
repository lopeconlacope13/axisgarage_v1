package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.InvoiceDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Invoice;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.InvoiceMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.InvoiceRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.ReservationRepository;
import org.springframework.stereotype.Service;

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
