# CivicEye AI - Database Migrations & Seed Data

This directory contains database schema migration scripts and development seed data for PostgreSQL.

## Directory Structure

```
database/
├── migrations/
│   ├── V1__create_users.sql         # Users table schema & role constraint
│   ├── V2__create_departments.sql   # Department management table
│   ├── V3__create_complaints.sql    # Complaints core entity table
│   ├── V4__create_assignments.sql   # Department officer assignments
│   ├── V5__create_status_history.sql# Complaint audit trail / history
│   └── V6__create_feedback.sql      # Citizen rating and resolution feedback
└── seed/
    ├── development_data.sql         # Test users (Admin, Citizen, Department Officer)
    └── departments.sql              # Standard civic departments seed
```

## Running Migrations Locally

### Option 1: Using `psql` command line
```bash
# Connect to your local PostgreSQL instance and run migrations in order:
psql -U postgres -d civiceye -f database/migrations/V1__create_users.sql
psql -U postgres -d civiceye -f database/migrations/V2__create_departments.sql
psql -U postgres -d civiceye -f database/migrations/V3__create_complaints.sql

# Load development seed data (users, departments, sample complaints)
psql -U postgres -d civiceye -f database/seed/development_data.sql
```

### Option 2: Using Docker Compose
When starting the PostgreSQL container via docker-compose:
```bash
docker-compose up -d
```

## Migration Catalog

| Migration | Description | Status |
|-----------|-------------|--------|
| `V1__create_users.sql` | Users table schema, roles constraint, indexes | Phase 1 (Complete) |
| `V2__create_departments.sql` | Municipal departments table with code, contact, indexes | Phase 2 (Complete) |
| `V3__create_complaints.sql` | Core complaint filing schema, geo-coordinates, status & priority constraints | Phase 2 (Complete) |
| `V4__create_assignments.sql` | Officer assignment & workflow dispatch | Phase 4 (Complete) |
| `V5__create_status_history.sql`| Complaint audit history & lifecycle transitions | Phase 4 (Complete) |
| `V6__create_feedback.sql` | Citizen satisfaction ratings & resolution feedback | Phase 4 (Complete) |
| `V7__create_admin_triage_audit.sql` | Admin triage notes, needs-info, AI overrides & queue indexes | Phase 5 (Complete) |
| `V8__create_department_resolution_schema.sql` | Department resolution workflow, progress notes & performance indexes | Phase 6 (Complete) |
| `V9__create_ai_recommendation_schema.sql` | AI recommendation metadata, confidence scoring & override audit trail | Phase 7 (Complete) |

## Default Test User Credentials

| Role | Username | Email | Password |
|------|----------|-------|----------|
| **ADMIN** | `admin` | `admin@civiceye.com` | `Password@123` |
| **CITIZEN** | `citizen_jane` | `citizen@civiceye.com` | `Password@123` |
| **DEPARTMENT_USER** | `officer_sharma` | `dept_roads@civiceye.com` | `Password@123` |

All passwords in the seed script are encrypted using **BCrypt** with 10 salt rounds.

