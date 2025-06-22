CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NULL,
    company VARCHAR(255) NULL,
    profile_image_url VARCHAR(255) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW()
);

-- Optional: Add an index on email if it's used very frequently for lookups beyond the UNIQUE constraint
-- The UNIQUE constraint itself implicitly creates an index, but sometimes a separate index
-- with different properties (e.g., B-tree vs hash) might be beneficial in specific scenarios.
-- For most cases, the index created by the UNIQUE constraint is sufficient for lookups.

-- The UNIQUE constraint itself implicitly creates an index, but sometimes a separate index
-- For most cases, the index created by the UNIQUE constraint is sufficient for lookups.
CREATE INDEX idx_users_email ON users (email);