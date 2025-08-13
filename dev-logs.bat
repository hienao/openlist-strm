@echo off
REM Windows Development Environment Log Viewer Batch Script
REM This batch file automatically sets PowerShell execution policy and runs the log script

setlocal

REM Set console code page to UTF-8 to properly display Unicode characters
chcp 65001 >nul 2>&1

REM Check if argument is provided
if "%1"=="" (
    set "ACTION=help"
) else (
    set "ACTION=%1"
)

REM Set PowerShell execution policy and run the script
powershell.exe -ExecutionPolicy Bypass -File ".\dev-logs.ps1" "%ACTION%"

endlocal