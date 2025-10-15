-- 1. DROP the existing index that includes payment_status
DROP INDEX IF EXISTS idx_enrollments_event_email_payment_status;

-- 2. CREATE the new, optimized index for event_id and email
CREATE INDEX idx_enrollments_event_email ON enrollments (event_id, email);