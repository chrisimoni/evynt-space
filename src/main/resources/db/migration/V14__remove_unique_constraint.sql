ALTER TABLE transactions
DROP CONSTRAINT IF EXISTS transactions_payment_reference_id_key;

-- 2. Optional: add a regular index to maintain lookup performance
CREATE INDEX IF NOT EXISTS idx_transactions_payment_reference_id
    ON transactions (payment_reference_id);