package com.rishab.seat_reservation_service.controller;

import com.rishab.seat_reservation_service.dto.ReservationResponse;
import com.rishab.seat_reservation_service.dto.ReserveSeatRequest;
import com.rishab.seat_reservation_service.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/reservations")
public class ReservationManagementController {

    private final ReservationService reservationService;

    public ReservationManagementController(
            ReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long reservationId,
            Authentication authentication
    ) {
        reservationService.cancel(
                reservationId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}