package org.iesalixar.daw2.alvarolopez.axisgarage.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para transportar datos de factura entre capas.
 * Sin anotaciones de validación — la validación ocurre en la entidad Invoice.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    private Long id;
    private String invoiceNumber;
    private LocalDate issueDate;
    private BigDecimal baseAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String notes;
    private Long reservationId;
}
