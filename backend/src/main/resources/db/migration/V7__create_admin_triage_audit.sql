-- =====================================================
-- CivicEye AI - Database Migration V7
-- Description: Adds admin triage fields and query indexing to 'complaints'
-- =====================================================

ALTER TABLE complaints
    ADD COLUMN IF NOT EXISTS admin_notes TEXT,
    ADD COLUMN IF NOT EXISTS needs_info_remarks TEXT,
    ADD COLUMN IF NOT EXISTS ai_category_overridden BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS ai_priority_overridden BOOLEAN DEFAULT FALSE;

-- High-performance compound indexes for Admin triage queue filtering
CREATE INDEX IF NOT EXISTS idx_complaints_status_created ON complaints(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_status_dept ON complaints(status, department_id);

COMMENT ON COLUMN complaints.admin_notes IS 'Internal administrative triage notes';
COMMENT ON COLUMN complaints.needs_info_remarks IS 'Latest information request message sent to citizen';
COMMENT ON COLUMN complaints.ai_category_overridden IS 'Indicates whether the administrator modified the AI recommended category';
COMMENT ON COLUMN complaints.ai_priority_overridden IS 'Indicates whether the administrator modified the AI recommended priority';
