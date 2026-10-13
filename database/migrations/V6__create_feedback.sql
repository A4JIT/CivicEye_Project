-- =====================================================
-- CivicEye AI - Database Migration V6
-- Description: Creates the 'complaint_feedback' table
-- =====================================================

CREATE TABLE IF NOT EXISTS complaint_feedback (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL UNIQUE REFERENCES complaints(id) ON DELETE CASCADE,
    citizen_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL,
    comments TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_feedback_rating CHECK (rating >= 1 AND rating <= 5)
);

CREATE INDEX IF NOT EXISTS idx_feedback_complaint_id ON complaint_feedback(complaint_id);
CREATE INDEX IF NOT EXISTS idx_feedback_citizen_id ON complaint_feedback(citizen_id);
CREATE INDEX IF NOT EXISTS idx_feedback_rating ON complaint_feedback(rating);

COMMENT ON TABLE complaint_feedback IS 'Stores citizen satisfaction feedback, star ratings, and review comments for resolved complaints';
COMMENT ON COLUMN complaint_feedback.rating IS 'Citizen satisfaction rating on a 1-5 scale';
