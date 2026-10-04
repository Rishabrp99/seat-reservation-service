CREATE TABLE idempotency_keys (
                                  id BIGSERIAL PRIMARY KEY,

                                  user_id VARCHAR(255) NOT NULL,

                                  show_id BIGINT NOT NULL,

                                  idempotency_key VARCHAR(255) NOT NULL,

                                  request_hash VARCHAR(64) NOT NULL,

                                  reservation_id BIGINT,

                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT uk_idempotency_user_key
                                      UNIQUE (user_id, idempotency_key),

                                  CONSTRAINT fk_idempotency_show
                                      FOREIGN KEY (show_id)
                                          REFERENCES shows(id),

                                  CONSTRAINT fk_idempotency_reservation
                                      FOREIGN KEY (reservation_id)
                                          REFERENCES reservations(id)
);