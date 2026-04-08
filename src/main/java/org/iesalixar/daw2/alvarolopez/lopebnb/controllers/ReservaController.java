package org.iesalixar.daw2.alvarolopez.lopebnb.controllers;

import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Reserva;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.CasaRuralRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.HuespedRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private static final Logger logger = LoggerFactory.getLogger(ReservaController.class);

    //inyectamos la lista de casas y huespedes para mostrarlas en el form
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private CasaRuralRepository casaRuralRepository;

    @Autowired
    private HuespedRepository huespedRepository;

    // --- 1. LISTAR RESERVAS ---
    @GetMapping
    public String listReservas(Model model) {
        logger.info("Solicitando lista de reservas...");
        List<Reserva> listReservas = reservaRepository.findAll();
        model.addAttribute("listReservas", listReservas);
        return "reserva";
    }

    // --- 2. FORMULARIO NUEVA RESERVA ---
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Formulario nueva reserva...");
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("listaCasas", casaRuralRepository.findAll());
        model.addAttribute("listaHuespedes", huespedRepository.findAll());
        return "reserva-form";
    }

    // --- 3. FORMULARIO EDITAR ---
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Editando reserva ID: {}", id);
        Optional<Reserva> resOptional = reservaRepository.findById(id);

        if (resOptional.isPresent()) {
            model.addAttribute("reserva", resOptional.get());
            model.addAttribute("listaCasas", casaRuralRepository.findAll());
            model.addAttribute("listaHuespedes", huespedRepository.findAll());
            return "reserva-form";
        } else {
            return "redirect:/reservas";
        }
    }

    // --- 4. INSERTAR / ACTUALIZAR---
    @PostMapping("/insert")
    public String insertReserva(@ModelAttribute("reserva") Reserva reserva,
                                RedirectAttributes redirectAttributes) {

        logger.info("Procesando reserva para la casa ID: {}", reserva.getCasaRural().getId());

        // 🔥 VALIDACIÓN DE FECHAS (isAfter):
        // "Profe, valido a nivel de servidor que la fecha de salida sea posterior a la entrada.
        // Aunque lo ponga en el HTML, nunca hay que fiarse de lo que viene del cliente."
        //validamos a nivel de servidor que la fecha de salida sea posterior a la de entrada
        //aunque lo ponga en el html, lo aseguramos en el servidor con isAfter
        if (!reserva.getFechaSalida().isAfter(reserva.getFechaEntrada())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: La fecha de salida debe ser posterior a la de entrada.");
            return "redirect:/reservas/new";
        }

        //control de overbooking
        //para evitar que 2 personas reserven la misma casa en los mismos días o dentro del tramo, uso un metodo personalizado
        // en el repositorio (findReservasConflictivas). Si devuelve algún resultado, significa que la casa
        // está ocupada, así que bloqueo la operación y le digo al usuario exactamente qué fechas están cogidas."

        if (reserva.getId() == null) {
            List<Reserva> conflictos = reservaRepository.findReservasConflictivas(
                    reserva.getCasaRural().getId(),
                    reserva.getFechaEntrada(),
                    reserva.getFechaSalida()
            );

            if (!conflictos.isEmpty()) {
                Reserva ocupada = conflictos.get(0);
                String mensajeError = String.format(
                        "Error: La casa ya está reservada en esas fechas (Ocupada del %s al %s).",
                        ocupada.getFechaEntrada(),
                        ocupada.getFechaSalida()
                );
                redirectAttributes.addFlashAttribute("errorMessage", mensajeError);
                return "redirect:/reservas/new";
            }
        }

        //cálculo del precio automatico
        //en lugar de pedir el precio total, lo calculamos con ChronoUnit.DAYS para extraer la ç
        // diferencia de dias y multiplicarla por el precio de la noche
        Optional<CasaRural> casaOpt = casaRuralRepository.findById(reserva.getCasaRural().getId());

        if (casaOpt.isPresent()) {
            CasaRural casa = casaOpt.get();

            // calculo los días
            long dias = ChronoUnit.DAYS.between(reserva.getFechaEntrada(), reserva.getFechaSalida());

            // calculo el total
            double total = dias * casa.getPrecioNoche();

            // lo asignamos al objeto
            reserva.setImporteTotal(total);

            logger.info("Precio calculado: {} días * {}€ = {}€", dias, casa.getPrecioNoche(), total);
        }

        // guardo en BBDD
        reservaRepository.save(reserva);

        redirectAttributes.addFlashAttribute("mensaje", "Reserva guardada correctamente.");
        return "redirect:/reservas";
    }

    // --- 5. BORRAR RESERVA ---
    @GetMapping("/delete")
    public String deleteReserva(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        if (reservaRepository.existsById(id)) {
            reservaRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Reserva cancelada/eliminada.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Reserva no encontrada.");
        }
        return "redirect:/reservas";
    }
}