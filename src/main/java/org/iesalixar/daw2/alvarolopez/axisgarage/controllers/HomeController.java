package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Dashboard", description = "Estadísticas globales de la plataforma Axis Garage")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private RenterRepository renterRepository;
    @Autowired
    private OwnerRepository ownerRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Devuelve las estadísticas globales del dashboard de Axis Garage.
     *
     * @return ResponseEntity con un mapa de contadores: vehículos, reservas, clientes, propietarios y reseñas.
     */
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas globales", description = "Devuelve los totales de vehículos, reservas, clientes, propietarios y reseñas.")
    public ResponseEntity<Map<String, Long>> stats() {
        logger.info("Cargando estadísticas del dashboard...");
        return ResponseEntity.ok(Map.of(
                "totalVehicles", vehicleRepository.count(),
                "totalReservations", reservationRepository.count(),
                "totalRenters", renterRepository.count(),
                "totalOwners", ownerRepository.count(),
                "totalReviews", reviewRepository.count()
        ));
    }
}