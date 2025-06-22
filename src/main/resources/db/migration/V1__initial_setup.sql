-- Verification Codes Table
CREATE TABLE IF NOT EXISTS verification_codes (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   code VARCHAR(6) NOT NULL,
   email VARCHAR(255) NOT NULL,
   is_used BOOLEAN DEFAULT FALSE NOT NULL,
   expiration_time TIMESTAMPTZ NOT NULL,
   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
   updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for Verification Codes table
-- Index (composite index) to optimize updates and simple lookups by email and usage status
CREATE INDEX idx_vc_email_is_used
    ON verification_codes (email, is_used);

-- Optional: Index to optimize finding active verification codes, covering filtering and ordering (Optional)
-- CREATE INDEX idx_vc_email_is_used_expiration_created
--     ON verification_codes (email, is_used, expiration_time, created_at DESC);


-- ======================================================================
-- Verified sessions Table
CREATE TABLE IF NOT EXISTS verified_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    expiration_time TIMESTAMPTZ NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);