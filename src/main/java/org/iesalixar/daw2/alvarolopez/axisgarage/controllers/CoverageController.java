package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.CoverageDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.CoverageService;
import org.iesalixar.daw2.alvarolopez.axisgarage.utils.MessageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/coverages")
@Tag(name = "Coverages", description = "Gestión de coberturas de seguro")
public class CoverageController {

    private static final Logger logger = LoggerFactory.getLogger(CoverageController.class);

    @Autowired
    private CoverageService coverageService;

    @Operation(summary = "Obtener cobertura por ID de reserva")
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<?> getCoverageByReservationId(@PathVariable Long reservationId) {
        Optional<CoverageDTO> dto = coverageService.getCoverageByReservationId(reservationId);
        if (dto.isPresent()) {
            return ResponseEntity.ok(dto.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MessageConstants.COVERAGE_RESERVATION_NOT_FOUND);
    }

    @Operation(summary = "Obtener cobertura por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCoverageById(@PathVariable Long id) {
        Optional<CoverageDTO> dto = coverageService.getCoverageById(id);
        if (dto.isPresent()) {
            return ResponseEntity.ok(dto.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MessageConstants.COVERAGE_NOT_FOUND);
    }

    @Operation(summary = "Actualizar cobertura (upgrade/downgrade)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoverage(@PathVariable Long id, @Valid @RequestBody CoverageDTO dto) {
        try {
            CoverageDTO updated = coverageService.updateCoverage(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
