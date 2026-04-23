package org.iesalixar.daw2.alvarolopez.axisgarage.repositories;

import org.iesalixar.daw2.alvarolopez.axisgarage.entities.DamageReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DamageReportRepository extends JpaRepository<DamageReport, Long> {

    List<DamageReport> findByReservationId(Long reservationId);
}
