package org.iesalixar.daw2.alvarolopez.axisgarage.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa la factura fiscal generada para una reserva confirmada.
 * Incluye el desglose de base imponible, IVA y total, junto al método de pago.
 */
@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    /**
     * Identificador único de la factura (Clave Primaria).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número de factura único con formato AG-YYYY-NNNN.
     * Se genera automáticamente al crear la factura.
     */
    @NotEmpty(message = "{msg.invoice.invoiceNumber.notEmpty}")
    @Column(name = "invoice_number", nullable = false, unique = true, length = 20)
    private String invoiceNumber;

    /**
     * Fecha de emisión del documento fiscal.
     */
    @NotNull(message = "{msg.invoice.issueDate.notNull}")
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /**
     * Importe base de la operación sin impuestos.
     * Corresponde al total_price de la reserva.
     */
    @NotNull(message = "{msg.invoice.baseAmount.notNull}")
    @Positive(message = "{msg.invoice.baseAmount.positive}")
    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount;

    /**
     * Tipo de IVA aplicado (0.2100 para el 21% estándar en España).
     */
    @NotNull(message = "{msg.invoice.taxRate.notNull}")
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal taxRate;

    /**
     * Importe de los impuestos calculado como baseAmount × taxRate.
     */
    @NotNull(message = "{msg.invoice.taxAmount.notNull}")
    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    /**
     * Importe total de la factura (baseAmount + taxAmount).
     */
    @NotNull(message = "{msg.invoice.totalAmount.notNull}")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Método de pago utilizado: CARD, TRANSFER o CASH.
     */
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    /**
     * Notas o comentarios adicionales sobre la factura.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Reserva asociada a esta factura. Relación 1:1.
     * La clave foránea reservation_id reside en esta tabla.
     */
    @OneToOne
    @JoinColumn(name = "reservation_id", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Reservation reservation;
}
