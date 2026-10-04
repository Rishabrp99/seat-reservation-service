package com.rishab.seat_reservation_service.repository;

import com.rishab.seat_reservation_service.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository
        extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByUserIdAndIdempotencyKey(
            String userId,
            String idempotencyKey
    );
}