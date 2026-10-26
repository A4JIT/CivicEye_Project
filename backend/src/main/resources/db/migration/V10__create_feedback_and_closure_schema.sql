-- =====================================================
-- CivicEye AI - Database Migration V10
-- Description: Complaint closure metadata, citizen resolution verification, and feedback enhancement
-- =====================================================

-- 1. Enhance complaints table with closure and resolution verification attributes
ALTER TABLE complaints
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS closed_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS closure_remarks TEXT,
    ADD COLUMN IF NOT EXISTS citizen_confirmed_resolution BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Enhance complaint_feedback table with department association for direct department analytics
ALTER TABLE complaint_feedback
    ADD COLUMN IF NOT EXISTS department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL;

-- 3. Indexes for closure querying and department satisfaction aggregations
CREATE INDEX IF NOT EXISTS idx_complaints_closed_at ON complaints(closed_at);
CREATE INDEX IF NOT EXISTS idx_complaints_closed_by ON complaints(closed_by_user_id);
CREATE INDEX IF NOT EXISTS idx_feedback_department_id ON complaint_feedback(department_id);
CREATE INDEX IF NOT EXISTS idx_feedback_created_at ON complaint_feedback(created_at DESC);

-- 4. Column documentation
COMMENT ON COLUMN complaints.closed_at IS 'Timestamp when the complaint was formally closed after verification';
COMMENT ON COLUMN complaints.closed_by_user_id IS 'Admin user who closed the complaint, or null if citizen confirmed resolution';
COMMENT ON COLUMN complaints.closure_remarks IS 'Administrative notes or feedback summary recorded upon complaint closure';
COMMENT ON COLUMN complaints.citizen_confirmed_resolution IS 'True if the reporting citizen explicitly confirmed that the issue was resolved';
COMMENT ON COLUMN complaint_feedback.department_id IS 'Responsible department linked for direct performance metric evaluation';
