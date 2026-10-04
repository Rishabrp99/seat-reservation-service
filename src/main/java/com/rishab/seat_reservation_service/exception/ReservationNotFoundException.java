package com.rishab.seat_reservation_service.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(Long reservationId) {
        super("Reservation not found: " + reservationId);
    }
}