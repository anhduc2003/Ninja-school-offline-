@echo off
setlocal EnableExtensions
cd /d %~dp0
where node >nul 2>&1 || (echo [ERROR] Khong tim thay Node.js trong PATH.& exit /b 1)
where npm >nul 2>&1 || (echo [ERROR] Khong tim thay npm trong PATH.& exit /b 1)
if not exist node_modules\mysql2\package.json npm ci --omit=dev --no-audit --no-fund || exit /b 1
if not exist node_modules\bcryptjs\index.js npm ci --omit=dev --no-audit --no-fund || exit /b 1
set "PANEL_URL=http://127.0.0.1:18080"
curl -fsS --max-time 2 %PANEL_URL%/api/system/health >nul 2>&1
if not errorlevel 1 (
  echo Ninja Control Room da san sang: %PANEL_URL%
  exit /b 0
)
start "Ninja Control Room" /b node server.mjs
for /L %%I in (1,1,30) do (
  curl -fsS --max-time 2 %PANEL_URL%/api/system/health >nul 2>&1 && (
    echo Ninja Control Room da san sang: %PANEL_URL%
    exit /b 0
  )
  timeout /t 1 /nobreak >nul
)
echo [ERROR] Panel khong san sang trong 30 giay. Xem logs\admin-panel.log
exit /b 1
endlocal
