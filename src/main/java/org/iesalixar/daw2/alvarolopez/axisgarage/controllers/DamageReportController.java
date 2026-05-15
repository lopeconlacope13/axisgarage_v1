package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.DamageReportDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.DamageReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de informes de daños vinculados a las reservas de Axis Garage.
 * <p>
 * Permite registrar el estado del vehículo antes y después de cada alquiler (PRE/POST).
 * Cada informe queda asociado a una reserva concreta y puede incluir descripciones
 * y fotografías de los desperfectos detectados. Solo accesible para MANAGER y ADMIN.
 * </p>
 */
@RestController
@RequestMapping("/api/damage-reports")
@Tag(name = "Damage Reports", description = "Gestión de informes de daños PRE/POST alquiler")
public class DamageReportController {

    private static final Logger logger = LoggerFactory.getLogger(DamageReportController.class);

    @Autowired
    private DamageReportService damageReportService;

    // Fuente de mensajes i18n — lee de messages_en.properties o messages_es.properties
    @Autowired
    private MessageSource messageSource;

    /**
     * Devuelve todos los informes de daños registrados en el sistema.
     *
     * @return ResponseEntity con la lista completa de informes.
     */
    @Operation(summary = "Listar todos los informes de daños", description = "Devuelve todos los informes PRE/POST de daños. Restringido a MANAGER y ADMIN.")
    @GetMapping
    public ResponseEntity<List<DamageReportDTO>> getAllReports() {
        return ResponseEntity.ok(damageReportService.getAllReports());
    }

    /**
     * Devuelve todos los informes de daños asociados a una reserva concreta.
     *
     * @param reservationId ID de la reserva cuyos informes se quieren consultar.
     * @return ResponseEntity con la lista de informes de esa reserva.
     */
    @Operation(summary = "Listar informes de daños por reserva", description = "Filtra los informes de daños por el ID de la reserva. Útil para ver el estado PRE y POST de un alquiler.")
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<DamageReportDTO>> getReportsByReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(damageReportService.getReportsByReservationId(reservationId));
    }

    /**
     * Obtiene un informe de daños concreto por su identificador único.
     *
     * @param id ID del informe de daños a recuperar.
     * @return ResponseEntity con el DamageReportDTO o 404 si no se encuentra.
     */
    @Operation(summary = "Obtener informe de daños por ID", description = "Devuelve el informe de daños identificado por su ID. Devuelve 404 si no existe.")
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable Long id) {
        Optional<DamageReportDTO> dto = damageReportService.getReportById(id);
        if (dto.isPresent()) {
            return ResponseEntity.ok(dto.get());
        }
        String msg = messageSource.getMessage("msg.damageReport-controller.notFound", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
    }

    /**
     * Registra un nuevo informe de daños para la reserva indicada en el DTO.
     *
     * @param dto DTO con los datos del informe (reservationId, tipo PRE/POST, descripción).
     * @return ResponseEntity 201 con el informe creado o 400 si los datos son inválidos.
     */
    @Operation(summary = "Crear un nuevo informe de daños", description = "Registra el estado del vehículo (PRE o POST alquiler) asociado a una reserva. Requiere reservationId válido.")
    @PostMapping
    public ResponseEntity<?> createReport(@Valid @RequestBody DamageReportDTO dto) {
        try {
            DamageReportDTO created = damageReportService.createReport(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Modifica los datos de un informe de daños existente.
     *
     * @param id  ID del informe a actualizar.
     * @param dto DTO con los nuevos datos del informe.
     * @return ResponseEntity con el informe actualizado o 400 si los datos son inválidos.
     */
    @Operation(summary = "Actualizar un informe de daños", description = "Modifica la descripción, tipo o fotos de un informe de daños existente.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReport(@PathVariable Long id, @Valid @RequestBody DamageReportDTO dto) {
        try {
            DamageReportDTO updated = damageReportService.updateReport(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Elimina permanentemente un informe de daños del sistema.
     *
     * @param id ID del informe a eliminar.
     * @return ResponseEntity 204 sin contenido o 404 si no existe.
     */
    @Operation(summary = "Eliminar un informe de daños", description = "Borra definitivamente el informe. Devuelve 404 si el ID no existe.")
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
