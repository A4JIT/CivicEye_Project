@echo off
cd /d "%~dp0.."
echo ========================================================
echo   Starting Prisma Studio (Database Visual Editor)
echo   Local URL: http://localhost:5555
echo ========================================================
npx prisma@5 studio
pause
