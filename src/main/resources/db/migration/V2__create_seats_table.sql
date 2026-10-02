CREATE TABLE seats (
                       id BIGSERIAL PRIMARY KEY,
                       show_id BIGINT NOT NULL,
                       seat_code VARCHAR(50) NOT NULL,
                       status VARCHAR(20) NOT NULL,

                       CONSTRAINT fk_seats_show
                           FOREIGN KEY (show_id)
                               REFERENCES shows(id),

                       CONSTRAINT uk_seats_show_seat_code
                           UNIQUE (show_id, seat_code),

                       CONSTRAINT chk_seats_status
                           CHECK (status IN ('AVAILABLE', 'HELD', 'CONFIRMED'))
);