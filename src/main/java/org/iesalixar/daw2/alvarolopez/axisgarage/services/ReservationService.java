package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.ReservationDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Vehicle;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Renter;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.ReservationMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.VehicleRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.RenterRepository;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.ReservationRepository;
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

/**
 * Servicio que orquesta el ciclo de vida completo de una reserva en Axis Garage.
 * <p>
 * Cubre la creación (con detección de solapamiento de fechas), la consulta filtrada
 * por vehículo o cliente, la actualización y el borrado. Al confirmar una reserva nueva,
 * este servicio dispara automáticamente dos acciones adicionales: la creación de una
 * cobertura STANDARD asociada y el envío de un correo de confirmación al cliente.
 * </p>
 */
@Service
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private RenterRepository renterRepository;
    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private CoverageService coverageService;

    @Autowired
    private EmailService emailService;

    // --- 1. LISTAR PAGINADO CON FILTROS ---
    /**
     * Retorna reservas basadas en la combinación de distintos filtros y en formato de página.
     * 
     * @param vehicleId ID del vehículo reservado (opcional).
     * @param renterId ID del huésped de la reserva (opcional).
     * @param fechaDesde Fecha mínima para la búsqueda (opcional).
     * @param fechaHasta Fecha máxima para la búsqueda (opcional).
     * @param pageable Configuración de paginación.
     * @return Page de dtos de reservas localizadas.
     */
    public Page<ReservationDTO> getAllReservations(Long vehicleId, Long renterId, LocalDate fechaDesde,
            LocalDate fechaHasta, Pageable pageable) {
        try {
            logger.info("Solicitando reservas con filtros -> Vehículo: {}, Huésped: {}, Desde: {}, Hasta: {}",
                    vehicleId, renterId, fechaDesde, fechaHasta);

            Page<Reservation> reservations = reservationRepository.findByFiltros(vehicleId, renterId, fechaDesde,
                    fechaHasta, pageable);
            logger.info("Se han encontrado {} reservas.", reservations.getNumberOfElements());

            return reservations.map(reservationMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener las reservas paginadas: {}", e.getMessage());
            throw new RuntimeException("Error interno al listar las reservas.", e);
        }
    }

    // --- 2. OBTENER POR ID ---
    /**
     * Accede de forma individual a la data de una reserva por su id.
     *
     * @param id de la reserva objetivo.
     * @return Un encapsulamiento Optional con los datos o null.
     */
    public Optional<ReservationDTO> getReservationById(Long id) {
        try {
            return reservationRepository.findById(id).map(reservationMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar reserva por ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error interno al buscar la reserva.", e);
        }
    }

    // --- 3. CREAR RESERVA ---
    /**
     * Da de alta en el sistema una reserva, comprueba duplicados de fechas, solapamiento y genera una póliza STANDARD por defecto.
     *
     * @param dto El objeto data transfer object con fechas e información.
     * @throws IllegalArgumentException si falta integridad lógica o de negocio en las horas.
     * @return ReservationDTO con un tracker ID.
     */
    public ReservationDTO createReservation(@Valid ReservationDTO dto) {
        try {
            logger.info("Iniciando creación de reserva para Vehículo {} y Huésped {}", dto.getVehicleId(),
                    dto.getRenterId());

            if (!dto.getEndDate().isAfter(dto.getStartDate())) {
                throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada.");
            }

            // validar solapamientos usando el método que devuelve la lista
            List<Reservation> conflictos = reservationRepository.findReservasConflictivas(dto.getVehicleId(),
                    dto.getStartDate(), dto.getEndDate());

            if (!conflictos.isEmpty()) {
                Reservation choque = conflictos.get(0);
                throw new IllegalArgumentException(
                        "El vehículo ya está reservado en esas fechas. (Choque con reserva del " +
                                choque.getStartDate() + " al " + choque.getEndDate() + ").");
            }

            Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado."));
            Renter renter = renterRepository.findById(dto.getRenterId())
                    .orElseThrow(() -> new IllegalArgumentException("Huésped no encontrado."));

            Reservation reservation = reservationMapper.toEntity(dto, vehicle, renter);

            Reservation savedReservation = reservationRepository.save(reservation);

            // Auto-crear cobertura STANDARD para la nueva reserva
            coverageService.createStandardCoverage(savedReservation);

            // Notificar al cliente por correo electrónico (el try/catch interno no bloquea si falla)
            emailService.sendConfirmationEmail(
                    renter.getEmail(),
                    renter.getName() + " " + renter.getLastName(),
                    vehicle.getBrand() + " " + vehicle.getModel(),
                    dto.getStartDate().toString(),
                    dto.getEndDate().toString(),
                    savedReservation.getTotalPrice()
            );

            return reservationMapper.toDTO(savedReservation);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al crear reserva: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear la reserva: {}", e.getMessage());
            throw new RuntimeException("Error interno al crear la reserva.", e);
        }
    }

    // --- 4. ACTUALIZAR RESERVA ---
    /**
     * Actualiza las fechas y recalcula el precio total de una reserva existente.
     * Antes de guardar, comprueba que el nuevo rango de fechas no colisione con
     * otras reservas del mismo vehículo (excluyendo la propia reserva que se edita).
     *
     * @param id  Identificador de la reserva a modificar.
     * @param dto DTO con los nuevos datos de fechas, vehículo y cliente.
     * @return ReservationDTO con los datos actualizados.
     * @throws IllegalArgumentException si la fecha de salida no es posterior a la de entrada,
     *                                  si la reserva no existe o si las fechas solapan con otra reserva.
     */
    public ReservationDTO updateReservation(Long id, @Valid ReservationDTO dto) {
        try {
            logger.info("Actualizando reserva con ID {}", id);
            Reservation existente = reservationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));

            if (!dto.getEndDate().isAfter(dto.getStartDate())) {
                throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada.");
            }

            // validar solapamientos (excluyendo esta misma reserva)
            List<Reservation> conflictos = reservationRepository.findReservasConflictivasExcludingId(
                    dto.getVehicleId(), dto.getStartDate(), dto.getEndDate(), id);

            if (!conflictos.isEmpty()) {
                Reservation choque = conflictos.get(0);
                throw new IllegalArgumentException(
                        "No se pueden actualizar las fechas. El vehículo ya está reservado del " +
                                choque.getStartDate() + " al " + choque.getEndDate() + ".");
            }

            Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado."));
            Renter renter = renterRepository.findById(dto.getRenterId())
                    .orElseThrow(() -> new IllegalArgumentException("Huésped no encontrado."));

            existente.setStartDate(dto.getStartDate());
            existente.setEndDate(dto.getEndDate());
            existente.setVehicle(vehicle);
            existente.setRenter(renter);

            long dias = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate());
            existente.setTotalPrice(dias * vehicle.getPricePerDay());

            Reservation savedReservation = reservationRepository.save(existente);
            return reservationMapper.toDTO(savedReservation);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al actualizar reserva: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar la reserva {}: {}", id, e.getMessage());
            throw new RuntimeException("Error interno al actualizar la reserva.", e);
        }
    }

    // --- 5. BORRAR RESERVA ---
    /**
     * Elimina definitivamente una reserva del sistema.
     *
     * @param id Identificador de la reserva a eliminar.
     * @throws IllegalArgumentException si no existe ninguna reserva con ese identificador.
     */
    public void deleteReservation(Long id) {
        try {
            if (!reservationRepository.existsById(id)) {
                throw new IllegalArgumentException("Reserva no encontrada con ID: " + id);
            }
            reservationRepository.deleteById(id);
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
