package com.rishab.seat_reservation_service.exception;

public class ReservationCancellationException extends RuntimeException {

    public ReservationCancellationException(String message) {
        super(message);
    }
}