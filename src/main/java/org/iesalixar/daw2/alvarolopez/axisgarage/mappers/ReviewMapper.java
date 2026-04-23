package org.iesalixar.daw2.alvarolopez.axisgarage.mappers;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.ReviewDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Reservation;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Renter;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewDTO toDTO(Review entity) {
        if (entity == null)
            return null;

        ReviewDTO dto = new ReviewDTO();
        dto.setId(entity.getId());
        dto.setRating(entity.getRating());
        dto.setComment(entity.getComment());

        // Extraemos datos de la reserva
        if (entity.getReservation() != null) {
            dto.setReservationId(entity.getReservation().getId());
            dto.setReservationString("Reserva ID " + entity.getReservation().getId());
        }

        // Extraemos datos del renter
        if (entity.getRenter() != null) {
            dto.setRenterId(entity.getRenter().getId());
            dto.setRenterName(entity.getRenter().getName() + " " + entity.getRenter().getLastName());
        }

        return dto;
    }

    public Review toEntity(ReviewDTO dto, Reservation reservation, Renter renter) {
        if (dto == null)
            return null;

        Review entity = new Review();
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setRating(dto.getRating());
        entity.setComment(dto.getComment());
        entity.setReservation(reservation);
        entity.setRenter(renter);

        return entity;
    }
}
