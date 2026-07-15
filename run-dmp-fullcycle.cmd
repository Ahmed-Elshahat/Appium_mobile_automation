@echo off
cd /d "%~dp0"
call mvn test -Dsuite=suites/dmp-fullcycle-order.xml -Dprofile=sit-dmp-c -Dmaven.test.failure.ignore=true
