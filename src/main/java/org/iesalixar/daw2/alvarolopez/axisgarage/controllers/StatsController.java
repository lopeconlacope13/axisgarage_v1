package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.RenterRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.ReservationRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.VehicleRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador que expone estadísticas globales de la plataforma.
 *
 * Solo accesible para usuarios con rol MANAGER o ADMIN.
 * Devuelve un mapa JSON con tres métricas clave del negocio:
 *   - totalReservations: número total de reservas registradas
 *   - availableVehicles: número de vehículos marcados como disponibles
 *   - totalClients:      número total de arrendatarios (renters) registrados
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    // Repositorios inyectados directamente: no necesitamos servicio intermedio
    // porque son simples conteos sin lógica de negocio adicional.
    private final ReservationRepository reservationRepo;
    private final VehicleRepository     vehicleRepo;
    private final RenterRepository      renterRepo;

    public StatsController(ReservationRepository reservationRepo,
                           VehicleRepository vehicleRepo,
                           RenterRepository renterRepo) {
        this.reservationRepo = reservationRepo;
        this.vehicleRepo     = vehicleRepo;
        this.renterRepo      = renterRepo;
    }

    /**
     * Devuelve las estadísticas globales de la plataforma en un único objeto JSON.
     *
     * Ejemplo de respuesta:
     * {
     *   "totalReservations": 47,
     *   "availableVehicles": 24,
     *   "totalClients": 18
     * }
     *
     * @return Mapa con las tres métricas clave del negocio.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public Map<String, Long> getStats() {
        // Contamos directamente usando los métodos del repositorio JPA
        long totalReservations = reservationRepo.count();
        long availableVehicles = vehicleRepo.countByAvailableTrue();
        long totalClients      = renterRepo.count();

        // Devolvemos los tres valores en un mapa — Spring los serializa a JSON automáticamente
        return Map.of(
            "totalReservations", totalReservations,
            "availableVehicles", availableVehicles,
            "totalClients",      totalClients
        );
    }
}
