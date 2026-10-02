package com.rishab.seat_reservation_service.repository;

import com.rishab.seat_reservation_service.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Long> {
}