-- =====================================================
-- CivicEye AI - Database Migration V9
-- Description: AI recommendation metadata schema, override audit tracking, and query indexes
-- =====================================================

CREATE TABLE IF NOT EXISTS ai_recommendations (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT UNIQUE NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    suggested_category VARCHAR(50) NOT NULL,
    suggested_priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    suggested_department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    confidence_score DOUBLE PRECISION NOT NULL,
    explanation TEXT,
    is_overridden BOOLEAN NOT NULL DEFAULT FALSE,
    overridden_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    overridden_at TIMESTAMP WITH TIME ZONE,
    override_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_ai_recommendations_priority CHECK (suggested_priority IN (
        'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    )),
    CONSTRAINT chk_ai_recommendations_confidence CHECK (
        confidence_score >= 0.0 AND confidence_score <= 1.0
    )
);

-- Compound and foreign key indexes for AI recommendation querying and admin triage
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_complaint_id ON ai_recommendations(complaint_id);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_dept_id ON ai_recommendations(suggested_department_id);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_overridden ON ai_recommendations(is_overridden);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_category ON ai_recommendations(suggested_category);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_dept_overridden ON ai_recommendations(suggested_department_id, is_overridden);

-- Column documentation
COMMENT ON TABLE ai_recommendations IS 'Stores NLP rule-based AI recommendations, confidence scores, and administrative overrides for complaints';
COMMENT ON COLUMN ai_recommendations.complaint_id IS 'Unique foreign key linking AI recommendation directly to the parent complaint';
COMMENT ON COLUMN ai_recommendations.suggested_category IS 'AI predicted civic category (e.g., ROADS_AND_FOOTPATHS, WATER_SUPPLY_AND_LEAKAGE)';
COMMENT ON COLUMN ai_recommendations.suggested_priority IS 'AI calculated priority level based on severity rule evaluation';
COMMENT ON COLUMN ai_recommendations.suggested_department_id IS 'Suggested municipal department responsible for addressing the complaint';
COMMENT ON COLUMN ai_recommendations.confidence_score IS 'Confidence rating of the AI recommendation between 0.0 and 1.0';
COMMENT ON COLUMN ai_recommendations.explanation IS 'Human-readable explanation of keyword signals and risk factors that influenced recommendation';
COMMENT ON COLUMN ai_recommendations.is_overridden IS 'Flag indicating whether a human triage administrator altered the AI recommendation';
COMMENT ON COLUMN ai_recommendations.overridden_by_user_id IS 'Admin user who executed the recommendation override';
COMMENT ON COLUMN ai_recommendations.overridden_at IS 'Timestamp when the recommendation was overridden';
COMMENT ON COLUMN ai_recommendations.override_reason IS 'Justification notes provided by the admin during override';
