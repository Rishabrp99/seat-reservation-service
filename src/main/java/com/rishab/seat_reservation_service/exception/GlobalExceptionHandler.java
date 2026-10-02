package com.rishab.seat_reservation_service.exception;

import org.springframework.http.HttpStatus;
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
}