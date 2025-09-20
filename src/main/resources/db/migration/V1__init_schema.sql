-- V1__init_schema.sql

-- =====================================
-- Grant privileges on public schema to the application user (auth_user)
-- This will run only if the role exists; otherwise it logs a notice and continues.
-- =====================================
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_roles
    WHERE rolname = 'auth_user'
  ) THEN
    RAISE NOTICE 'Role auth_user does not exist, skipping grants';
ELSE
    EXECUTE 'GRANT USAGE ON SCHEMA public TO auth_user';
EXECUTE 'GRANT CREATE ON SCHEMA public TO auth_user';
-- optionally make auth_user the owner of schema (uncomment if you want that)
-- EXECUTE 'ALTER SCHEMA public OWNER TO auth_user';
END IF;
END$$;

-- =====================================
-- Ensure pgcrypto extension (for gen_random_uuid)
-- NOTE: CREATE EXTENSION requires superuser privileges.
-- If you cannot run this as superuser, create the extension once manually as postgres:
--   CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- =====================================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pgcrypto') THEN
    EXECUTE 'CREATE EXTENSION IF NOT EXISTS pgcrypto';
END IF;
END$$;

-- =====================================
-- Create enum type if not exists
-- =====================================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_status') THEN
    EXECUTE 'CREATE TYPE user_status AS ENUM (''active'', ''locked'', ''disabled'')';
END IF;
END$$;

-- =====================================
-- Users table
-- =====================================
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE,
    phone TEXT UNIQUE,
    password_hash TEXT NOT NULL,
    name TEXT,
    status user_status NOT NULL DEFAULT 'active',
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- =====================================
-- Refresh tokens
-- =====================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL,
    device_info TEXT,
    ip_address TEXT,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT chk_expiry CHECK (expires_at > issued_at)
    );

CREATE INDEX IF NOT EXISTS ix_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS ix_refresh_tokens_token_hash ON refresh_tokens(token_hash);

-- =====================================
-- Audit logs
-- =====================================
CREATE TABLE IF NOT EXISTS audit_logs (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    event_type TEXT NOT NULL,
    event_ts TIMESTAMPTZ NOT NULL DEFAULT now(),
    ip_address TEXT,
    user_agent TEXT,
    metadata JSONB
    );

CREATE INDEX IF NOT EXISTS ix_audit_user_event ON audit_logs(user_id, event_type, event_ts DESC);
