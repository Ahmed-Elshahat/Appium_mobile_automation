@echo off
cd /d "%~dp0"
call mvn test -Dsuite=suites/auto-topup.xml -Dprofile=sit-wmv
