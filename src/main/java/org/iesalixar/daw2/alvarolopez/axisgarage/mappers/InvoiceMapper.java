package org.iesalixar.daw2.alvarolopez.axisgarage.mappers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.InvoiceDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Invoice;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.springframework.stereotype.Component;

/**
 * Mapper encargado de convertir entre la entidad Invoice y su DTO.
 */
@Component
public class InvoiceMapper {

    /**
     * Convierte una entidad Invoice a su DTO de respuesta.
     *
     * @param invoice Entidad origen.
     * @return InvoiceDTO con todos los datos de la factura.
     */
    public InvoiceDTO toDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setIssueDate(invoice.getIssueDate());
        dto.setBaseAmount(invoice.getBaseAmount());
        dto.setTaxRate(invoice.getTaxRate());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setPaymentMethod(invoice.getPaymentMethod());
        dto.setNotes(invoice.getNotes());
        dto.setReservationId(invoice.getReservation() != null ? invoice.getReservation().getId() : null);
        return dto;
    }

    /**
     * Convierte un InvoiceDTO a entidad Invoice, vinculando la reserva proporcionada.
     *
     * @param dto         DTO con los datos de entrada.
     * @param reservation Entidad Reservation ya cargada desde la base de datos.
     * @return Entidad Invoice lista para persistir.
     */
    public Invoice toEntity(InvoiceDTO dto, Reservation reservation) {
        Invoice invoice = new Invoice();
        invoice.setId(dto.getId());
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setBaseAmount(dto.getBaseAmount());
        invoice.setTaxRate(dto.getTaxRate());
        invoice.setTaxAmount(dto.getTaxAmount());
        invoice.setTotalAmount(dto.getTotalAmount());
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setNotes(dto.getNotes());
        invoice.setReservation(reservation);
        return invoice;
    }
}
