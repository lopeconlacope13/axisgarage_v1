package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.CasaRuralRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.PropietarioRepository;
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
@RequestMapping("/casas")
public class CasaRuralController {

    private static final Logger logger = LoggerFactory.getLogger(CasaRuralController.class);

    @Autowired
    private CasaRuralRepository casaRuralRepository;
    // Al crear o editar una casa, necesitamos la lista de propietarios a la hora de selccionarlo en el formulario
    @Autowired
    private PropietarioRepository propietarioRepository;

    // --- 1. LISTAR ---
    @GetMapping
    public String listCasaRural(Model model) {
        logger.info("Solicitando la lista de todas las casas rurales...");

        // Utilizo el método findAll() que heredo de JpaRepository para traer todo de la BBDD
        List<CasaRural> listCasaRural = casaRuralRepository.findAll();

        logger.info("Se han cargado {} casas rurales.", listCasaRural.size());

        // Paso la lista a la vista para recorrerla con un th:each
        model.addAttribute("listCasaRural", listCasaRural);

        return "casa-rural";
    }

    // --- 2. FORMULARIO NUEVA CASA ---
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nueva casa rural...");

        // Pasamos el objeto vacío para que Thymeleaf pueda vincular los campos
        model.addAttribute("casa_rural", new CasaRural());

        //Cargamos los propietarios y los mandamos a la vista para poder pintar el desplegable
        model.addAttribute("listaPropietarios", propietarioRepository.findAll());

        return "casa-rural-form";
    }

    // --- 3. FORMULARIO EDITAR (Por ID en URL) ---
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para la casa ID: {}", id);

        //Usamos optional porque findById no puede no encontrar nada
        Optional<CasaRural> casaOptional = casaRuralRepository.findById(id);

        if (casaOptional.isEmpty()) {
            logger.warn("No se encontró la casa con ID {}", id);
            return "redirect:/casas"; // Si no existe, lo devuelvo al listado por seguridad
        }

        // Si existe, saco el objeto del Optional y lo mando al formulario
        model.addAttribute("casa_rural", casaOptional.get());

        // Vuelvo a cargar la lista de propietarios por si quiere cambiar de dueño
        model.addAttribute("listaPropietarios", propietarioRepository.findAll());

        return "casa-rural-form";
    }

    // --- 4. INSERTAR ---
    @PostMapping("/insert")
    public String insertCasaRural(@ModelAttribute("casa_rural") CasaRural casaRural,
                                  RedirectAttributes redirectAttributes) {

        logger.info("Intentando guardar casa: {}", casaRural.getNombre());

        // tenemos que distinguir si estoy CREANDO o EDITANDO.
        // Si el ID es null -> Es NUEVA.
        // Si es nueva, compruebo si ya existe otra casa con ese mismo nombre para evitar duplicados.
        if (casaRural.getId() == null && casaRuralRepository.existsByNombre(casaRural.getNombre())) {
            logger.warn("El nombre '{}' ya existe.", casaRural.getNombre());

            // Si existe, mando un error y lo devuelvo al formulario de creación
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Ya existe una casa con ese nombre.");
            return "redirect:/casas/new";
        }

        //El método .save de JPA ya hace el trabajo por nosotros
        // - Si el objeto tiene ID -> hace UPDATE
        // - Si el objeto NO tiene ID -> hace INSERT
        casaRuralRepository.save(casaRural);

        logger.info("Casa guardada con éxito (ID: {})", casaRural.getId());
        redirectAttributes.addFlashAttribute("mensaje", "Casa rural guardada correctamente.");

        return "redirect:/casas";
    }

    // --- 5. BORRAR ---
    @GetMapping("/delete")
    public String deleteCasaRural(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Solicitud de borrado para casa ID: {}", id);

        // Antes de borrar, verifico que exista para evitar excepciones de base de datos
        if (casaRuralRepository.existsById(id)) {
            casaRuralRepository.deleteById(id);
            logger.info("Casa eliminada.");
            redirectAttributes.addFlashAttribute("mensaje", "Casa eliminada correctamente.");
        } else {
            logger.warn("Intento de borrar casa inexistente.");
            redirectAttributes.addFlashAttribute("errorMessage", "Error: La casa no existe.");
        }

        return "redirect:/casas";
    }
}