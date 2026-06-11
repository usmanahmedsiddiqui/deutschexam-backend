-- Refresh tokens are now stored as SHA-256 hex hashes instead of raw UUIDs (SEC-6).
-- Existing plaintext tokens are unusable after this change, so we clear the table.
-- All active users will need to re-authenticate once after this migration is applied.
TRUNCATE TABLE refresh_tokens;

-- SHA-256 hex output is always exactly 64 characters; tighten the column constraint.
ALTER TABLE refresh_tokens ALTER COLUMN token TYPE VARCHAR(64);
