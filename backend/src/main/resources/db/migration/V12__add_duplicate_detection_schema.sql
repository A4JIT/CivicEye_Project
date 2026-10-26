-- =====================================================
-- CivicEye AI - Database Migration V12
-- Description: Duplicate complaint detection, parent linking, and citizen upvotes/endorsements
-- =====================================================

-- 1. Enhance complaints table with duplicate tracking and citizen upvotes
ALTER TABLE complaints
    ADD COLUMN IF NOT EXISTS duplicate_of_id BIGINT REFERENCES complaints(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS duplicate_score DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS upvotes_count INTEGER NOT NULL DEFAULT 1;

-- 2. Create complaint_upvotes table for tracking citizen endorsements
CREATE TABLE IF NOT EXISTS complaint_upvotes (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    citizen_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_complaint_citizen_upvote UNIQUE (complaint_id, citizen_id)
);

-- 3. Indexes for high-performance spatial/duplicate queries and upvote tracking
CREATE INDEX IF NOT EXISTS idx_complaints_duplicate_of ON complaints(duplicate_of_id);
CREATE INDEX IF NOT EXISTS idx_complaints_upvotes ON complaints(upvotes_count DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_geo_active ON complaints(category, status, latitude, longitude)
    WHERE status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED');
CREATE INDEX IF NOT EXISTS idx_complaint_upvotes_complaint ON complaint_upvotes(complaint_id);
CREATE INDEX IF NOT EXISTS idx_complaint_upvotes_citizen ON complaint_upvotes(citizen_id);

-- 4. Documentation
COMMENT ON COLUMN complaints.duplicate_of_id IS 'Points to the primary parent complaint if this ticket was identified or merged as a duplicate';
COMMENT ON COLUMN complaints.duplicate_score IS 'Calculated AI similarity score (0.00 to 1.00) between this complaint and the parent complaint';
COMMENT ON COLUMN complaints.upvotes_count IS 'Total count of citizens who have upvoted/endorsed this grievance';
COMMENT ON TABLE complaint_upvotes IS 'Audit log of citizens who upvoted an existing issue instead of creating duplicate tickets';
