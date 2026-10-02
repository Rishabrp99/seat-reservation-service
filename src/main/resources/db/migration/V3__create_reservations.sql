CREATE TABLE reservations (
                              id BIGSERIAL PRIMARY KEY,

                              show_id BIGINT NOT NULL,

                              user_id VARCHAR(255) NOT NULL,

                              status VARCHAR(20) NOT NULL,

                              amount_paise BIGINT NOT NULL,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_reservations_show
                                  FOREIGN KEY (show_id)
                                      REFERENCES shows(id),

                              CONSTRAINT chk_reservations_status
                                  CHECK (status IN ('CONFIRMED', 'CANCELLED')),

                              CONSTRAINT chk_reservations_amount_non_negative
                                  CHECK (amount_paise >= 0)
);