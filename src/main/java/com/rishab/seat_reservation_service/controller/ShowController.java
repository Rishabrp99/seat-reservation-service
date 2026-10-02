package com.rishab.seat_reservation_service.controller;

import com.rishab.seat_reservation_service.dto.CreateShowRequest;
import com.rishab.seat_reservation_service.dto.ShowResponse;
import com.rishab.seat_reservation_service.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    public ResponseEntity<ShowResponse> createShow(
            @Valid @RequestBody CreateShowRequest request
    ) {
        ShowResponse response = showService.createShow(request);

        return ResponseEntity
                .created(URI.create("/shows/" + response.id()))
                .body(response);
    }

    @GetMapping("/{showId}")
    public ShowResponse getShow(@PathVariable Long showId) {
        return showService.getShow(showId);
    }
}