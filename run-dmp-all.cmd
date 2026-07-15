@echo off
REM ============================================================================
REM  Run ALL 29 DMP suites in ONE mvn run / ONE TestNG suite / ONE profile.
REM  parallel="tests" thread-count=19 in dmp-all.xml => up to 19 LambdaTest
REM  sessions at once; remaining suites queue and run as devices free up.
REM
REM  Usage:   run-dmp-all.cmd
REM  Local:   run-dmp-all.cmd -Dremote=false
REM ============================================================================
setlocal
set MVN=mvn
where %MVN% >nul 2>nul || set "MVN=C:\Tools\apache-maven-3.9.8\bin\mvn.cmd"

"%MVN%" -o test -Dsuite=suites/dmp-all.xml -Dprofile=sit-dmp-all -Denv=SIT -Dremote=true -Dmaven.test.failure.ignore=true %*
endlocal
