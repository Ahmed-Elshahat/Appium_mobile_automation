@echo off
cd /d "%~dp0"
call mvn test -Dsuite=suites/transfast-bankdeposit-transfer.xml -Dprofile=sit-remittance
