@echo off
echo ========================================================
echo   Starting CivicEye AI (Backend + Frontend)
echo ========================================================

echo [1/2] Launching Backend (Spring Boot on port 8080)...
start "CivicEye AI - Backend (Spring Boot)" cmd /k "call "%~dp0scripts\start-backend.bat""

echo [2/2] Launching Frontend (React + Vite on port 5173)...
start "CivicEye AI - Frontend (React + Vite)" cmd /k "call "%~dp0scripts\start-frontend.bat""

echo ========================================================
echo   Services are running!
echo   Frontend: http://localhost:5173
echo   Backend:  http://localhost:8080
echo ========================================================
