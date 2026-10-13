-- =====================================================
-- CivicEye AI - Development Seed Data
-- Description: Inserts test users and sample complaints for local testing
--
-- Default Test Credentials:
-- 1. Admin:
--    Username: admin
--    Email:    admin@civiceye.com
--    Password: Password@123
--    Role:     ADMIN
--
-- 2. Citizen:
--    Username: citizen_jane
--    Email:    citizen@civiceye.com
--    Password: Password@123
--    Role:     CITIZEN
--
-- 3. Department Officer:
--    Username: officer_sharma
--    Email:    dept_roads@civiceye.com
--    Password: Password@123
--    Role:     DEPARTMENT_USER
-- =====================================================

-- 1. Insert Users
INSERT INTO users (username, password, email, full_name, role, is_active, created_at, updated_at)
VALUES
    (
        'admin',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5VpI0L/w7U2l.Z7Oq9uUkm0E1N2/m',
        'admin@civiceye.com',
        'CivicEye Administrator',
        'ADMIN',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'citizen_jane',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5VpI0L/w7U2l.Z7Oq9uUkm0E1N2/m',
        'citizen@civiceye.com',
        'Jane Citizen',
        'CITIZEN',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'officer_sharma',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5VpI0L/w7U2l.Z7Oq9uUkm0E1N2/m',
        'dept_roads@civiceye.com',
        'Rajesh Sharma (PWD Officer)',
        'DEPARTMENT_USER',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (email) DO UPDATE 
SET 
    full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    updated_at = CURRENT_TIMESTAMP;

-- 2. Insert Standard Departments
INSERT INTO departments (name, code, description, contact_email, is_active)
VALUES
    ('Public Works & Roads', 'ROAD_WORKS', 'Responsible for potholes, damaged roads, flyovers, and sidewalks', 'roads@civiceye.gov.in', TRUE),
    ('Sanitation & Waste Management', 'SOLID_WASTE', 'Responsible for garbage accumulation, overflowing bins, and public cleanliness', 'sanitation@civiceye.gov.in', TRUE),
    ('Water Supply & Sewerage', 'WATER_SUPPLY', 'Responsible for pipe leaks, contaminated water, and open sewer lines', 'water@civiceye.gov.in', TRUE),
    ('Electricity & Street Lighting', 'ELECTRICITY', 'Responsible for non-functional streetlights and dangling power cables', 'electricity@civiceye.gov.in', TRUE),
    ('Public Health & Vector Control', 'HEALTH', 'Responsible for open stagnant water and disease vector hotspots', 'health@civiceye.gov.in', TRUE)
ON CONFLICT (code) DO NOTHING;

-- 3. Insert Sample Complaints across Various Statuses
INSERT INTO complaints (
    complaint_number, title, description, category, status, priority,
    location, latitude, longitude, citizen_id, department_id, assigned_to_id,
    rejection_reason, resolution_notes, created_at, updated_at
)
VALUES
    (
        'CE-2026-0001',
        'Deep Pothole on MG Road near City Hospital',
        'Large hazardous pothole measuring approximately 2 feet in diameter causing severe vehicular traffic jams and two-wheeler accidents.',
        'ROADS_AND_FOOTPATHS',
        'SUBMITTED',
        'HIGH',
        'MG Road, Block 4, Sector 12',
        28.6139,
        77.2090,
        (SELECT id FROM users WHERE email = 'citizen@civiceye.com'),
        NULL,
        NULL,
        NULL,
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        CURRENT_TIMESTAMP - INTERVAL '2 days'
    ),
    (
        'CE-2026-0002',
        'Broken Streetlights on Ring Road Flyover',
        'Continuous row of 5 solar streetlights not functioning since last Sunday, creating complete darkness at night.',
        'STREETLIGHTS_AND_ELECTRICITY',
        'ASSIGNED',
        'MEDIUM',
        'Ring Road Flyover, Pillar 42',
        28.6280,
        77.2180,
        (SELECT id FROM users WHERE email = 'citizen@civiceye.com'),
        (SELECT id FROM departments WHERE code = 'ELECTRICITY'),
        NULL,
        NULL,
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '4 days',
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    ),
    (
        'CE-2026-0003',
        'Road Repair Work on Nehru Marg',
        'Road asphalt peeled off after monsoon rains. PWD team has mobilized road patching crew.',
        'ROADS_AND_FOOTPATHS',
        'IN_PROGRESS',
        'HIGH',
        'Nehru Marg, Lane 2, South Ext.',
        28.5700,
        77.2200,
        (SELECT id FROM users WHERE email = 'citizen@civiceye.com'),
        (SELECT id FROM departments WHERE code = 'ROAD_WORKS'),
        (SELECT id FROM users WHERE email = 'dept_roads@civiceye.com'),
        NULL,
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        CURRENT_TIMESTAMP - INTERVAL '6 hours'
    ),
    (
        'CE-2026-0004',
        'Overflowing Garbage Container at Market Square',
        'Commercial waste container overflowing into public footpath emitting foul odor.',
        'GARBAGE_AND_CLEANLINESS',
        'RESOLVED',
        'MEDIUM',
        'Central Market Square, Gate 1',
        28.6300,
        77.2150,
        (SELECT id FROM users WHERE email = 'citizen@civiceye.com'),
        (SELECT id FROM departments WHERE code = 'SOLID_WASTE'),
        NULL,
        NULL,
        'Sanitation compactor truck cleared the container and disinfected the area.',
        CURRENT_TIMESTAMP - INTERVAL '7 days',
        CURRENT_TIMESTAMP - INTERVAL '12 hours'
    ),
    (
        'CE-2026-0005',
        'Private Property Boundary Wall Paint Dispute',
        'Neighbor painted exterior boundary wall without mutual residential agreement.',
        'OTHER',
        'REJECTED',
        'LOW',
        'Flat 402, Green Avenue',
        28.6400,
        77.2300,
        (SELECT id FROM users WHERE email = 'citizen@civiceye.com'),
        NULL,
        (SELECT id FROM users WHERE email = 'admin@civiceye.com'),
        'Private civil dispute outside municipal civic authority jurisdiction.',
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '8 days',
        CURRENT_TIMESTAMP - INTERVAL '3 days'
    ),
    (
        'CE-2026-0006',
        'Severe Water Main Pipe Leakage',
        'High-pressure drinking water line cracked, flooding road and causing water supply disruption to 50 households.',
        'WATER_SUPPLY_AND_LEAKAGE',
        'IN_PROGRESS',
        'CRITICAL',
        'Shanti Path, Near Community Centre',
        28.6100,
        77.2000,
        (SELECT id FROM users WHERE email = 'citizen@civiceye.com'),
        (SELECT id FROM departments WHERE code = 'WATER_SUPPLY'),
        NULL,
        NULL,
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP - INTERVAL '2 hours'
    )
ON CONFLICT (complaint_number) DO NOTHING;

-- 4. Insert Sample Status History
INSERT INTO complaint_status_history (complaint_id, from_status, to_status, changed_by_id, remarks, created_at)
VALUES
    (
        (SELECT id FROM complaints WHERE complaint_number = 'CE-2026-0001'),
        NULL,
        'SUBMITTED',
        (SELECT id FROM users WHERE email = 'citizen@civiceye.com'),
        'Initial complaint registered via Citizen Portal.',
        CURRENT_TIMESTAMP - INTERVAL '3 days'
    ),
    (
        (SELECT id FROM complaints WHERE complaint_number = 'CE-2026-0001'),
        'SUBMITTED',
        'ASSIGNED',
        (SELECT id FROM users WHERE email = 'admin@civiceye.com'),
        'Assigned to Roads & Traffic Department for inspection.',
        CURRENT_TIMESTAMP - INTERVAL '2 days'
    ),
    (
        (SELECT id FROM complaints WHERE complaint_number = 'CE-2026-0001'),
        'ASSIGNED',
        'IN_PROGRESS',
        (SELECT id FROM users WHERE email = 'dept_roads@civiceye.com'),
        'PWD maintenance squad dispatched on site with asphalt mixer.',
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    ),
    (
        (SELECT id FROM complaints WHERE complaint_number = 'CE-2026-0004'),
        NULL,
        'SUBMITTED',
        (SELECT id FROM users WHERE email = 'citizen@civiceye.com'),
        'Complaint registered.',
        CURRENT_TIMESTAMP - INTERVAL '7 days'
    ),
    (
        (SELECT id FROM complaints WHERE complaint_number = 'CE-2026-0004'),
        'SUBMITTED',
        'RESOLVED',
        (SELECT id FROM users WHERE email = 'dept_roads@civiceye.com'),
        'Streetlight ballast and sodium bulb replaced. Operational.',
        CURRENT_TIMESTAMP - INTERVAL '12 hours'
    );

-- 5. Insert Sample Citizen Feedback for Resolved Complaints
INSERT INTO complaint_feedback (complaint_id, citizen_id, rating, comments, created_at)
VALUES
    (
        (SELECT id FROM complaints WHERE complaint_number = 'CE-2026-0004'),
        (SELECT id FROM users WHERE email = 'citizen@civiceye.com'),
        5,
        'Prompt and efficient resolution! The waste container was cleared and sanitised within hours of reporting.',
        CURRENT_TIMESTAMP - INTERVAL '6 hours'
    )
ON CONFLICT (complaint_id) DO NOTHING;
