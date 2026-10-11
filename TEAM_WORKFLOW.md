# CivicEye AI - Team Collaboration Guide

Since there are 3 developers on this project, effective collaboration is crucial. This guide covers how to divide the work, manage the repository, and keep your code integrated.

## 1. Version Control Setup (Git)

I have already initialized a local Git repository for you and committed the base structure. 
Next, one of you should create a central repository (on GitHub, GitLab, or Bitbucket) and push this code:

```bash
# Add your remote repository URL (replace with your actual URL)
git remote add origin https://github.com/your-username/civiceye-ai.git

# Push the main branch to the remote repository
git push -u origin master
```

The other two partners can then clone the project:
```bash
git clone https://github.com/your-username/civiceye-ai.git
```

*(Note: Ensure everyone has Node.js, JDK 17, Maven, and PostgreSQL installed as per the README).*

## 2. Division of Labor

A typical and highly effective way to split a full-stack project for 3 people is:

### Person 1: Frontend Lead (React/Vite)
* **Focus:** Everything inside the `frontend/` folder.
* **Responsibilities:**
  * Build the UI components (Forms, Buttons, Modals).
  * Set up routing for Citizen, Admin, and Department roles.
  * Connect to the backend REST APIs using Axios.
  * Manage CSS/styling for a polished user experience.

### Person 2: Backend Core & Security (Spring Boot)
* **Focus:** Everything inside `backend/src/.../auth` and `backend/src/.../user`.
* **Responsibilities:**
  * Implement Spring Security and JWT/Session authentication.
  * Define User and Role entities.
  * Build the core Complaint tracking CRUD APIs.
  * Handle the database migrations (Flyway or raw SQL) for users and core tables.

### Person 3: AI Engine, Workflow & File Storage
* **Focus:** `backend/src/.../ai`, `workflow`, and `storage`.
* **Responsibilities:**
  * Implement the Keyword-based AI categorization logic in Java.
  * Manage secure local file storage for image uploads.
  * Define and enforce the Complaint Status Workflow logic (Submitted -> Assigned -> Resolved).
  * Write unit tests for the AI rule engine to ensure accuracy.

## 3. Git Branching Strategy (How to avoid conflicts)

Never push directly to `master`. Instead, use **Feature Branches**:

1. **Create a branch** for what you are working on:
   ```bash
   git checkout -b feature/frontend-login
   ```
2. **Work and Commit** your changes:
   ```bash
   git add .
   git commit -m "feat: built the login UI component"
   ```
3. **Push your branch** to GitHub:
   ```bash
   git push origin feature/frontend-login
   ```
4. **Open a Pull Request (PR)** on GitHub and have one of your partners review it before merging it into `master`.

## 4. Keeping up to date

Before starting new work, always pull the latest changes from `master`:
```bash
git checkout master
git pull origin master
```

## 5. Environment Variables (.env)
Never commit `.env` or `application-dev.yml` files containing real database passwords. I have added these to `.gitignore`. Share your database passwords and secrets with each other via a secure chat, not through GitHub.
