-- =====================================================
-- CivicEye AI - Database Migration V11
-- Description: Indexes, audit trail optimization, and composite keys for complaint history and administrative monitoring
-- =====================================================

-- 1. Optimize indexes on complaint_status_history for rapid timeline rendering & audit inspection
CREATE INDEX IF NOT EXISTS idx_status_history_complaint_created ON complaint_status_history(complaint_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_status_history_to_status ON complaint_status_history(to_status);
CREATE INDEX IF NOT EXISTS idx_status_history_changed_by_created ON complaint_status_history(changed_by_id, created_at DESC);

-- 2. Add composite indexes on complaints for high-performance multi-criteria administrative monitoring and filtering
CREATE INDEX IF NOT EXISTS idx_complaints_monitoring_dept_status ON complaints(department_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_monitoring_status_created ON complaints(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_monitoring_cat_created ON complaints(category, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_monitoring_priority_created ON complaints(priority, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_monitoring_created_at ON complaints(created_at DESC);

-- 3. Optimize indexes on complaint_assignments for active assignment lookups and history
CREATE INDEX IF NOT EXISTS idx_assignments_complaint_active ON complaint_assignments(complaint_id, is_active);
CREATE INDEX IF NOT EXISTS idx_assignments_dept_created ON complaint_assignments(department_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_assignments_assigned_to_active ON complaint_assignments(assigned_to_id, is_active);

-- 4. Audit comments
COMMENT ON INDEX idx_status_history_complaint_created IS 'Accelerates chronological audit timeline reconstruction for complaints';
COMMENT ON INDEX idx_complaints_monitoring_dept_status IS 'Optimizes administrative multi-criteria complaint filtering by department and lifecycle status';
