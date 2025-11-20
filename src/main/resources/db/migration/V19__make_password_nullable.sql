-- Make password column nullable to support OAuth2 users who don't have passwords
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;
