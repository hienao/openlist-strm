# Windows Development Environment Log Viewer Script
# View real-time logs for frontend and backend services

param(
    [Parameter(Position=0)]
    [string]$Action = "help"
)

# Set console encoding to UTF-8 to properly display Unicode characters
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

function Show-Usage {
    Write-Host "Development Log Viewer" -ForegroundColor Green
    Write-Host ""
    Write-Host "Usage: .\dev-logs.ps1 [option]" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  frontend, f    View frontend logs" -ForegroundColor Gray
    Write-Host "  backend, b     View backend logs" -ForegroundColor Gray
    Write-Host "  both, all      View both frontend and backend logs" -ForegroundColor Gray
    Write-Host "  status, s      Check service status" -ForegroundColor Gray
    Write-Host "  clear, c       Clear log files" -ForegroundColor Gray
    Write-Host "  help, h        Show this help" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor Yellow
    Write-Host "  .\dev-logs.ps1 frontend    # View frontend logs" -ForegroundColor Gray
    Write-Host "  .\dev-logs.ps1 both        # View both logs" -ForegroundColor Gray
    Write-Host "  .\dev-logs.ps1 status      # Check service status" -ForegroundColor Gray
}

function Check-ServiceStatus {
    Write-Host "Checking service status..." -ForegroundColor Yellow
    Write-Host ""
    
    # Check frontend service
    if (Test-Path ".frontend.pid") {
        $frontendPid = Get-Content ".frontend.pid" -Raw | ForEach-Object { $_.Trim() }
        try {
            $process = Get-Process -Id $frontendPid -ErrorAction SilentlyContinue
            if ($process) {
                Write-Host "Frontend service: Running (PID: $frontendPid)" -ForegroundColor Green
            } else {
                Write-Host "Frontend service: Stopped" -ForegroundColor Red
            }
        }
        catch {
            Write-Host "Frontend service: Stopped" -ForegroundColor Red
        }
    } else {
        Write-Host "Frontend service: Not started" -ForegroundColor Red
    }
    
    # Check backend service
    if (Test-Path ".backend.pid") {
        $backendPid = Get-Content ".backend.pid" -Raw | ForEach-Object { $_.Trim() }
        try {
            $process = Get-Process -Id $backendPid -ErrorAction SilentlyContinue
            if ($process) {
                Write-Host "Backend service: Running (PID: $backendPid)" -ForegroundColor Green
            } else {
                Write-Host "Backend service: Stopped" -ForegroundColor Red
            }
        }
        catch {
            Write-Host "Backend service: Stopped" -ForegroundColor Red
        }
    } else {
        Write-Host "Backend service: Not started" -ForegroundColor Red
    }
    
    Write-Host ""
    
    # Check port usage
    Write-Host "Port usage:" -ForegroundColor Cyan
    try {
        $port3000 = netstat -ano | Select-String ":3000" | Select-String "LISTENING"
        if ($port3000) {
            Write-Host "   Port 3000: In use (Frontend service)" -ForegroundColor Green
        } else {
            Write-Host "   Port 3000: Available" -ForegroundColor Red
        }
        
        $port8080 = netstat -ano | Select-String ":8080" | Select-String "LISTENING"
        if ($port8080) {
            Write-Host "   Port 8080: In use (Backend service)" -ForegroundColor Green
        } else {
            Write-Host "   Port 8080: Available" -ForegroundColor Red
        }
    }
    catch {
        Write-Host "   Cannot check port status" -ForegroundColor Yellow
    }
    
    Write-Host ""
    
    # Check log files
    Write-Host "Log file status:" -ForegroundColor Cyan
    if (Test-Path "logs/frontend.log") {
        $frontendLogSize = (Get-Item "logs/frontend.log").Length
        Write-Host "   Frontend log: Exists (Size: $([math]::Round($frontendLogSize/1KB, 2)) KB)" -ForegroundColor Green
    } else {
        Write-Host "   Frontend log: Not found" -ForegroundColor Red
    }
    
    if (Test-Path "logs/backend.log") {
        $backendLogSize = (Get-Item "logs/backend.log").Length
        Write-Host "   Backend log: Exists (Size: $([math]::Round($backendLogSize/1KB, 2)) KB)" -ForegroundColor Green
    } else {
        Write-Host "   Backend log: Not found" -ForegroundColor Red
    }
}

