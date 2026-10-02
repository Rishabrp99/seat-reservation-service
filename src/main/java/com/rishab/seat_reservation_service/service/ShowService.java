package com.rishab.seat_reservation_service.service;
import com.rishab.seat_reservation_service.dto.SeatResponse;
import com.rishab.seat_reservation_service.dto.CreateShowRequest;
import com.rishab.seat_reservation_service.dto.ShowResponse;
import com.rishab.seat_reservation_service.entity.Seat;
import com.rishab.seat_reservation_service.entity.Show;
import com.rishab.seat_reservation_service.exception.ShowNotFoundException;
import com.rishab.seat_reservation_service.repository.SeatRepository;
import com.rishab.seat_reservation_service.repository.ShowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;

    public ShowService(
            ShowRepository showRepository,
            SeatRepository seatRepository
    ) {
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public ShowResponse createShow(CreateShowRequest request) {
        Show show = new Show(
                request.name(),
                request.pricePaise(),
                4
        );

        showRepository.save(show);

        List<Seat> seats = request.seats()
                .stream()
                .distinct()
                .map(seatCode -> new Seat(show, seatCode))
                .toList();

        seatRepository.saveAll(seats);

        List<SeatResponse> seatResponses = seats.stream()
                .map(seat -> new SeatResponse(
                        seat.getSeatCode(),
                        seat.getStatus().name()
                ))
                .toList();

        return new ShowResponse(
                show.getId(),
                show.getName(),
                show.getPricePaise(),
                show.getPerUserLimit(),
                seats.size(),       // total
                seats.size(),       // all newly-created seats are available
                0,                  // held
                0,                  // confirmed
                seatResponses
        );
    }

    @Transactional(readOnly = true)
    public ShowResponse getShow(Long showId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));
        List<Seat> seats = seatRepository
                .findByShowIdOrderBySeatCode(showId);

        int available = 0;
        int held = 0;
        int confirmed = 0;

        for (Seat seat : seats) {
            switch (seat.getStatus()) {
                case AVAILABLE -> available++;
                case HELD -> held++;
                case CONFIRMED -> confirmed++;
            }
        }

        List<SeatResponse> seatResponses = seats.stream()
                .map(seat -> new SeatResponse(
                        seat.getSeatCode(),
                        seat.getStatus().name()
                ))
                .toList();

        return new ShowResponse(
                show.getId(),
                show.getName(),
                show.getPricePaise(),
                show.getPerUserLimit(),
                seats.size(),
                available,
                held,
                confirmed,
                seatResponses
        );
    }
}