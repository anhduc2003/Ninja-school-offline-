@echo off
setlocal
cd /d %~dp0
call start-panel.cmd
call start-scheduler.cmd
echo.
echo Control Room: http://127.0.0.1:18080
echo First-login credential: data\first-login.txt
endlocal
