@echo off
cd /d "%~dp0"
call mvn test -Dsuite=suites/send-money-request-approve.xml -Dprofile=sit-wmv
