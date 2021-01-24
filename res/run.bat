@echo off

rem Go to project dir
cd %~dp0\..

cd target
start javaw -jar AFM.jar
