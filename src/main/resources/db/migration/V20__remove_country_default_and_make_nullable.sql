-- Remove default 'US' and make country_code nullable
-- Users must explicitly provide their country, especially for Stripe Connect compliance
ALTER TABLE users ALTER COLUMN country_code DROP DEFAULT;
ALTER TABLE users ALTER COLUMN country_code DROP NOT NULL;
