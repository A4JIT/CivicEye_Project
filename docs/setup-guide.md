# CivicEye AI - Complete Setup & Execution Guide (Phases 1 to 4)

This guide provides end-to-end instructions for running the complete **CivicEye AI** system from Phase 1 through Phase 4.

---

## 📋 Architecture & Tech Stack

| Layer | Technologies | Local URL / Port |
|---|---|---|
| **Frontend** | React 18, Vite, React Router 6, Axios, Lucide Icons, Vitest | `http://localhost:5173` |
| **Backend** | Java 17/25, Spring Boot 3.2.3, Spring Security, Spring Data JPA, JWT (jjwt 0.12.5) | `http://localhost:8080` |
| **Database** | PostgreSQL 14+ / H2 In-Memory (Test) | `localhost:5432` (`civiceye`) |
| **Storage** | Local Multipart Storage Service | `uploads/` directory |

---

## ⚡ Prerequisites

1. **Java Development Kit**: JDK 17 or higher (configured at `C:\Editor\jbr` or in system `PATH`).
2. **Node.js**: v18.0+ and `npm`.
3. **PostgreSQL** or **Docker**: PostgreSQL running locally on port `5432` or Docker Desktop.

---

## 🗄️ Step 1: Database Setup

### Option A: Using Docker (Fastest)
From the project root directory:
```bash
docker-compose up -d
```
*This starts a PostgreSQL 15 container on port 5432 and automatically executes all migrations (`V1` to `V6`) and seed data.*

### Option B: Using Local PostgreSQL (`psql`)
1. Create the database:
   ```sql
   CREATE DATABASE civiceye;
   CREATE USER postgres WITH ENCRYPTED PASSWORD 'postgres';
   GRANT ALL PRIVILEGES ON DATABASE civiceye TO postgres;
   ```
2. Run migrations in order:
   ```bash
   psql -U postgres -d civiceye -f database/migrations/V1__create_users.sql
   psql -U postgres -d civiceye -f database/migrations/V2__create_departments.sql
   psql -U postgres -d civiceye -f database/migrations/V3__create_complaints.sql
   psql -U postgres -d civiceye -f database/migrations/V4__create_assignments.sql
   psql -U postgres -d civiceye -f database/migrations/V5__create_status_history.sql
   psql -U postgres -d civiceye -f database/migrations/V6__create_feedback.sql
   ```
3. Load development seed data:
   ```bash
   psql -U postgres -d civiceye -f database/seed/development_data.sql
   ```

---

## 🚀 Step 2: Start the Backend (Spring Boot API)

### Option A: Using Helper Script (Windows)
Double-click `scripts\start-backend.bat` or run:
```powershell
.\scripts\start-backend.bat
```

### Option B: Terminal Command
Open a terminal in `backend/`:
```powershell
$env:JAVA_HOME = "C:\Editor\jbr"
$env:PATH = "C:\Editor\jbr\bin;$env:PATH"
.\mvnw.cmd spring-boot:run
```
*The REST API will start on **`http://localhost:8080`**.*

---

## 🎨 Step 3: Start the Frontend (React + Vite)

### Option A: Using Helper Script (Windows)
Double-click `scripts\start-frontend.bat` or run:
```powershell
.\scripts\start-frontend.bat
```

### Option B: Terminal Command
Open a terminal in `frontend/`:
```bash
npm install
npm run dev
```
*The frontend application will start on **`http://localhost:5173`**.*

---

## 🔑 Default Test Accounts

| Role | Email | Password | Access & Features |
|---|---|---|---|
| **Citizen** | `citizen@civiceye.com` | `Password@123` | File complaints with AI assist, view personal complaints, track status with progress stepper & timeline |
| **Department Officer** | `dept_roads@civiceye.com` | `Password@123` | Department dashboard, assign officer, transition status (In Progress, Resolved, Rejected), upload resolution proof |
| **Administrator** | `admin@civiceye.com` | `Password@123` | System-wide dashboard, global KPI metrics, department routing & assignments |

---

## 🧭 Step 4: End-to-End Walkthrough of Phase 1 - 4 Features

### 1. Citizen Issue Filing (Phases 1–3)
- Navigate to `http://localhost:5173/login` and log in as `citizen@civiceye.com`.
- Click **"Report Issue"** (`/report`).
- Enter issue details (e.g., Title: *"Severe Pothole on Sector 4 road"*).
- Observe the **AI Assistant** instantly suggesting category (*Roads & Footpaths*) and priority (*High*).
- Optionally attach a photo evidence file and click **"Submit Grievance"**.

### 2. Public Complaint Tracking & Audit Stepper (Phase 4)
- Navigate to **"Track Complaint"** (`/track`).
- Click any quick chip or enter a tracking ID:
  - `CE-2026-0001` (In Progress)
  - `CE-2026-0004` (Resolved)
  - `CE-2026-0005` (Rejected)
- Observe the **4-stage visual stepper**, department badge, priority pill, and **Chronological Audit Timeline** with change timestamps and remarks.

### 3. Department Resolution & Assignment (Phase 4)
- Log out and log in as **`dept_roads@civiceye.com`**.
- Navigate to **"Department Issues"** (`/department/complaints`).
- Select an assigned issue and click **"Update Status / Resolve"**.
- Change status to **IN_PROGRESS** or **RESOLVED**, add resolution remarks, and optionally attach resolution proof.
- Verify that status history audit records are immediately reflected in the tracking timeline.

---

## 🧪 Step 5: Running Automated Tests

### Backend Tests (79 Tests)
```powershell
cd backend
$env:JAVA_HOME = "C:\Editor\jbr"
.\mvnw.cmd test
```
*Runs all 79 tests covering authentication, JWT filters, AI rule engine, complaint CRUD, lifecycle state machine, status history audit, and feedback repository.*

### Frontend Tests (35 Tests)
```bash
cd frontend
npm test -- --run
```
*Runs all 35 Vitest tests covering API clients, AuthContext, Login, Register, Dashboard, CreateComplaint, and ComplaintTracking components.*
