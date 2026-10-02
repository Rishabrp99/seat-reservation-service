CREATE TABLE user_show_bookings (
                                    id BIGSERIAL PRIMARY KEY,
                                    show_id BIGINT NOT NULL,
                                    user_id VARCHAR(255) NOT NULL,
                                    booking_count INTEGER NOT NULL DEFAULT 0,

                                    CONSTRAINT fk_user_show_bookings_show
                                        FOREIGN KEY (show_id)
                                            REFERENCES shows(id),

                                    CONSTRAINT uk_user_show_bookings_show_user
                                        UNIQUE (show_id, user_id),

                                    CONSTRAINT chk_user_show_bookings_count_non_negative
                                        CHECK (booking_count >= 0)
);