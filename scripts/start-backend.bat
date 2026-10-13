@echo off
cd /d "%~dp0..\backend"
echo ========================================================
echo   Starting CivicEye AI Backend (Spring Boot REST API)
echo   Local API: http://localhost:8080
echo ========================================================
set "JAVA_HOME=C:\Editor\jbr"
set "PATH=C:\Editor\jbr\bin;%PATH%"
mvnw.cmd spring-boot:run
pause
