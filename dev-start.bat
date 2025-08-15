@echo off
REM Windows Development Environment Start Batch Script
REM This batch file automatically sets PowerShell execution policy and runs the start script

setlocal

REM Set console code page to UTF-8 to properly display Unicode characters
chcp 65001 >nul 2>&1

echo Starting development environment...
echo.

REM Set PowerShell execution policy and run the script
powershell.exe -ExecutionPolicy Bypass -File ".\dev-start.ps1"

endlocal