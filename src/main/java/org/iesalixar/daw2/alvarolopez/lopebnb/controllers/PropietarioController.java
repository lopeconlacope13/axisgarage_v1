package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
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
@RequestMapping("/propietarios")
public class PropietarioController {

    private static final Logger logger = LoggerFactory.getLogger(PropietarioController.class);

    // Inyectamos el repositorio para poder contactar con la Base de Datos
    @Autowired
    private PropietarioRepository propietarioRepository;

    // --- 1. LISTAR ---
    @GetMapping
    public String listPropietarios(Model model) {
        logger.info("Solicitando la lista de todos los propietarios...");
        // pedimos todos los datos al repositorio
        List<Propietario> listPropietarios = propietarioRepository.findAll();

        logger.info("Se han encontrado {} propietarios.", listPropietarios.size());

        // pasamos la lista a la vista
        model.addAttribute("listPropietarios", listPropietarios);

        // devolvemos el nombre del archivo HTML a mostrar
        return "propietario";
    }

    // --- 2. AÑADIR ---
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para crear un nuevo propietario...");

        // pasamos un objeto vacío para que el formulario lo rellene
        model.addAttribute("propietario", new Propietario());

        return "propietario-form";
    }


    // --- 3. EDITAR ---
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Buscando propietario con ID {} para editar...", id);

        // buscamos por ID (devuelve un Optional por si no existe)
        Optional<Propietario> propietarioOptional = propietarioRepository.findById(id);

        if (propietarioOptional.isPresent()) {
            // si existe, lo pasamos al modelo para rellenar los campos
            model.addAttribute("propietario", propietarioOptional.get());
            return "propietario-form";
        } else {
            // si no existe, mostramos error y volvemos al listado
            logger.warn("No se encontró el propietario con ID {}", id);
            return "redirect:/propietarios";
        }
    }

    // --- 4- INSERTAR ---
    @PostMapping("/insert")
    public String insertPropietario(@ModelAttribute("propietario") Propietario propietario,
                                    RedirectAttributes redirectAttributes) {

        logger.info("Intentando guardar propietario: {}", propietario.getEmail());

        // validacion email
        // comprobamos si es un registro nuevo (id es null) y si el email ya existe en la BD
        if (propietario.getId() == null && propietarioRepository.findByEmail(propietario.getEmail()).isPresent()) {
            logger.warn("El email {} ya está registrado.", propietario.getEmail());
            // mandamos mensaje de error
            redirectAttributes.addFlashAttribute("errorMessage", "Error: El email ya está en uso.");
            // volvemos al formulario de creación
            return "redirect:/propietarios/new";
        }

        // validacion telefono
        if (propietario.getId() == null && propietarioRepository.findByTelefono(propietario.getTelefono()).isPresent()) {
            logger.warn("El teléfono {} ya está registrado.", propietario.getTelefono());
            redirectAttributes.addFlashAttribute("errorMessage", "Error: El teléfono ya está en uso.");
            return "redirect:/propietarios/new";
        }

        // si pasa las validaciones, guardamos con .save (auto)
        propietarioRepository.save(propietario);

        logger.info("Propietario guardado con éxito (ID: {})", propietario.getId());
        redirectAttributes.addFlashAttribute("mensaje", "Propietario guardado correctamente.");

        return "redirect:/propietarios";
    }

    // --- 5. BORRAR ---

    @GetMapping("/delete")
    public String deletePropietario(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Solicitud de borrado para propietario ID: {}", id);

        if (propietarioRepository.existsById(id)) {
            propietarioRepository.deleteById(id);
            logger.info("Propietario eliminado.");
            redirectAttributes.addFlashAttribute("mensaje", "Propietario eliminado correctamente.");
        } else {
            logger.warn("Intento de borrar propietario inexistente.");
            redirectAttributes.addFlashAttribute("errorMessage", "Error: El propietario no existe.");
        }

        return "redirect:/propietarios";
    }
}