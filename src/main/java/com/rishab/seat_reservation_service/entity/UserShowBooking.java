package com.rishab.seat_reservation_service.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "user_show_bookings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_show_bookings_show_user",
                        columnNames = {"show_id", "user_id"}
                )
        }
)
public class UserShowBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "booking_count", nullable = false)
    private int bookingCount;

    protected UserShowBooking() {
    }

    public UserShowBooking(Show show, String userId) {
        this.show = show;
        this.userId = userId;
        this.bookingCount = 0;
    }

    public void removeSeats(int count) {
        this.bookingCount -= count;

        if (this.bookingCount < 0) {
            this.bookingCount = 0;
        }
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public void addSeats(int count) {
        this.bookingCount += count;
    }
}