package com.rishab.seat_reservation_service.repository;

import com.rishab.seat_reservation_service.entity.UserShowBooking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserShowBookingRepository
        extends JpaRepository<UserShowBooking, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b
        FROM UserShowBooking b
        WHERE b.show.id = :showId
        AND b.userId = :userId
        """)
    Optional<UserShowBooking> findForUpdate(
            @Param("showId") Long showId,
            @Param("userId") String userId
    );

    @Modifying
    @Query(value = """
    INSERT INTO user_show_bookings (show_id, user_id, booking_count)
    VALUES (:showId, :userId, 0)
    ON CONFLICT (show_id, user_id) DO NOTHING
    """, nativeQuery = true)
    void createIfAbsent(
            @Param("showId") Long showId,
            @Param("userId") String userId
    );
}