@echo off
cd /d "%~dp0"
call mvn test -Dsuite=suites/family-mission-suite.xml -Dprofile=sit-wmv -Dlt.appUrl=lt://APP1016034271783616362836254
