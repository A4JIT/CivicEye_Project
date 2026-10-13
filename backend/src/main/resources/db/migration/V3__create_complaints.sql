-- =====================================================
-- CivicEye AI - Database Migration V3
-- Description: Creates the 'complaints' table and indexes
-- =====================================================

CREATE TABLE IF NOT EXISTS complaints (
    id BIGSERIAL PRIMARY KEY,
    complaint_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    location VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    image_url VARCHAR(500),
    citizen_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    assigned_to_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    rejection_reason TEXT,
    resolution_notes TEXT,
    resolution_image_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_complaints_status CHECK (status IN (
        'SUBMITTED', 'UNDER_REVIEW', 'NEEDS_INFORMATION', 'ASSIGNED',
        'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'
    )),
    CONSTRAINT chk_complaints_priority CHECK (priority IN (
        'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    ))
);

-- Performance indexes for dashboard filtering and aggregations
CREATE INDEX IF NOT EXISTS idx_complaints_citizen_id ON complaints(citizen_id);
CREATE INDEX IF NOT EXISTS idx_complaints_department_id ON complaints(department_id);
CREATE INDEX IF NOT EXISTS idx_complaints_status ON complaints(status);
CREATE INDEX IF NOT EXISTS idx_complaints_category ON complaints(category);
CREATE INDEX IF NOT EXISTS idx_complaints_created_at ON complaints(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_citizen_status ON complaints(citizen_id, status);
CREATE INDEX IF NOT EXISTS idx_complaints_dept_status ON complaints(department_id, status);

COMMENT ON TABLE complaints IS 'Core entity storing civic issue complaints filed by citizens';
COMMENT ON COLUMN complaints.complaint_number IS 'Human-readable unique identifier (e.g., CE-2026-0001)';
COMMENT ON COLUMN complaints.status IS 'Current lifecycle status in state machine';
COMMENT ON COLUMN complaints.rejection_reason IS 'Mandatory explanation recorded when an administrator rejects a complaint';
