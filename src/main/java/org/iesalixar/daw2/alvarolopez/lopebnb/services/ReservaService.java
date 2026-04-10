package org.iesalixar.daw2.alvarolopez.lopebnb.services;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.ReservaDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Huesped;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Reserva;
import org.iesalixar.daw2.alvarolopez.lopebnb.mappers.ReservaMapper;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.CasaRuralRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.HuespedRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

	private static final Logger logger = LoggerFactory.getLogger(ReservaService.class);

	@Autowired
	private ReservaRepository reservaRepository;
	@Autowired
	private CasaRuralRepository casaRuralRepository;
	@Autowired
	private HuespedRepository huespedRepository;
	@Autowired
	private ReservaMapper reservaMapper;

	// --- 1. LISTAR PAGINADO CON FILTROS ---
	public Page<ReservaDTO> getAllReservas(Long casaRuralId, Long huespedId, LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
		try {
			logger.info("Solicitando reservas con filtros -> Casa: {}, Huésped: {}, Desde: {}, Hasta: {}",
					casaRuralId, huespedId, fechaDesde, fechaHasta);

			Page<Reserva> reservas = reservaRepository.findByFiltros(casaRuralId, huespedId, fechaDesde, fechaHasta, pageable);
			logger.info("Se han encontrado {} reservas.", reservas.getNumberOfElements());

			return reservas.map(reservaMapper::toDTO);
		} catch (Exception e) {
			logger.error("Error al obtener las reservas paginadas: {}", e.getMessage());
			throw new RuntimeException("Error interno al listar las reservas.", e);
		}
	}

	// --- 2. OBTENER POR ID ---
	public Optional<ReservaDTO> getReservaById(Long id) {
		try {
			return reservaRepository.findById(id).map(reservaMapper::toDTO);
		} catch (Exception e) {
			logger.error("Error al buscar reserva por ID {}: {}", id, e.getMessage());
			throw new RuntimeException("Error interno al buscar la reserva.", e);
		}
	}

	// --- 3. CREAR RESERVA ---
	public ReservaDTO createReserva(@Valid ReservaDTO dto) {
		try {
			logger.info("Iniciando creación de reserva para Casa {} y Huésped {}", dto.getCasaRuralId(), dto.getHuespedId());

			if (!dto.getFechaSalida().isAfter(dto.getFechaEntrada())) {
				throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada.");
			}

			// validar solapamientos usando el método que devuelve la lista
			List<Reserva> conflictos = reservaRepository.findReservasConflictivas(dto.getCasaRuralId(), dto.getFechaEntrada(), dto.getFechaSalida());

			if (!conflictos.isEmpty()) {
				Reserva choque = conflictos.get(0);
				throw new IllegalArgumentException("La casa rural ya está reservada en esas fechas. (Choque con reserva del " +
						choque.getFechaEntrada() + " al " + choque.getFechaSalida() + ").");
			}

			CasaRural casa = casaRuralRepository.findById(dto.getCasaRuralId())
					.orElseThrow(() -> new IllegalArgumentException("Casa rural no encontrada."));
			Huesped huesped = huespedRepository.findById(dto.getHuespedId())
					.orElseThrow(() -> new IllegalArgumentException("Huésped no encontrado."));

			Reserva reserva = reservaMapper.toEntity(dto, casa, huesped);

			// cálculo seguro del importe
			long dias = ChronoUnit.DAYS.between(dto.getFechaEntrada(), dto.getFechaSalida());
			reserva.setImporteTotal(dias * casa.getPrecioNoche());

			Reserva savedReserva = reservaRepository.save(reserva);
			return reservaMapper.toDTO(savedReserva);

		} catch (IllegalArgumentException e) {
			logger.warn("Validación fallida al crear reserva: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error inesperado al crear la reserva: {}", e.getMessage());
			throw new RuntimeException("Error interno al crear la reserva.", e);
		}
	}

	// --- 4. ACTUALIZAR RESERVA ---
	public ReservaDTO updateReserva(Long id, @Valid ReservaDTO dto) {
		try {
			logger.info("Actualizando reserva con ID {}", id);
			Reserva existente = reservaRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));

			if (!dto.getFechaSalida().isAfter(dto.getFechaEntrada())) {
				throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada.");
			}

			// validar solapamientos (excluyendo esta misma reserva)
			List<Reserva> conflictos = reservaRepository.findReservasConflictivasExcludingId(
					dto.getCasaRuralId(), dto.getFechaEntrada(), dto.getFechaSalida(), id);

			if (!conflictos.isEmpty()) {
				Reserva choque = conflictos.get(0);
				throw new IllegalArgumentException("No se pueden actualizar las fechas. La casa ya está reservada del " +
						choque.getFechaEntrada() + " al " + choque.getFechaSalida() + ".");
			}

			CasaRural casa = casaRuralRepository.findById(dto.getCasaRuralId())
					.orElseThrow(() -> new IllegalArgumentException("Casa rural no encontrada."));
			Huesped huesped = huespedRepository.findById(dto.getHuespedId())
					.orElseThrow(() -> new IllegalArgumentException("Huésped no encontrado."));

			existente.setFechaEntrada(dto.getFechaEntrada());
			existente.setFechaSalida(dto.getFechaSalida());
			existente.setCasaRural(casa);
			existente.setHuesped(huesped);

			long dias = ChronoUnit.DAYS.between(dto.getFechaEntrada(), dto.getFechaSalida());
			existente.setImporteTotal(dias * casa.getPrecioNoche());

			Reserva savedReserva = reservaRepository.save(existente);
			return reservaMapper.toDTO(savedReserva);

		} catch (IllegalArgumentException e) {
			logger.warn("Validación fallida al actualizar reserva: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error inesperado al actualizar la reserva {}: {}", id, e.getMessage());
			throw new RuntimeException("Error interno al actualizar la reserva.", e);
		}
	}

	// --- 5. BORRAR RESERVA ---
	public void deleteReserva(Long id) {
		try {
			if (!reservaRepository.existsById(id)) {
				throw new IllegalArgumentException("Reserva no encontrada con ID: " + id);
			}
			reservaRepository.deleteById(id);
			logger.info("Reserva {} borrada correctamente.", id);
		} catch (IllegalArgumentException e) {
			logger.warn("No se pudo borrar la reserva: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error inesperado al borrar la reserva {}: {}", id, e.getMessage());
			throw new RuntimeException("Error interno al borrar la reserva.", e);
		}
	}
}