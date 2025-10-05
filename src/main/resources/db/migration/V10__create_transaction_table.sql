-- adding the necessary payout tracking columns to event
ALTER TABLE events
    ADD COLUMN registration_close_date TIMESTAMPTZ(6) NULL,
    ADD COLUMN payout_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN payout_amount DECIMAL(10, 2) NULL,
    ADD COLUMN payout_date TIMESTAMPTZ(6) NULL;

-- OPTIONAL: Add a composite index for efficient lookup by the scheduled payout job.
CREATE INDEX idx_events_payout_schedule
    ON events (is_paid, payout_status, end_date);

-- CREATE transaction table
CREATE TABLE IF NOT EXISTS transactions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    payment_reference_id VARCHAR(255) NOT NULL UNIQUE,
    -- Financial Details
    amount DECIMAL(10, 2) NOT NULL, -- Gross amount paid by the customer
    currency VARCHAR(3) NOT NULL,    -- e.g., 'usd'
    -- Transaction Status
    status VARCHAR(50) NOT NULL,    -- e.g., 'SUCCEEDED', 'FAILED', 'CANCELED', 'REFUNDED'
    -- Audit and Management Fields
    platform_fee DECIMAL(10, 2) NULL, -- Your platform's fee amount retained
    organizer_payout_amount DECIMAL(10, 2) NULL, -- The net amount transferred
    transfer_id VARCHAR(255) NULL, -- Reference to the Transfer object
    created_at TIMESTAMPTZ(6) DEFAULT now() NOT NULL,
    updated_at TIMESTAMPTZ(6) DEFAULT now() NOT NULL
);


-- MODIFYING enrollments table
DROP INDEX IF EXISTS idx_enrollments_reservation_number; -- Keep the unique constraint, drop the separate index

-- Drop the old payment tracking columns
ALTER TABLE enrollments
    DROP COLUMN payment_reference;

ALTER TABLE enrollments
    ADD COLUMN transaction_id UUID NULL;

-- 4. Add Constraints

-- Unique Constraint: Ensures a single transaction can only be linked to one enrollment (1:1 relationship)
ALTER TABLE enrollments
    ADD CONSTRAINT uq_enrollment_transaction_id
        UNIQUE (transaction_id);

-- Foreign Key Constraint: Links to the 'transactions' table
ALTER TABLE enrollments
    ADD CONSTRAINT fk_enrollment_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES transactions(id)
            ON DELETE RESTRICT;


-- 5. Add the Optimized Index for Payout Joining
CREATE INDEX idx_enrollments_event_payment
    ON enrollments USING btree (event_id, transaction_id);