package com.rishab.seat_reservation_service.dto;

import java.util.List;

public record ShowResponse(
        Long id,
        String name,
        long pricePaise,
        int perUserLimit,
        int totalSeats,
        int availableSeats,
        int heldSeats,
        int confirmedSeats,
        List<SeatResponse> seats
) {
}