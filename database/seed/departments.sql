-- =====================================================
-- CivicEye AI - Department Seed Data
-- =====================================================

CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    description TEXT,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO departments (name, code, description, contact_email, is_active)
VALUES
    ('Public Works & Roads', 'ROAD_WORKS', 'Responsible for potholes, damaged roads, flyovers, and sidewalks', 'roads@civiceye.gov.in', TRUE),
    ('Sanitation & Waste Management', 'SOLID_WASTE', 'Responsible for garbage accumulation, overflowing bins, and public cleanliness', 'sanitation@civiceye.gov.in', TRUE),
    ('Water Supply & Sewerage', 'WATER_SUPPLY', 'Responsible for pipe leaks, contaminated water, and open sewer lines', 'water@civiceye.gov.in', TRUE),
    ('Electricity & Street Lighting', 'ELECTRICITY', 'Responsible for non-functional streetlights and dangling power cables', 'electricity@civiceye.gov.in', TRUE),
    ('Public Health & Vector Control', 'HEALTH', 'Responsible for open stagnant water and disease vector hotspots', 'health@civiceye.gov.in', TRUE)
ON CONFLICT (code) DO NOTHING;
