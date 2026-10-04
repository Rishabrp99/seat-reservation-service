package com.rishab.seat_reservation_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "idempotency_keys",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_idempotency_user_key",
                columnNames = {"user_id", "idempotency_key"}
        )
)
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected IdempotencyKey() {
    }

    public IdempotencyKey(
            String userId,
            Long showId,
            String idempotencyKey,
            String requestHash) {

        this.userId = userId;
        this.showId = showId;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Long getShowId() {
        return showId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
}