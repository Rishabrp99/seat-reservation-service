package com.rishab.seat_reservation_service.repository;

import com.rishab.seat_reservation_service.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByShowIdOrderBySeatCode(Long showId);
}