@echo off
setlocal EnableExtensions
set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
set "MYSQL_SERVICE=%NSO_MYSQL_SERVICE%"
if "%MYSQL_SERVICE%"=="" set "MYSQL_SERVICE=MariaDB"

where java >nul 2>&1 || (echo [ERROR] Khong tim thay Java 17+ trong PATH.& exit /b 1)
where mvn >nul 2>&1 || (echo [ERROR] Khong tim thay Maven trong PATH.& exit /b 1)
where node >nul 2>&1 || (echo [ERROR] Khong tim thay Node.js trong PATH.& exit /b 1)

sc query "%MYSQL_SERVICE%" >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Khong tim thay Windows service "%MYSQL_SERVICE%".
  echo Cai MariaDB/MySQL Windows service truoc, hoac dat NSO_MYSQL_SERVICE thanh ten service dung.
  exit /b 1
)
sc query "%MYSQL_SERVICE%" | find "RUNNING" >nul
if errorlevel 1 (
  echo [1/4] Khoi dong MariaDB service %MYSQL_SERVICE%...
  net start "%MYSQL_SERVICE%" || exit /b 1
)

cd /d "%ROOT_DIR%"
if not exist "config.properties" copy /Y "config.properties.example" "config.properties" >nul || exit /b 1
echo [2/5] Ap dung event pending neu co...
node admin-panel\apply-event-plan.mjs || exit /b 1

echo [3/5] Build Java server neu can...
if not exist "target\Nso-jar-with-dependencies.jar" call mvn -DskipTests package || exit /b 1

echo [4/5] Khoi dong Ninja Control Room va scheduler...
call admin-panel\run-panel-stack.cmd || exit /b 1
for /L %%I in (1,1,30) do (
  curl -fsS --max-time 2 http://127.0.0.1:18080/api/system/health >nul 2>&1 && goto :panel_ready
  timeout /t 1 /nobreak >nul
)
echo [ERROR] Panel health check khong thanh cong. Xem logs\admin-panel.log
exit /b 1
:panel_ready

echo [5/5] Khoi dong Java game server headless...
start "Ninja School Game Server" /b java -Dninja.headless=true -jar "target\Nso-jar-with-dependencies.jar"
echo.
echo Game TCP: 127.0.0.1:14444
echo Control Room: http://127.0.0.1:18080
echo Panel health: http://127.0.0.1:18080/api/system/health
echo Control Room local-only: khong can dang nhap
endlocal
