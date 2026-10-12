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
# Connect to your local PostgreSQL instance
psql -U postgres -d civiceye -f database/migrations/V1__create_users.sql
psql -U postgres -d civiceye -f database/seed/development_data.sql
```

### Option 2: Using Docker Compose
When starting the PostgreSQL container via docker-compose:
```bash
docker-compose up -d
```

## Default Test User Credentials

| Role | Username | Email | Password |
|------|----------|-------|----------|
| **ADMIN** | `admin` | `admin@civiceye.com` | `Password@123` |
| **CITIZEN** | `citizen_jane` | `citizen@civiceye.com` | `Password@123` |
| **DEPARTMENT_USER** | `officer_sharma` | `dept_roads@civiceye.com` | `Password@123` |

All passwords in the seed script are encrypted using **BCrypt** with 10 salt rounds.
