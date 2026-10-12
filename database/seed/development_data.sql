-- =====================================================
-- CivicEye AI - Development Seed Data
-- Description: Inserts test users for local development and testing
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
