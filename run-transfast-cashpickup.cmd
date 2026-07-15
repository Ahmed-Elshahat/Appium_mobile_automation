@echo off
cd /d "%~dp0"
call mvn test -Dsuite=suites/transfast-cashpickup-transfer.xml -Dprofile=sit-remittance
