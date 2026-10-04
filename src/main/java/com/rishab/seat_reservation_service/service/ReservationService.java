package com.rishab.seat_reservation_service.service;

import com.rishab.seat_reservation_service.dto.ReservationResponse;
import com.rishab.seat_reservation_service.dto.ReserveSeatRequest;
import com.rishab.seat_reservation_service.entity.Reservation;
import com.rishab.seat_reservation_service.entity.Seat;
import com.rishab.seat_reservation_service.entity.Show;
import com.rishab.seat_reservation_service.entity.UserShowBooking;
import com.rishab.seat_reservation_service.exception.SeatTakenException;
import com.rishab.seat_reservation_service.exception.ShowNotFoundException;
import com.rishab.seat_reservation_service.exception.UserLimitExceededException;
import com.rishab.seat_reservation_service.repository.ReservationRepository;
import com.rishab.seat_reservation_service.repository.SeatRepository;
import com.rishab.seat_reservation_service.repository.ShowRepository;
import com.rishab.seat_reservation_service.repository.UserShowBookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final UserShowBookingRepository userShowBookingRepository;

    public ReservationService(
            ShowRepository showRepository,
            SeatRepository seatRepository,
            ReservationRepository reservationRepository, UserShowBookingRepository userShowBookingRepository
    ) {
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.userShowBookingRepository = userShowBookingRepository;
    }

    private String computeCanonicalHash(List<String> seats) {
        List<String> sortedSeats = seats.stream().distinct().sorted().toList();
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(String.join(",", sortedSeats).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error computing request hash", e);
        }
    }

    @Transactional
    public ReservationResponse reserve(
            Long showId,
            String userId,
            ReserveSeatRequest request
    ) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));

        List<String> seatCodes = request.seats()
                .stream()
                .distinct()
                .sorted()
                .toList();

        List<Seat> seats = seatRepository.findSeatsForUpdate(
                showId,
                seatCodes
        );

        if (seats.size() != seatCodes.size()) {
            throw new IllegalArgumentException("One or more seats do not exist");
        }

        boolean anyTaken = seats.stream()
                .anyMatch(seat -> seat.getStatus() != com.rishab.seat_reservation_service.entity.SeatStatus.AVAILABLE);

        if (anyTaken) {
                throw new SeatTakenException("One or more seats are already taken");
            }


        userShowBookingRepository.createIfAbsent(showId, userId);

        UserShowBooking userShowBooking =
                userShowBookingRepository.findForUpdate(showId, userId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "User booking state could not be loaded"
                                )
                        );

        int requestedSeats = seats.size();
        if (userShowBooking.getBookingCount() + requestedSeats
                > show.getPerUserLimit()) {

            throw new UserLimitExceededException(
                    "User booking limit is " + show.getPerUserLimit()
            );
        }
        long amountPaise = show.getPricePaise() * seats.size();

        Reservation reservation = new Reservation(
                show,
                userId,
                amountPaise,
                seats
        );

        seats.forEach(Seat::confirm);
        reservationRepository.save(reservation);
        userShowBooking.addSeats(requestedSeats);



        return new ReservationResponse(
                reservation.getId(),
                show.getId(),
                userId,
                seats.stream()
                        .map(Seat::getSeatCode)
                        .toList(),
                amountPaise,
                reservation.getStatus().name()
        );


    }
}