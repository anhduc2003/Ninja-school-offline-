@echo off
setlocal
cd /d %~dp0
if not exist node_modules\mysql2 npm install --omit=dev --no-audit --no-fund
start "Ninja Control Room Scheduler" /b node scheduler.mjs
echo Ninja Control Room scheduler started.
endlocal
