package com.rishab.seat_reservation_service.controller;

import com.rishab.seat_reservation_service.dto.ReservationResponse;
import com.rishab.seat_reservation_service.dto.ReserveSeatRequest;
import com.rishab.seat_reservation_service.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/shows")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

    @PostMapping("/{showId}/reserve")
    public ResponseEntity<ReservationResponse> reserve(
            @PathVariable Long showId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReserveSeatRequest request
    ) {
        ReservationResponse response =
                reservationService.reserve(showId, userId, request);

        return ResponseEntity
                .created(
                        URI.create("/reservations/" + response.reservationId())
                )
                .body(response);
    }
}