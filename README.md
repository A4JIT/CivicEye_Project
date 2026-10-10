# CivicEye AI

**CivicEye AI** is an AI-assisted civic issue reporting and management platform.

## 🚀 Key Features
* **Citizen Reporting:** Submit complaints with a title, description, category, location, and photo evidence.
* **AI Assistance:** Transparent rule-based recommendation system suggesting category and priority.
* **Workflow Management:** Complete lifecycle tracking.
* **Role-Based Access Control:** Citizens, Admins, and Department Users.

## 🛠️ Technology Stack
* **Frontend:** React, Vite, React Router, Axios
* **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA
* **Database:** PostgreSQL

## ⚙️ Local Development Setup

### Prerequisites
* **Node.js (v18+)**
* **Java Development Kit (JDK 17)**
* **Maven**
* **PostgreSQL (v14+)**

### Project Setup and Execution

1. **Database Setup:**
   Ensure PostgreSQL is running locally on port 5432.
   Start the database (if running via Docker, for example):
   ```bash
   docker-compose up -d
   ```
   Or create it manually using psql:
   ```sql
   CREATE DATABASE civiceye;
   CREATE USER postgres WITH ENCRYPTED PASSWORD 'postgres';
   GRANT ALL PRIVILEGES ON DATABASE civiceye TO postgres;
   ```

2. **Start the Backend:**
   Open a terminal in the `backend` directory.
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   The backend runs on `http://localhost:8080`.

3. **Start the Frontend:**
   Open a terminal in the `frontend` directory.
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The frontend runs on `http://localhost:3000`.

## 📁 Project Structure Overview

* `frontend/`: React + Vite SPA application
* `backend/`: Spring Boot Maven application
* `database/`: Database SQL migration and seed scripts
* `docs/`: System documentation (architecture, schema, workflows)
* `scripts/`: Helper scripts for startup and setup
* `uploads/`: Ignored directory for local file storage
