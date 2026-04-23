package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.DamageReportDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.DamageReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/damage-reports")
@Tag(name = "Damage Reports", description = "Gestión de informes de daños PRE/POST alquiler")
public class DamageReportController {

    private static final Logger logger = LoggerFactory.getLogger(DamageReportController.class);

    @Autowired
    private DamageReportService damageReportService;

    @Operation(summary = "Listar informes de daños por reserva")
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<DamageReportDTO>> getReportsByReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(damageReportService.getReportsByReservationId(reservationId));
    }

    @Operation(summary = "Obtener informe de daños por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable Long id) {
        Optional<DamageReportDTO> dto = damageReportService.getReportById(id);
        if (dto.isPresent()) {
            return ResponseEntity.ok(dto.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Informe de daños no encontrado.");
    }

    @Operation(summary = "Crear un nuevo informe de daños")
    @PostMapping
    public ResponseEntity<?> createReport(@Valid @RequestBody DamageReportDTO dto) {
        try {
            DamageReportDTO created = damageReportService.createReport(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Actualizar un informe de daños")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReport(@PathVariable Long id, @Valid @RequestBody DamageReportDTO dto) {
        try {
            DamageReportDTO updated = damageReportService.updateReport(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Eliminar un informe de daños")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        try {
            damageReportService.deleteReport(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