function Show-FrontendLog {
    if (Test-Path "logs/frontend.log") {
        Write-Host "Frontend logs (Press Ctrl+C to exit):" -ForegroundColor Cyan
        Write-Host "" 
        Get-Content "logs/frontend.log" -Tail 50 -Wait
    } else {
        Write-Host "Frontend log file not found" -ForegroundColor Red
        Write-Host "Please start development environment first: .\dev-start.ps1" -ForegroundColor Yellow
    }
}

function Show-BackendLog {
    if (Test-Path "logs/backend.log") {
        Write-Host "Backend logs (Press Ctrl+C to exit):" -ForegroundColor Cyan
        Write-Host ""
        Get-Content "logs/backend.log" -Tail 50 -Wait
    } else {
        Write-Host "Backend log file not found" -ForegroundColor Red
        Write-Host "Please start development environment first: .\dev-start.ps1" -ForegroundColor Yellow
    }
}

function Show-BothLogs {
    Write-Host "Showing both frontend and backend logs (Press Ctrl+C to exit):" -ForegroundColor Green
    Write-Host "Tip: Frontend logs prefixed with [F], backend logs with [B]" -ForegroundColor Yellow
    Write-Host ""
    
    if (-not (Test-Path "logs/frontend.log") -and -not (Test-Path "logs/backend.log")) {
        Write-Host "No log files found" -ForegroundColor Red
        Write-Host "Please start development environment first: .\dev-start.ps1" -ForegroundColor Yellow
        return
    }
    
    # Use PowerShell jobs to monitor both log files simultaneously
    $frontendJob = $null
    $backendJob = $null
    
    if (Test-Path "logs/frontend.log") {
        $frontendJob = Start-Job -ScriptBlock {
            Get-Content "logs/frontend.log" -Tail 25 -Wait | ForEach-Object { "[F] $_" }
        }
    }
    
    if (Test-Path "logs/backend.log") {
        $backendJob = Start-Job -ScriptBlock {
            Get-Content "logs/backend.log" -Tail 25 -Wait | ForEach-Object { "[B] $_" }
        }
    }
    
    try {
        while ($true) {
            if ($frontendJob) {
                $frontendOutput = Receive-Job -Job $frontendJob
                if ($frontendOutput) {
                    $frontendOutput | ForEach-Object { Write-Host $_ -ForegroundColor Cyan }
                }
            }
            
            if ($backendJob) {
                $backendOutput = Receive-Job -Job $backendJob
                if ($backendOutput) {
                    $backendOutput | ForEach-Object { Write-Host $_ -ForegroundColor Yellow }
                }
            }
            
            Start-Sleep -Milliseconds 100
        }
    }
    finally {
        if ($frontendJob) { Stop-Job -Job $frontendJob; Remove-Job -Job $frontendJob }
        if ($backendJob) { Stop-Job -Job $backendJob; Remove-Job -Job $backendJob }
    }
}

function Clear-Logs {
    Write-Host "Clearing log files..." -ForegroundColor Yellow
    
    if (Test-Path "logs/frontend.log") {
        Clear-Content "logs/frontend.log"
        Write-Host "Frontend log cleared" -ForegroundColor Green
    }
    
    if (Test-Path "logs/backend.log") {
        Clear-Content "logs/backend.log"
        Write-Host "Backend log cleared" -ForegroundColor Green
    }
    
    if (-not (Test-Path "logs/frontend.log") -and -not (Test-Path "logs/backend.log")) {
        Write-Host "No log files found" -ForegroundColor Yellow
    }
}

# Main logic
switch ($Action.ToLower()) {
    { $_ -in @("frontend", "f") } {
        Show-FrontendLog
    }
    { $_ -in @("backend", "b") } {
        Show-BackendLog
    }
    { $_ -in @("both", "all") } {
        Show-BothLogs
    }
    { $_ -in @("status", "s") } {
        Check-ServiceStatus
    }
    { $_ -in @("clear", "c") } {
        Clear-Logs
    }
    { $_ -in @("help", "h", "") } {
        Show-Usage
    }
    default {
        Write-Host "Unknown option: $Action" -ForegroundColor Red
        Write-Host ""
        Show-Usage
    }
}