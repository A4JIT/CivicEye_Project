-- =====================================================
-- CivicEye AI - Database Migration V2
-- Description: Creates the 'departments' table and indexes
-- =====================================================

CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    description TEXT,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Performance and lookup indexes
CREATE INDEX IF NOT EXISTS idx_departments_code ON departments(code);
CREATE INDEX IF NOT EXISTS idx_departments_is_active ON departments(is_active);

COMMENT ON TABLE departments IS 'Municipal departments responsible for handling and resolving civic complaints';
COMMENT ON COLUMN departments.code IS 'Unique department identifier code (e.g., ROAD_WORKS, SOLID_WASTE)';
