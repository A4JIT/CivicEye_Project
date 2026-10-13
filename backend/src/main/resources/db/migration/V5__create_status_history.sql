-- =====================================================
-- CivicEye AI - Database Migration V5
-- Description: Creates the 'complaint_status_history' table
-- =====================================================

CREATE TABLE IF NOT EXISTS complaint_status_history (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    changed_by_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    remarks TEXT,
    resolution_image_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_history_to_status CHECK (to_status IN (
        'SUBMITTED', 'UNDER_REVIEW', 'NEEDS_INFORMATION', 'ASSIGNED',
        'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'
    ))
);

CREATE INDEX IF NOT EXISTS idx_status_history_complaint ON complaint_status_history(complaint_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_status_history_changed_by ON complaint_status_history(changed_by_id);

COMMENT ON TABLE complaint_status_history IS 'Chronological audit trail of all status transitions for complaints';
