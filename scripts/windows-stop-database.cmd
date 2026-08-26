@echo off
setlocal EnableExtensions
set "MYSQL_SERVICE=%NSO_MYSQL_SERVICE%"
if "%MYSQL_SERVICE%"=="" set "MYSQL_SERVICE=MariaDB"
choice /M "Dung MariaDB/MySQL Windows service %MYSQL_SERVICE%"
if errorlevel 2 (
  echo Giu MariaDB service dang chay.
  endlocal
  exit /b 0
)
net stop "%MYSQL_SERVICE%" || exit /b 1
echo MariaDB/MySQL service da dung an toan.
endlocal
