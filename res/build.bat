@echo off

rem Go to project dir
cd %~dp0\..

rem Build project
call mvn -B clean package > nul
