@echo off
cd /d "%~dp0"
call mvn test -Dsuite=suites/family-mission-suite.xml -Dprofile=sit-wmv -Dlt.appUrl=lt://APP10160411311780304827767325
