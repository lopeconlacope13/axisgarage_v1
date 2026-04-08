package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    //inyecto los repositorios
    @Autowired private CasaRuralRepository casaRuralRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private HuespedRepository huespedRepository;
    @Autowired private PropietarioRepository propietarioRepository;
    @Autowired private OpinionRepository opinionRepository;

    @GetMapping
    public String home(Model model) {
        logger.info("Cargando Dashboard...");

        //usamos .count para hacer un SELECT COUNT * a la bd
        //si usara .findAll().size(), funcionaría pero no es eficiente
        model.addAttribute("totalCasas", casaRuralRepository.count());
        model.addAttribute("totalReservas", reservaRepository.count());
        model.addAttribute("totalHuespedes", huespedRepository.count());
        model.addAttribute("totalPropietarios", propietarioRepository.count());
        model.addAttribute("totalOpiniones", opinionRepository.count());


        //he creado un método en el repositorio, que ordena los 5 primeros por id descendiente y los muestra
        model.addAttribute("ultimasReservas", reservaRepository.findTop5ByOrderByIdDesc());


        return "index";
    }
}