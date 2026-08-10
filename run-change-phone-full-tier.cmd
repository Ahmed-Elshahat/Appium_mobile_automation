@echo off
mvn clean test -Dsuite=suites/change-phone-number-full-tier.xml -Dprofile=sit-wmv -Dmaven.test.failure.ignore=true
