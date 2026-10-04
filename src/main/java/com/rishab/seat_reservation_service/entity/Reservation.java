package com.rishab.seat_reservation_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "reservation_seats",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    private List<Seat> seats = new ArrayList<>();

    protected Reservation() {
    }

    public Reservation(
            Show show,
            String userId,
            long amountPaise,
            List<Seat> seats
    ) {
        this.show = show;
        this.userId = userId;
        this.amountPaise = amountPaise;
        this.seats = new ArrayList<>(seats);
        this.status = ReservationStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Show getShow() {
        return show;
    }

    public String getUserId() {
        return userId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public long getAmountPaise() {
        return amountPaise;
    }

    public List<Seat> getSeats() {
        return seats;
    }
}