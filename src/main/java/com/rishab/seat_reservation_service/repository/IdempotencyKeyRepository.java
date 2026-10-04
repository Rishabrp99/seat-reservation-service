package com.rishab.seat_reservation_service.repository;

import com.rishab.seat_reservation_service.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface IdempotencyKeyRepository
        extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByUserIdAndIdempotencyKey(
            String userId,
            String idempotencyKey
    );
    @Modifying
    @Query(value = """

            INSERT INTO idempotency_keys
            (user_id, show_id, idempotency_key, request_hash, created_at)
        VALUES
            (:userId, :showId, :idempotencyKey, :requestHash, CURRENT_TIMESTAMP)
        ON CONFLICT (user_id, idempotency_key)
        DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(
            @Param("userId") String userId,
            @Param("showId") Long showId,
            @Param("idempotencyKey") String idempotencyKey,
            @Param("requestHash") String requestHash
    );
}