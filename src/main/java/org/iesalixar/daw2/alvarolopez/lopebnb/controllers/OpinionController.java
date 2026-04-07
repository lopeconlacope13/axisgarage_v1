package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Opinion;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.CasaRuralRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.HuespedRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.OpinionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/opiniones")
public class OpinionController {

    private static final Logger logger = LoggerFactory.getLogger(OpinionController.class);

    @Autowired
    private OpinionRepository opinionRepository;

    //tememos que inyectar 2 repositorios extra ya que una opinion pertenece a una casa y la escribe un huesped
    // por lo que necesitamos cargar las listas de ambos para que el usuario pueda elegirlos
    // en los desplegables del formulario.
    @Autowired
    private CasaRuralRepository casaRuralRepository;

    @Autowired
    private HuespedRepository huespedRepository;

    // --- 1. LISTAR ---
    @GetMapping
    public String listOpiniones(Model model) {
        logger.info("Solicitando lista de opiniones...");

        // traemos todas las opiniones de la base de datos
        List<Opinion> listOpiniones = opinionRepository.findAll();

        logger.info("Cargadas {} opiniones.", listOpiniones.size());

        // las paso a la vista para mostrarlas en la tabla
        model.addAttribute("listOpiniones", listOpiniones);

        return "opinion"; // opinion.html
    }

    // --- 2. FORMULARIO NUEVA OPINIÓN ---
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nueva opinión...");

        // Creo un objeto vacío para el relleno del formulario
        model.addAttribute("opinion", new Opinion());

        //Cargamos las listas de Casas y Huespedes y las pasamos al model para poder recorrerlas en los select del html
        model.addAttribute("listaCasas", casaRuralRepository.findAll());
        model.addAttribute("listaHuespedes", huespedRepository.findAll());

        return "opinion-form"; // opinion-form.html
    }

    // --- 3. FORMULARIO EDITAR OPINIÓN ---
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Editando opinión ID: {}", id);

        // Uso Optional para evitar errores si el ID no existe en la URL
        Optional<Opinion> opinionOptional = opinionRepository.findById(id);

        if (opinionOptional.isPresent()) {
            // Si existe, paso la opinión cargada al formulario
            model.addAttribute("opinion", opinionOptional.get());

            // volvemos a cargar las listas de Casas y Huespedes y las pasamos al model para poder recorrerlas en los select del html
            // por si el usuario quiere cambiar la casa o el autor de la opinión al editar.
            model.addAttribute("listaCasas", casaRuralRepository.findAll());
            model.addAttribute("listaHuespedes", huespedRepository.findAll());

            return "opinion-form";
        } else {
            //mostramos error si no se encuentra la opinion
            logger.warn("No se encontró la opinión ID {}", id);
            return "redirect:/opiniones";
        }
    }

    // --- 4. INSERTAR / ACTUALIZAR ---
    @PostMapping("/insert")
    public String insertOpinion(@ModelAttribute("opinion") Opinion opinion,
                                RedirectAttributes redirectAttributes) {

        logger.info("Guardando opinión con puntuación: {}", opinion.getPuntuacion());

        //para insertar una opinion, no necesitamos una lógica completa ya que tenemos puesta una restriccion del 1 al 5
        // con @min y @max, luego el metodo .save() se encargará de insertar o actualizar según id
        opinionRepository.save(opinion);

        logger.info("Opinión guardada correctamente.");
        redirectAttributes.addFlashAttribute("mensaje", "Opinión registrada con éxito.");

        return "redirect:/opiniones";
    }

    // --- 5. BORRAR ---
    @GetMapping("/delete")
    public String deleteOpinion(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Solicitud de borrado opinión ID: {}", id);

        // verificamos que exista antes de intentar borrar
        if (opinionRepository.existsById(id)) {
            opinionRepository.deleteById(id);
            logger.info("Opinión eliminada.");
            redirectAttributes.addFlashAttribute("mensaje", "Opinión eliminada correctamente.");
        } else {
            logger.warn("Intento de borrar opinión inexistente.");
            redirectAttributes.addFlashAttribute("errorMessage", "Error: La opinión no existe.");
        }

        return "redirect:/opiniones";
    }
}