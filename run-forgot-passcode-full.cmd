@echo off
cd /d "%~dp0"
call mvn test -Dsuite=suites/forgot-passcode-full-tier.xml -Dprofile=sit-wmv
