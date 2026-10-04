package com.rishab.seat_reservation_service.service;

import com.rishab.seat_reservation_service.dto.ReservationResponse;
import com.rishab.seat_reservation_service.dto.ReserveSeatRequest;
import com.rishab.seat_reservation_service.entity.*;
import com.rishab.seat_reservation_service.exception.IdempotencyConflictException;
import com.rishab.seat_reservation_service.exception.SeatTakenException;
import com.rishab.seat_reservation_service.exception.ShowNotFoundException;
import com.rishab.seat_reservation_service.exception.UserLimitExceededException;
import com.rishab.seat_reservation_service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final UserShowBookingRepository userShowBookingRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public ReservationService(
            ShowRepository showRepository,
            SeatRepository seatRepository,
            ReservationRepository reservationRepository,
            UserShowBookingRepository userShowBookingRepository,
            IdempotencyKeyRepository idempotencyKeyRepository
    ) {
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.userShowBookingRepository = userShowBookingRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    private String computeCanonicalHash(List<String> seats) {

        List<String> sortedSeats = seats.stream()
                .distinct()
                .sorted()
                .toList();

        try {
            java.security.MessageDigest digest =
                    java.security.MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    String.join(",", sortedSeats)
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error computing request hash",
                    e
            );
        }
    }

    @Transactional
    public ReservationResponse reserve(
            Long showId,
            String userId,
            String idempotencyKey,
            ReserveSeatRequest request
    ) {

        /*
         * STEP 1
         * Validate Idempotency-Key.
         */
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key header is required"
            );
        }

        /*
         * STEP 2
         * Create a canonical representation of the requested seats.
         */
        List<String> requestedSeats = request.seats()
                .stream()
                .sorted()
                .toList();

        String requestHash = computeCanonicalHash(requestedSeats);

        /*
         * STEP 3
         * Atomically claim the idempotency key.
         *
         * inserted == 1
         *      -> first request using this key
         *
         * inserted == 0
         *      -> key already exists
         */
        int inserted = idempotencyKeyRepository.insertIfAbsent(
                userId,
                showId,
                idempotencyKey,
                requestHash
        );

        /*
         * STEP 4
         * Idempotency replay.
         */
        if (inserted == 0) {

            IdempotencyKey existing =
                    idempotencyKeyRepository
                            .findByUserIdAndIdempotencyKey(
                                    userId,
                                    idempotencyKey
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Idempotency record could not be loaded"
                                    )
                            );

            /*
             * Same idempotency key but different request.
             */
            if (!existing.getRequestHash().equals(requestHash)) {

                throw new IdempotencyConflictException(
                        "Idempotency-Key was already used with a different request"
                );
            }

            /*
             * The original request should have stored
             * its reservation ID before committing.
             */
            if (existing.getReservationId() == null) {

                throw new IllegalStateException(
                        "Idempotency record has no reservation"
                );
            }

            /*
             * Return the original reservation.
             */
            Reservation existingReservation =
                    reservationRepository
                            .findById(existing.getReservationId())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Original reservation could not be loaded"
                                    )
                            );

            return new ReservationResponse(
                    existingReservation.getId(),
                    existingReservation.getShow().getId(),
                    existingReservation.getUserId(),
                    existingReservation.getSeats()
                            .stream()
                            .map(Seat::getSeatCode)
                            .toList(),
                    existingReservation.getAmountPaise(),
                    existingReservation.getStatus().name()
            );
        }

        /*
         * STEP 5
         * Existing reservation flow starts here.
         */
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));

        List<String> seatCodes = request.seats()
                .stream()
                .distinct()
                .sorted()
                .toList();

        /*
         * Lock requested seats in deterministic order.
         */
        List<Seat> seats = seatRepository.findSeatsForUpdate(
                showId,
                seatCodes
        );

        if (seats.size() != seatCodes.size()) {
            throw new IllegalArgumentException(
                    "One or more seats do not exist"
            );
        }

        /*
         * Check whether any requested seat is already taken.
         */
        boolean anyTaken = seats.stream()
                .anyMatch(seat ->
                        seat.getStatus() != SeatStatus.AVAILABLE
                );

        if (anyTaken) {
            throw new SeatTakenException(
                    "One or more seats are already taken"
            );
        }

        /*
         * Create the user/show booking row if it doesn't exist.
         */
        userShowBookingRepository.createIfAbsent(
                showId,
                userId
        );

        /*
         * Lock the user/show booking row.
         */
        UserShowBooking userShowBooking =
                userShowBookingRepository.findForUpdate(
                                showId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "User booking state could not be loaded"
                                )
                        );

        int requestedSeatCount = seats.size();

        /*
         * Enforce per-user booking limit.
         */
        if (userShowBooking.getBookingCount()
                + requestedSeatCount
                > show.getPerUserLimit()) {

            throw new UserLimitExceededException(
                    "User booking limit is "
                            + show.getPerUserLimit()
            );
        }

        /*
         * Calculate amount using integer paise.
         */
        long amountPaise =
                show.getPricePaise() * seats.size();

        /*
         * Create reservation.
         */
        Reservation reservation = new Reservation(
                show,
                userId,
                amountPaise,
                seats
        );

        /*
         * Confirm seats.
         */
        seats.forEach(Seat::confirm);

        /*
         * Persist reservation.
         */
        reservationRepository.save(reservation);

        /*
         * Associate the idempotency key with the
         * newly created reservation.
         */
        IdempotencyKey idempotencyRecord =
                idempotencyKeyRepository
                        .findByUserIdAndIdempotencyKey(
                                userId,
                                idempotencyKey
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Idempotency record could not be loaded"
                                )
                        );

        idempotencyRecord.setReservationId(
                reservation.getId()
        );

        /*
         * Update user's booking count.
         */
        userShowBooking.addSeats(
                requestedSeatCount
        );

        /*
         * Return confirmed reservation.
         */
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