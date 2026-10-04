package com.rishab.seat_reservation_service.repository;

import com.rishab.seat_reservation_service.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowIdOrderBySeatCode(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM Seat s
        WHERE s.show.id = :showId
        AND s.seatCode IN :seatCodes
        ORDER BY s.seatCode
        """)
    List<Seat> findSeats(
            @Param("showId") Long showId,
            @Param("seatCodes") List<String> seatCodes
    );


}