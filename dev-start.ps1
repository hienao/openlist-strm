# Windows Development Environment Start Script
# Start frontend and backend development servers

$ErrorActionPreference = "Stop"

# Set console encoding to UTF-8 to properly display Unicode characters
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "Starting development environment..." -ForegroundColor Green

# Check required tools
$nodeAvailable = Get-Command node -ErrorAction SilentlyContinue
$javaAvailable = Get-Command java -ErrorAction SilentlyContinue

if (-not $nodeAvailable) {
    Write-Host "Error: Node.js is required for frontend" -ForegroundColor Red
    Write-Host "Please install Node.js from https://nodejs.org/" -ForegroundColor Yellow
    exit 1
}

if (-not $javaAvailable) {
    Write-Host "Warning: Java 21 is required for backend" -ForegroundColor Yellow
    Write-Host "Backend will not start. Please install Java 21 from https://adoptium.net/" -ForegroundColor Yellow
    Write-Host "Continuing with frontend only..." -ForegroundColor Cyan
    $backendSkipped = $true
} else {
    $backendSkipped = $false
}

# Create logs directory
if (-not (Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs" | Out-Null
}

# Set environment variables
$env:DATABASE_PATH = "./data/config/db/openlist2strm.db"
$env:ALLOWED_ORIGINS = "http://localhost:3000,http://localhost:8080"
$env:ALLOWED_METHODS = "*"
$env:ALLOWED_HEADERS = "*"
$env:ALLOWED_EXPOSE_HEADERS = "*"
$env:JWT_SECRET = "dev-secret-key"
$env:JWT_EXPIRATION_MIN = "1440"

Write-Host "Installing frontend dependencies..." -ForegroundColor Yellow
Set-Location frontend
if (-not (Test-Path "node_modules")) {
    npm install
}
Set-Location ..

Write-Host "Starting frontend dev server (port 3000)..." -ForegroundColor Cyan
$frontendProcess = Start-Process -FilePath "cmd" -ArgumentList "/c", "npm", "run", "dev" -WorkingDirectory "frontend" -RedirectStandardOutput "logs/frontend.log" -RedirectStandardError "logs/frontend-error.log" -PassThru -WindowStyle Hidden
$frontendPid = $frontendProcess.Id
Write-Host "Frontend PID: $frontendPid" -ForegroundColor Green

if (-not $backendSkipped) {
    Write-Host "Starting backend dev server (port 8080)..." -ForegroundColor Cyan
    $backendProcess = Start-Process -FilePath "cmd" -ArgumentList "/c", "gradlew.bat", "bootRun" -WorkingDirectory "backend" -RedirectStandardOutput "logs/backend.log" -RedirectStandardError "logs/backend-error.log" -PassThru -WindowStyle Hidden
    $backendPid = $backendProcess.Id
    Write-Host "Backend PID: $backendPid" -ForegroundColor Green
} else {
    Write-Host "Skipping backend startup (Java not available)" -ForegroundColor Yellow
    $backendPid = $null
}

# Save PIDs to files
$frontendPid | Out-File -FilePath ".frontend.pid" -Encoding UTF8
if ($backendPid) {
    $backendPid | Out-File -FilePath ".backend.pid" -Encoding UTF8
}

Write-Host ""
Write-Host "Development environment started!" -ForegroundColor Green
Write-Host "Frontend URL: http://localhost:3000" -ForegroundColor Cyan
Write-Host "Backend API: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Swagger Docs: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "Log files:" -ForegroundColor Yellow
Write-Host "  Frontend: logs/frontend.log" -ForegroundColor Gray
Write-Host "  Backend: logs/backend.log" -ForegroundColor Gray
Write-Host ""
Write-Host "To stop services run: .\dev-stop.ps1" -ForegroundColor Yellow
Write-Host "To view logs run: .\dev-logs.ps1" -ForegroundColor Yellow
Write-Host ""
Write-Host "Waiting for services to start..." -ForegroundColor Yellow

# Wait for frontend service
Write-Host "Checking frontend service status..." -ForegroundColor Yellow
$frontendReady = $false
for ($i = 1; $i -le 30; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:3000" -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "Frontend service started successfully (took $i seconds)" -ForegroundColor Green
            $frontendReady = $true
            break
        }
    }
    catch {
        # Ignore errors, continue waiting
    }
    Write-Host "Frontend service starting... ($i/30 seconds)" -ForegroundColor Gray
    Start-Sleep -Seconds 1
}

if (-not $frontendReady) {
    Write-Host "Frontend service failed to start or timed out" -ForegroundColor Red
    Write-Host "Check frontend logs: Get-Content logs/frontend.log -Tail 20" -ForegroundColor Yellow
}

# Wait for backend service
if (-not $backendSkipped) {
    Write-Host "Checking backend service status..." -ForegroundColor Yellow
    $backendReady = $false
    for ($i = 1; $i -le 60; $i++) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "Backend service started successfully (took $i seconds)" -ForegroundColor Green
                $backendReady = $true
                break
            }
        }
        catch {
            # Ignore errors, continue waiting
        }
        Write-Host "Backend service starting... ($i/60 seconds)" -ForegroundColor Gray
        Start-Sleep -Seconds 1
    }
    
    if (-not $backendReady) {
        Write-Host "Backend service failed to start or timed out" -ForegroundColor Red
        Write-Host "Check backend logs: Get-Content logs/backend.log -Tail 20" -ForegroundColor Yellow
    }
} else {
    Write-Host "Backend service skipped (Java not available)" -ForegroundColor Yellow
    $backendReady = $false
}

Write-Host ""
if ($frontendReady -and $backendReady) {
    Write-Host "Development environment is fully ready! Happy coding!" -ForegroundColor Green
} elseif ($frontendReady -and $backendSkipped) {
    Write-Host "Frontend is ready! Backend was skipped (install Java 21 to enable backend)" -ForegroundColor Green
} elseif ($frontendReady) {
    Write-Host "Frontend is ready, but backend failed to start" -ForegroundColor Yellow
} elseif ($backendReady) {
    Write-Host "Backend is ready, but frontend failed to start" -ForegroundColor Yellow
} else {
    if ($backendSkipped) {
        Write-Host "Frontend failed to start, backend was skipped" -ForegroundColor Red
    } else {
        Write-Host "Both frontend and backend failed to start, check log files" -ForegroundColor Red
    }
}