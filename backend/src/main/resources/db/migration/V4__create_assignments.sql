-- =====================================================
-- CivicEye AI - Database Migration V4
-- Description: Creates the 'complaint_assignments' table
-- =====================================================

CREATE TABLE IF NOT EXISTS complaint_assignments (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    department_id BIGINT NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    assigned_to_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    assigned_by_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    instructions TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_assignments_complaint ON complaint_assignments(complaint_id);
CREATE INDEX IF NOT EXISTS idx_assignments_department ON complaint_assignments(department_id);
CREATE INDEX IF NOT EXISTS idx_assignments_assigned_to ON complaint_assignments(assigned_to_id);

COMMENT ON TABLE complaint_assignments IS 'History and tracking of department and officer assignments for complaints';
