-- =====================================================
-- CivicEye AI - Database Migration V1
-- Description: Creates the 'users' table and constraints
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_users_role CHECK (role IN ('CITIZEN', 'ADMIN', 'DEPARTMENT_USER'))
);

-- Indexes for performance on lookups and role filtering
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

COMMENT ON TABLE users IS 'Stores system users across all roles: CITIZEN, ADMIN, and DEPARTMENT_USER';
COMMENT ON COLUMN users.role IS 'Role of the user: CITIZEN, ADMIN, or DEPARTMENT_USER';
COMMENT ON COLUMN users.password IS 'BCrypt encrypted password hash';
