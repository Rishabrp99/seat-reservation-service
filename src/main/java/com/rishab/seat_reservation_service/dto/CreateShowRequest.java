package com.rishab.seat_reservation_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateShowRequest(
        @NotBlank String name,
        @NotEmpty List<@NotBlank String> seats,
        @Min(0) long pricePaise
) {
}