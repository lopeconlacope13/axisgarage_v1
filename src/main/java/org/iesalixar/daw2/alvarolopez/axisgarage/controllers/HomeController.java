package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    // inyecto los repositorios
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

    @GetMapping
    public String home(Model model) {
        logger.info("Cargando Dashboard...");

        // usamos .count para hacer un SELECT COUNT * a la bd
        // si usara .findAll().size(), funcionaría pero no es eficiente
        model.addAttribute("totalVehicles", vehicleRepository.count());
        model.addAttribute("totalReservations", reservationRepository.count());
        model.addAttribute("totalRenters", renterRepository.count());
        model.addAttribute("totalOwners", ownerRepository.count());
        model.addAttribute("totalReviews", reviewRepository.count());

        // he creado un método en el repositorio, que ordena los 5 primeros por id
        // descendiente y los muestra
        model.addAttribute("lastReservations", reservationRepository.findTop5ByOrderByIdDesc());

        return "index";
    }
}