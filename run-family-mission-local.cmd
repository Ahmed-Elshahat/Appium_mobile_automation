@echo off
cd /d "%~dp0"
call mvn test -Dsuite=suites/family-mission-suite.xml -Dprofile=sit-wmv -Dremote=false -Dudid=R5CX73LLSPE -DplatformVersion=16
