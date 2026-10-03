$ErrorActionPreference = 'Continue'
$url = 'http://localhost:8080/shop'
$selfPid = $PID
$selfParent = 0
try {
    $selfParent = [int](Get-CimInstance Win32_Process -Filter "ProcessId=$PID" -ErrorAction SilentlyContinue).ParentProcessId
} catch { }

function Pause-Exit([int]$code = 0) {
    Write-Host ''
    Read-Host 'Press Enter to close this window'
    exit $code
}

function Get-BackendPath {
    if ($PSScriptRoot -and (Test-Path -LiteralPath (Join-Path $PSScriptRoot 'mvnw.cmd'))) {
        return (Resolve-Path -LiteralPath $PSScriptRoot).Path
    }
    foreach ($dir in (Get-ChildItem -Path 'D:\' -Directory -ErrorAction SilentlyContinue)) {
        $candidate = Join-Path $dir.FullName 'order-system'
        if (Test-Path -LiteralPath (Join-Path $candidate 'mvnw.cmd')) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }
    Write-Host 'Cannot find order-system\mvnw.cmd on D:\' -ForegroundColor Red
    Pause-Exit 1
}

function Stop-OrderSystemBackend {
    Write-Host 'Closing old Spring Boot processes on port 8080...' -ForegroundColor Yellow
    $toStop = @()

    foreach ($listener in (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue)) {
        $toStop += [int]$listener.OwningProcess
    }

    foreach ($process in (Get-CimInstance Win32_Process -ErrorAction SilentlyContinue)) {
        $cmd = [string]$process.CommandLine
        $name = [string]$process.Name
        if (-not $cmd) { continue }
        $isApp = ($cmd -match 'OrderSystemApplication') -or ($cmd -match 'spring-boot:run' -and $cmd -match 'order-system')
        $isJava = $name -match '^(java|javaw)\.exe$'
        if ($isApp -or ($isJava -and $cmd -match 'OrderSystemApplication')) {
            $toStop += [int]$process.ProcessId
        }
    }

    $toStop = $toStop | Select-Object -Unique | Where-Object {
        $_ -gt 4 -and $_ -ne $selfPid -and $_ -ne $selfParent
    }

    foreach ($processId in $toStop) {
        try {
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
            Write-Host ("Stopped PID " + $processId) -ForegroundColor Gray
        } catch { }
    }

    Start-Sleep -Seconds 2
    $left = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Where-Object {
        $_.OwningProcess -ne $selfPid -and $_.OwningProcess -ne $selfParent
    }
    if ($left) {
        Write-Host 'Port 8080 is still in use after cleanup.' -ForegroundColor Red
        Pause-Exit 1
    }
    Write-Host 'Port 8080 is free.' -ForegroundColor Green
}

function Test-MySqlReady {
    $mysqlExe = 'D:\MySQL8.0\bin\mysql.exe'
    if (-not (Test-Path -LiteralPath $mysqlExe)) { return $false }
    $sqlFile = Join-Path $env:TEMP 'order-system-mysql-ready.sql'
    $outFile = Join-Path $env:TEMP 'order-system-mysql-ready.txt'
    $errFile = Join-Path $env:TEMP 'order-system-mysql-ready.err'
    Set-Content -LiteralPath $sqlFile -Value 'SELECT 1;' -Encoding ASCII
    Remove-Item $outFile, $errFile -Force -ErrorAction SilentlyContinue
    $proc = Start-Process -FilePath $mysqlExe -ArgumentList @(
        '--host=127.0.0.1', '--port=3306', '--user=root', '--password=$(if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { 'YOUR_MYSQL_PASSWORD' })',
        '--database=order_system', '--batch', '--raw'
    ) -Wait -PassThru -WindowStyle Hidden -RedirectStandardInput $sqlFile -RedirectStandardOutput $outFile -RedirectStandardError $errFile
    $text = ''
    if (Test-Path $outFile) { $text = (Get-Content -Raw $outFile).Trim() }
    return ($proc.ExitCode -eq 0 -and $text -match '1')
}

try {
    $backend = Get-BackendPath
    Write-Host ('Backend folder: ' + $backend) -ForegroundColor Gray

    Write-Host 'Checking MySQL...' -ForegroundColor Cyan
    $mysql = Get-Service -Name MySQL80 -ErrorAction SilentlyContinue
    if (-not $mysql) {
        Write-Host 'MySQL80 service not found.' -ForegroundColor Red
        Pause-Exit 1
    }
    if ($mysql.Status -ne 'Running') {
        Write-Host 'Starting MySQL80...' -ForegroundColor Yellow
        Start-Service -Name MySQL80
    }

    Write-Host 'Waiting until MySQL can accept connections...' -ForegroundColor Gray
    $mysqlReady = $false
    for ($i = 1; $i -le 40; $i++) {
        if (Test-MySqlReady) { $mysqlReady = $true; break }
        Write-Host ("Waiting for MySQL... ($i/40)") -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
    if (-not $mysqlReady) {
        Write-Host 'MySQL did not become ready.' -ForegroundColor Red
        Pause-Exit 1
    }
    Write-Host 'MySQL is ready.' -ForegroundColor Green

    Stop-OrderSystemBackend

    Write-Host 'Starting a new Spring Boot backend...' -ForegroundColor Cyan
    $cmd = 'cd /d "' + $backend + '" && mvnw.cmd spring-boot:run'
    Start-Process -FilePath 'cmd.exe' -ArgumentList @('/k', $cmd) | Out-Null

    Write-Host 'Waiting for backend and database...' -ForegroundColor Gray
    $ready = $false
    for ($i = 1; $i -le 90; $i++) {
        Start-Sleep -Seconds 1
        try {
            $hello = Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8080/hello' -TimeoutSec 2
            $products = Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8080/api/products' -TimeoutSec 3
            if ($hello.StatusCode -eq 200 -and $products.StatusCode -eq 200) {
                $ready = $true
                break
            }
        } catch { }
        if (($i % 10) -eq 0) { Write-Host ("Still waiting... $i/90") -ForegroundColor Gray }
    }

    if (-not $ready) {
        Write-Host 'Backend did not become ready. Check the backend cmd window.' -ForegroundColor Red
        Pause-Exit 1
    }

    Write-Host 'Backend is ready. Opening the user ordering page.' -ForegroundColor Green
    Start-Process $url
    Write-Host 'User page: http://localhost:8080/shop' -ForegroundColor Green
    Write-Host 'Admin page: http://localhost:8080/' -ForegroundColor Green
    Write-Host 'Keep the backend cmd window open.' -ForegroundColor Yellow
    Pause-Exit 0
} catch {
    Write-Host 'Launcher failed:' -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Pause-Exit 1
}

