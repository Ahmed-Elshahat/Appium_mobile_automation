@echo off
REM Focused Kid Receive Gift run (LambdaTest). Avoids PowerShell -Dsuite arg mangling.
call mvn -o clean test -Dsuite=suites/kid-receive-gift.xml -Dprofile=sit-wmv -Dmaven.test.failure.ignore=true
