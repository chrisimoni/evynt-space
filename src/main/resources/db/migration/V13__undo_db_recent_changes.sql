-- Remove columns from events table
ALTER TABLE events
    DROP COLUMN IF EXISTS payout_status,
    DROP COLUMN IF EXISTS payout_amount,
    DROP COLUMN IF EXISTS payout_date;

-- Drop index on events table
DROP INDEX IF EXISTS idx_events_payout_schedule;

-- Remove added columns from transactions table
ALTER TABLE transactions
    DROP COLUMN IF EXISTS platform_fee,
    DROP COLUMN IF EXISTS organizer_payout_amount,
    DROP COLUMN IF EXISTS transfer_id;

-- Drop index from enrollments table
DROP INDEX IF EXISTS idx_enrollments_event_payment;