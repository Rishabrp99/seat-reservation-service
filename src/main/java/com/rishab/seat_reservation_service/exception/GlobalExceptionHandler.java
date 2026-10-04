package com.rishab.seat_reservation_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShowNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleShowNotFound(
            ShowNotFoundException exception
    ) {
        return Map.of(
                "error", "SHOW_NOT_FOUND",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(SeatTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleSeatTaken(
            SeatTakenException exception
    ) {
        return Map.of(
                "error", "SEAT_TAKEN",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(UserLimitExceededException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleUserLimitExceeded(
            UserLimitExceededException exception
    ) {
        return Map.of(
                "error", "PER_USER_LIMIT_EXCEEDED",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<Map<String, String>> handleIdempotencyConflict(IdempotencyConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "IDEMPOTENCY_CONFLICT", "message", ex.getMessage()));
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleReservationNotFound(
            ReservationNotFoundException exception
    ) {
        return Map.of(
                "error", "RESERVATION_NOT_FOUND",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(ReservationCancellationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleCancellation(
            ReservationCancellationException exception
    ) {
        return Map.of(
                "error", "CANCELLATION_NOT_ALLOWED",
                "message", exception.getMessage()
        );
    }
}