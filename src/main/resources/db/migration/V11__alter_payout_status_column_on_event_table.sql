ALTER TABLE events
    ALTER COLUMN payout_status DROP NOT NULL,
    ALTER COLUMN payout_status DROP DEFAULT;