package com.rishab.seat_reservation_service.repository;

import com.rishab.seat_reservation_service.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {
}