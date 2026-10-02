package com.rishab.seat_reservation_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ReserveSeatRequest(
        @NotEmpty
        List<@NotBlank String> seats
) {
}