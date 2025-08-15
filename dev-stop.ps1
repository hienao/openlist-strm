# Windows Development Environment Stop Script
# Stop frontend and backend development servers

# Set console encoding to UTF-8 to properly display Unicode characters
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "Stopping development environment..." -ForegroundColor Yellow

# Stop frontend service
if (Test-Path ".frontend.pid") {
    $frontendPid = Get-Content ".frontend.pid" -Raw | ForEach-Object { $_.Trim() }
    try {
        $process = Get-Process -Id $frontendPid -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "Stopping frontend service (PID: $frontendPid)..." -ForegroundColor Cyan
            Stop-Process -Id $frontendPid -Force
            Write-Host "Frontend service stopped" -ForegroundColor Green
        } else {
            Write-Host "Frontend service already stopped" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "Frontend service already stopped" -ForegroundColor Yellow
    }
    Remove-Item ".frontend.pid" -Force -ErrorAction SilentlyContinue
} else {
    Write-Host "Frontend service PID file not found" -ForegroundColor Yellow
}

# Stop backend service
if (Test-Path ".backend.pid") {
    $backendPid = Get-Content ".backend.pid" -Raw | ForEach-Object { $_.Trim() }
    try {
        $process = Get-Process -Id $backendPid -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "Stopping backend service (PID: $backendPid)..." -ForegroundColor Cyan
            Stop-Process -Id $backendPid -Force
            Write-Host "Backend service stopped" -ForegroundColor Green
        } else {
            Write-Host "Backend service already stopped" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "Backend service already stopped" -ForegroundColor Yellow
    }
    Remove-Item ".backend.pid" -Force -ErrorAction SilentlyContinue
} else {
    Write-Host "Backend service PID file not found" -ForegroundColor Yellow
}

# Additional cleanup: find and stop possible remaining processes
Write-Host "Cleaning up remaining processes..." -ForegroundColor Yellow

# Find and stop Node.js development server (nuxt dev)
try {
    $nuxtProcesses = Get-Process | Where-Object { $_.ProcessName -eq "node" -and $_.CommandLine -like "*nuxt*dev*" }
    if ($nuxtProcesses) {
        Write-Host "Found Nuxt remaining processes, cleaning up..." -ForegroundColor Cyan
        $nuxtProcesses | Stop-Process -Force
    }
}
catch {
    # 忽略错误
}

# Find and stop Java Gradle processes
try {
    $gradleProcesses = Get-Process | Where-Object { $_.ProcessName -eq "java" -and $_.CommandLine -like "*gradle*bootRun*" }
    if ($gradleProcesses) {
        Write-Host "Found Gradle remaining processes, cleaning up..." -ForegroundColor Cyan
        $gradleProcesses | Stop-Process -Force
    }
}
catch {
    # 忽略错误
}

# Find and stop processes occupying ports
try {
    # Check port 3000 (frontend)
    $port3000 = netstat -ano | Select-String ":3000" | Select-String "LISTENING"
    if ($port3000) {
        $pid = ($port3000 -split "\s+")[-1]
        if ($pid -and $pid -ne "0") {
            Write-Host "Cleaning up port 3000 occupied process (PID: $pid)..." -ForegroundColor Cyan
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        }
    }
    
    # Check port 8080 (backend)
    $port8080 = netstat -ano | Select-String ":8080" | Select-String "LISTENING"
    if ($port8080) {
        $pid = ($port8080 -split "\s+")[-1]
        if ($pid -and $pid -ne "0") {
            Write-Host "Cleaning up port 8080 occupied process (PID: $pid)..." -ForegroundColor Cyan
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        }
    }
}
catch {
    # 忽略错误
}

Write-Host ""
Write-Host "Development environment stopped successfully!" -ForegroundColor Green
Write-Host "To restart, run: .\dev-start.ps1" -ForegroundColor Cyan