package com.rishab.seat_reservation_service.exception;

public class UserLimitExceededException extends RuntimeException {

    public UserLimitExceededException(String message) {
        super(message);
    }
}