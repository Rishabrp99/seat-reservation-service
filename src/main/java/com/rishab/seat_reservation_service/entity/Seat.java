package com.rishab.seat_reservation_service.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seats_show_seat_code",
                        columnNames = {"show_id", "seat_code"}
                )
        }
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(name = "seat_code", nullable = false)
    private String seatCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    protected Seat() {
    }

    public Seat(Show show, String seatCode) {
        this.show = show;
        this.seatCode = seatCode;
        this.status = SeatStatus.AVAILABLE;
    }
    public void release() {
        this.status = SeatStatus.AVAILABLE;
    }
    public Long getId() {
        return id;
    }

    public Show getShow() {
        return show;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void confirm() {
        this.status = SeatStatus.CONFIRMED;
    }


}