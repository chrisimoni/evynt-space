CREATE TABLE IF NOT EXISTS notification_outbox (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    last_attempt_time TIMESTAMPTZ(6) NULL,
    last_error VARCHAR(255) NULL,
    message_details JSONB NOT NULL,
    next_attempt_time TIMESTAMPTZ(6) NULL,
    notification_type VARCHAR(255) NOT NULL,
    retry_attempts INT4 NOT NULL DEFAULT 0,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),
    updated_at TIMESTamptz(6) NOT NULL DEFAULT NOW()
);