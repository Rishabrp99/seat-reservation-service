package com.rishab.seat_reservation_service;

import com.rishab.seat_reservation_service.dto.ReservationResponse;
import com.rishab.seat_reservation_service.dto.ReserveSeatRequest;
import com.rishab.seat_reservation_service.entity.Seat;
import com.rishab.seat_reservation_service.entity.Show;
import com.rishab.seat_reservation_service.exception.SeatTakenException;
import com.rishab.seat_reservation_service.repository.SeatRepository;
import com.rishab.seat_reservation_service.repository.ShowRepository;
import com.rishab.seat_reservation_service.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.jpa.properties.hibernate.jdbc.time_zone=UTC",
        "spring.datasource.url=jdbc:postgresql://localhost:5433/seat_reservation_db?options=-c%20TimeZone%3DUTC"
})
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    private Long showId;

    private Long createTestShow() {

        Show show = new Show(
                "Concurrency Test Show",
                10000L,
                4
        );

        show = showRepository.save(show);

        Seat seat = new Seat(
                show,
                "A1"
        );

        seatRepository.save(seat);

        return show.getId();
    }

    @Test
    void hotSeatShouldAllowOnlyOneReservation() throws Exception {

        showId = createTestShow();

        int requestCount = 20;

        ExecutorService executorService =
                Executors.newFixedThreadPool(requestCount);

        CountDownLatch ready =
                new CountDownLatch(requestCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<ReservationResponse>> futures =
                new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {

            final String userId = "user-" + i;

            futures.add(
                    executorService.submit(() -> {

                        ready.countDown();

                        start.await();

                        return reservationService.reserve(
                                showId,
                                userId,
                                UUID.randomUUID().toString(),
                                new ReserveSeatRequest(
                                        List.of("A1")
                                )
                        );
                    })
            );
        }

        ready.await();

        start.countDown();

        int successCount = 0;
        int seatTakenCount = 0;

        for (Future<ReservationResponse> future : futures) {

            try {
                future.get();
                successCount++;

            } catch (ExecutionException e) {

                if (e.getCause() instanceof SeatTakenException) {
                    seatTakenCount++;
                } else {
                    throw e;
                }
            }
        }

        executorService.shutdown();

        assertEquals(1, successCount);
        assertEquals(requestCount - 1, seatTakenCount);
    }
}