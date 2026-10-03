@echo off
cd /d "%~dp0"
powershell.exe -NoExit -ExecutionPolicy Bypass -File "%~dp0StartOrderSystem.ps1"
if errorlevel 1 pause
