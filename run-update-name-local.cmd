@echo off
cd /d "%~dp0"
call mvn test -Dsuite=suites/update-name.xml -Dprofile=sit-wmv -Dremote=false -Dudid=R5CX73LLSPE -DplatformVersion=16 -Dmaven.test.failure.ignore=true
