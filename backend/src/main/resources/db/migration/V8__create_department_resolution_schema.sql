-- =====================================================
-- CivicEye AI - Database Migration V8
-- Description: Department resolution workflow, progress tracking, and performance indexes
-- =====================================================

-- 1. Associate department users with their designated municipal department
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_users_department_id ON users(department_id);

-- 2. Enhance complaints table with field progress tracking attributes
ALTER TABLE complaints
    ADD COLUMN IF NOT EXISTS started_working_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS progress_notes TEXT;

-- 3. High-performance compound indexes for Department workload queues
CREATE INDEX IF NOT EXISTS idx_complaints_dept_priority_created ON complaints(department_id, priority, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_assigned_to_status ON complaints(assigned_to_id, status);
CREATE INDEX IF NOT EXISTS idx_complaints_dept_resolved_at ON complaints(department_id, resolved_at DESC);

-- Column documentation
COMMENT ON COLUMN users.department_id IS 'Foreign key linking department officers to their municipal department';
COMMENT ON COLUMN complaints.started_working_at IS 'Timestamp when the department officer moved complaint from ASSIGNED to IN_PROGRESS';
COMMENT ON COLUMN complaints.progress_notes IS 'Interim progress updates and field notes recorded by department personnel';
COMMENT ON COLUMN complaints.resolution_notes IS 'Mandatory completion description provided when complaint is marked RESOLVED';
COMMENT ON COLUMN complaints.resolution_image_url IS 'Relative path to verified resolution photo evidence stored in protected storage';
