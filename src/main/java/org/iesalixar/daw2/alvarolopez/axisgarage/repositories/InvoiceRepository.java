package org.iesalixar.daw2.alvarolopez.axisgarage.repositories;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Invoice.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Obtiene la factura asociada a una reserva concreta.
     *
     * @param reservationId ID de la reserva.
     * @return Optional con la factura si existe.
     */
    Optional<Invoice> findByReservationId(Long reservationId);

    /**
     * Comprueba si ya existe una factura para la reserva indicada.
     * Se usa como guardia antes de crear una nueva factura.
     *
     * @param reservationId ID de la reserva.
     * @return true si ya existe factura para esa reserva.
     */
    boolean existsByReservationId(Long reservationId);

    /**
     * Busca una factura por su número único de formato AG-YYYY-NNNN.
     *
     * @param invoiceNumber Número de factura.
     * @return Optional con la factura si existe.
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Cuenta las facturas emitidas en un año concreto.
     * Se usa para generar el número correlativo del ejercicio.
     *
     * @param year Año de consulta (p.ej. 2026).
     * @return Número de facturas emitidas ese año.
     */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE YEAR(i.issueDate) = :year")
    long countByYear(@Param("year") int year);
}
