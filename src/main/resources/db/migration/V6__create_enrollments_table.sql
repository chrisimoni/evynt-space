CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    reservation_number VARCHAR(255) NOT NULL UNIQUE,
    payment_status VARCHAR(50),
    payment_reference VARCHAR(255),
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_enrollments_event_id FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE INDEX idx_event_user_email ON enrollments (event_id, email);