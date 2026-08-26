@echo off
setlocal
cd /d %~dp0
if not exist node_modules\mysql2 npm install --omit=dev --no-audit --no-fund
start "Ninja Control Room" /b node server.mjs
echo Ninja Control Room: http://127.0.0.1:18080
endlocal
