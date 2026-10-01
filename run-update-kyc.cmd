@echo off
mvn clean test -Dsuite=suites/update-kyc.xml -Dprofile=sit-wmv -Dlt.appUrl=lt://APP101601911789922755011893 -Dmaven.test.failure.ignore=true
