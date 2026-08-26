@echo off
setlocal EnableExtensions
set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"

echo Dung Java game server, panel va scheduler...
taskkill /FI "WINDOWTITLE eq Ninja School Game Server" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Ninja Control Room" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Ninja Control Room Scheduler" /T /F >nul 2>&1
echo Da gui lenh dung game server, panel va scheduler.
call "%~dp0windows-stop-database.cmd"
endlocal
