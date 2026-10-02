package com.rishab.seat_reservation_service.exception;

public class SeatTakenException extends RuntimeException {

    public SeatTakenException(String message) {
        super(message);
    }
}