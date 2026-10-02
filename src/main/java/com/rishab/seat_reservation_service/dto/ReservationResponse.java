package com.rishab.seat_reservation_service.dto;

import java.util.List;

public record ReservationResponse(
        Long reservationId,
        Long showId,
        String userId,
        List<String> seats,
        long amountPaise,
        String status
) {
}