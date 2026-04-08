package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Huesped;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.HuespedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/huespedes")
public class HuespedController {

    private static final Logger logger = LoggerFactory.getLogger(HuespedController.class);

    // inyectamos el repositorio para interactuar con la BBDD
    @Autowired
    private HuespedRepository huespedRepository;

    // --- 1. LISTAR HUÉSPEDES ---
    @GetMapping
    public String listHuespedes(Model model) {
        logger.info("Solicitando la lista de huéspedes...");

        // Uso el método estándar de JPA para traer todos los registros
        List<Huesped> listHuespedes = huespedRepository.findAll();

        logger.info("Se han cargado {} huéspedes.", listHuespedes.size());

        // Paso la lista a la vista para pintarla en la tabla HTML
        model.addAttribute("listHuespedes", listHuespedes);

        return "huesped";
    }

    // --- 2. FORMULARIO NUEVO ---
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo huésped...");

        //Creo un objeto vacio, para que thymeleaf pueda vincular los inputs a los atributos
        model.addAttribute("huesped", new Huesped());

        return "huesped-form";
    }

    // --- 3. FORMULARIO EDITAR ---
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Solicitando edición para huésped con ID: {}", id);

        // Uso Optional para evitar NullPointerExceptions si el ID no existe
        Optional<Huesped> huespedOptional = huespedRepository.findById(id);

        if (huespedOptional.isPresent()) {
            // Si lo encuentro, lo mando al formulario cargado con sus datos
            model.addAttribute("huesped", huespedOptional.get());
            return "huesped-form";
        } else {
            // Si el usuario pone un ID falso en la URL, lo devuelvo al listado
            logger.warn("No se encontró el huésped con ID {}", id);
            return "redirect:/huespedes";
        }
    }

    // --- 4. INSERTAR / ACTUALIZAR (MÉTODO COMPLEJO) ---
    // este método gestiona tanto Crear como Editar.
    @PostMapping("/insert")
    public String insertHuesped(@ModelAttribute("huesped") Huesped huesped,
                                RedirectAttributes redirectAttributes) {

        logger.info("Intentando guardar huésped: {} {}", huesped.getNombre(), huesped.getApellidos());

        // evitar duplicados
        // Solo compruebo si existen el DNI/Email/Teléfono si es un NUEVO registro (id == null).
        // Si estoy editando (id != null), permito guardar porque el DNI ya existe (es el mío).

        if (huesped.getId() == null) {

            // 1. Comprobar DNI
            if (huespedRepository.findByDni(huesped.getDni()).isPresent()) {
                logger.warn("El DNI {} ya existe.", huesped.getDni());
                redirectAttributes.addFlashAttribute("errorMessage", "Error: Ya existe un huésped con ese DNI.");
                return "redirect:/huespedes/new";
            }

            // 2. Comprobar Email
            if (huespedRepository.findByEmail(huesped.getEmail()).isPresent()) {
                logger.warn("El email {} ya existe.", huesped.getEmail());
                redirectAttributes.addFlashAttribute("errorMessage", "Error: El email ya está registrado.");
                return "redirect:/huespedes/new";
            }

            // 3. Comprobar Teléfono
            if (huespedRepository.findByTelefono(huesped.getTelefono()).isPresent()) {
                logger.warn("El teléfono {} ya existe.", huesped.getTelefono());
                redirectAttributes.addFlashAttribute("errorMessage", "Error: El teléfono ya está registrado.");
                return "redirect:/huespedes/new";
            }
        }

        // El método .save() detecta automáticamente si tiene ID (Update) o no (Insert)
        huespedRepository.save(huesped);

        logger.info("Huésped guardado con éxito (ID: {})", huesped.getId());
        redirectAttributes.addFlashAttribute("mensaje", "Huésped guardado correctamente.");

        return "redirect:/huespedes";
    }

    // --- 5. BORRAR ---
    @GetMapping("/delete")
    public String deleteHuesped(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Solicitud de borrado para huésped ID: {}", id);

        // Verifico existencia antes de borrar para mayor seguridad
        if (huespedRepository.existsById(id)) {
            huespedRepository.deleteById(id);
            logger.info("Huésped eliminado.");
            redirectAttributes.addFlashAttribute("mensaje", "Huésped eliminado correctamente.");
        } else {
            logger.warn("Intento de borrar huésped inexistente.");
            redirectAttributes.addFlashAttribute("errorMessage", "Error: El huésped no existe.");
        }

        return "redirect:/huespedes";
    }
}