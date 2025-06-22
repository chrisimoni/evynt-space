CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    description TEXT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_image_url VARCHAR(255) NULL,
    number_of_slots INT4 NOT NULL DEFAULT 0,
    start_date TIMESTAMPTZ(6) NOT NULL,
    end_date TIMESTAMPTZ(6) NOT NULL,
    meeting_link VARCHAR(255) NULL,
    online_platform VARCHAR(255) NULL,
    address VARCHAR(255) NULL,
    city VARCHAR(255) NULL,
    country VARCHAR(255) NULL,
    state VARCHAR(255) NULL,
    venue_name VARCHAR(255) NULL,
    price NUMERIC(38, 2) NULL,
    published_date TIMESTAMPTZ(6) NULL,
    scheduled_publish_date TIMESTAMPTZ(6) NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL,
    organizer_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE, -- New events are active by default
    deactivated_at TIMESTAMPTZ(6) NULL,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW()
);

-- events foreign keys
ALTER TABLE events
    ADD CONSTRAINT fk_events_organizer_id FOREIGN KEY (organizer_id) REFERENCES users(id);

-- Optional: Add an index on slug for faster lookups based on URL
CREATE UNIQUE INDEX IF NOT EXISTS idx_events_slug ON public.events (slug);