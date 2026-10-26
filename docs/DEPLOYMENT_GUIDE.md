# CivicEye AI - Cloud Deployment Guide

This guide walks you through deploying **CivicEye AI** to production using the cloud setup:
- **Database:** Supabase or Neon (Free, managed Cloud PostgreSQL)
- **Backend:** Render or Railway (Spring Boot 3 + Java 17 Container)
- **Frontend:** Vercel (React + Vite + Tailwind/CSS, Global CDN)

---

## 🏗️ Architecture Overview

```mermaid
graph TD
    Client["Citizen / Admin / Officer Browser"]
    Vercel["Frontend on Vercel (SPA)<br/>https://civiceye-ai.vercel.app"]
    Backend["Backend on Render / Railway<br/>https://civiceye-backend.onrender.com"]
    Database["Cloud PostgreSQL (Supabase / Neon)"]
    Storage["Persistent File Storage (/app/uploads)"]

    Client -->|HTTPS / UI| Vercel
    Vercel -->|REST API Requests with JWT| Backend
    Backend -->|JDBC connection| Database
    Backend -->|Store Uploads| Storage
```

---

## Step 1: Set Up Cloud PostgreSQL (Neon or Supabase)

### Option A: Neon (Fastest & Simplest - 2 Minutes)
1. Go to [https://neon.tech](https://neon.tech) and create a free account.
2. Click **Create Project**, name it `civiceye`, and select your closest region.
3. Neon will present your **Connection string**. It looks like:
   ```text
   postgresql://civiceye_owner:AbCd1234@ep-cool-flower-12345.us-east-2.aws.neon.tech/civiceye?sslmode=require
   ```
4. Copy this connection string.

### Option B: Supabase
1. Go to [https://supabase.com](https://supabase.com) and create a new project `civiceye`.
2. Set a database password and wait ~1 minute for provisioning.
3. Go to **Project Settings** -> **Database** -> **Connection string** (URI).
4. Copy the connection string (with your password replaced).

> [!NOTE]
> CivicEye AI backend automatically creates all required database tables and seeds initial test users (`admin@civiceye.com`, `citizen@civiceye.com`, `dept_roads@civiceye.com`) on its very first start! No manual SQL import is necessary.

---

## Step 2: Deploy Backend to Render (or Railway)

### Using Render:
1. Push your repository changes to GitHub:
   ```bash
   git add .
   git commit -m "feat: deployment readiness configuration"
   git push origin dev
   ```
2. Go to [Render Dashboard](https://dashboard.render.com).
3. Click **New +** -> **Web Service**.
4. Connect your GitHub repository (`CivicEye_Project`).
5. Configure the service settings:
   - **Name:** `civiceye-backend`
   - **Branch:** `dev` (or `main`)
   - **Root Directory:** `backend`
   - **Runtime:** `Docker` (Render will automatically detect `backend/Dockerfile`)
   - **Instance Type:** `Free`
6. Scroll down to **Environment Variables** and add the following:

| Key | Value | Description |
| :--- | :--- | :--- |
| `DATABASE_URL` | `<Your connection string from Step 1>` | Cloud PostgreSQL connection string |
| `JWT_SECRET` | `<32+ char random secret key>` | Used to sign JWT authentication tokens |
| `ADMIN_SECRET_CODE` | `CIVICEYE-ADMIN-2026-SECURE` | Secret required for admin registration |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,https://*.vercel.app` | Allow your Vercel frontend |
| `UPLOAD_DIR` | `uploads` | Local upload directory path |

7. Click **Create Web Service**.
8. Render will build the container (~2-3 minutes) and output logs.
9. Verify health once deployed:
   Visit: `https://<your-service-name>.onrender.com/api/health`
   Expected response:
   ```json
   {
     "status": "UP",
     "service": "CivicEye AI Backend",
     "timestamp": "2026-10-26T...",
     "environment": "production-ready"
   }
   ```
10. Copy your backend URL: `https://<your-service-name>.onrender.com`.

---

## Step 3: Deploy Frontend to Vercel

1. Go to [Vercel](https://vercel.com) and click **Add New...** -> **Project**.
2. Import your GitHub repository (`CivicEye_Project`).
3. In the project setup screen:
   - **Framework Preset:** `Vite`
   - **Root Directory:** Click **Edit** and choose `frontend`.
   - **Build Command:** `npm run build`
   - **Output Directory:** `dist`
4. Expand **Environment Variables** and add:

| Key | Value |
| :--- | :--- |
| `VITE_API_URL` | `https://<your-backend-service>.onrender.com` |

5. Click **Deploy**.
6. Vercel will build and deploy your frontend in ~30 seconds and give you a public URL (e.g. `https://civiceye-ai.vercel.app`).
7. Open the Vercel link and test:
   - User registration & login
   - Citizen complaint creation
   - Admin complaint triage & assignment
   - Department officer resolution flow

---

## Step 4 (Optional): Local / VPS Full-Stack Docker Deployment

If you want to run the entire stack on any server or VPS with Docker:
```bash
# 1. Start all 3 containers (Database + Backend + Frontend)
docker compose -f docker-compose.prod.yml up -d --build

# 2. Check container status
docker compose -f docker-compose.prod.yml ps

# 3. Access applications:
# Frontend: http://localhost:80
# Backend:  http://localhost:8080/api/health
```

---

## 🔐 Default Credentials After First Boot

When the backend runs for the first time, `DatabaseSeeder` automatically initializes:

- **Admin Account:** `admin@civiceye.com` / `Password@123`
- **Citizen Account:** `citizen@civiceye.com` / `Password@123`
- **Department Officer:** `dept_roads@civiceye.com` / `Password@123`

---

## ✅ Deployment Checklist

- [x] Backend Dockerfile (Multi-stage Eclipse Temurin 17 JRE)
- [x] Cloud Database URI Parser in `DatabaseConfig.java` (supports `postgres://`, `postgresql://`, and standard JDBC)
- [x] Permissive Cloud CORS configuration with wildcard pattern support (`https://*.vercel.app`)
- [x] Public `/api/health` monitoring endpoint
- [x] Frontend `vercel.json` SPA rewrite rules
- [x] Full-stack `docker-compose.prod.yml`
