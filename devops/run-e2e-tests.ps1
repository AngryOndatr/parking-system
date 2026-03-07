#Requires -Version 5.1
<#
.SYNOPSIS
    Запуск E2E тестов для parking-system.
.DESCRIPTION
    Проверяет Docker, собирает jar и Docker-образы, запускает E2E тесты (mvn test -Pe2e).
.PARAMETER SkipBuild
    Пропустить Maven-сборку и сборку Docker-образов.
.PARAMETER SkipDockerBuild
    Пропустить только сборку Docker-образов.
.EXAMPLE
    .\run-e2e-tests.ps1
    .\run-e2e-tests.ps1 -SkipBuild
    .\run-e2e-tests.ps1 -SkipDockerBuild
#>
param(
    [switch]$SkipBuild,
    [switch]$SkipDockerBuild
)
$ErrorActionPreference = "Stop"
$RootDir    = Split-Path -Parent $PSScriptRoot
$BackendDir = Join-Path $RootDir "backend"
$E2EDir     = Join-Path $BackendDir "e2e-tests"
function Write-Step { param($msg) Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-OK   { param($msg) Write-Host "    [OK]   $msg" -ForegroundColor Green }
function Write-Warn { param($msg) Write-Host "    [WARN] $msg" -ForegroundColor Yellow }
function Write-Fail { param($msg) Write-Host "    [FAIL] $msg" -ForegroundColor Red }
function Write-Info { param($msg) Write-Host "    $msg"        -ForegroundColor Gray }
# ── 1. Проверка Docker ────────────────────────────────────────────────────────
Write-Step "Проверка Docker"
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Fail "docker.exe не найден в PATH."
    Write-Fail "Установите Docker Desktop: https://www.docker.com/products/docker-desktop/"
    exit 1
}
Write-OK "docker.exe найден"
try {
    # Разделяем stdout и stderr — DEPRECATION NOTICE идёт в stderr и при
    # $ErrorActionPreference="Stop" воспринимается PowerShell как ошибка.
    $tmpOut = [System.IO.Path]::GetTempFileName()
    $tmpErr = [System.IO.Path]::GetTempFileName()
    $proc = Start-Process -FilePath "docker" -ArgumentList "info" `
        -RedirectStandardOutput $tmpOut -RedirectStandardError $tmpErr `
        -Wait -PassThru -NoNewWindow
    $stdout = Get-Content $tmpOut -Raw -ErrorAction SilentlyContinue
    Remove-Item $tmpOut, $tmpErr -Force -ErrorAction SilentlyContinue

    if ($proc.ExitCode -ne 0) { throw "docker info вернул exit code $($proc.ExitCode)" }
    if ($stdout -notmatch 'Server Version') { throw "daemon не вернул Server Version" }
    Write-OK "Docker daemon запущен"
} catch {
    Write-Fail "Docker daemon недоступен: $_"
    Write-Fail "Запустите Docker Desktop и дождитесь значка в трее."
    Write-Warn "Если ошибка BadRequest 400: добавьте в Docker Engine config:"
    Write-Warn '    "min-api-version": "1.24"'
    exit 1
}
$pipePath = "\\.\pipe\docker_engine"
if (Test-Path $pipePath) {
    Write-OK "Named pipe доступен ($pipePath)"
} else {
    Write-Warn "Named pipe $pipePath не найден — Testcontainers может не подключиться."
    Write-Warn "Проверьте Docker Desktop -> General -> Use the WSL 2 based engine."
}
# ── 2. Поиск Maven ────────────────────────────────────────────────────────────
Write-Step "Поиск Maven"
$mvnCmd = $null
$mvnInPath = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnInPath) {
    $mvnCmd = $mvnInPath.Source
} else {
    $candidates = @(
        "C:\Gene_Soft\apache-maven-3.6.3\bin\mvn.cmd",
        "C:\Program Files\Apache\Maven\bin\mvn.cmd",
        "$env:M2_HOME\bin\mvn.cmd"
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { $mvnCmd = $c; break }
    }
}
if (-not $mvnCmd) {
    Write-Fail "mvn не найден. Установите Maven или добавьте в PATH."
    exit 1
}
Write-OK "Maven: $mvnCmd"
# ── 3. Maven-сборка ───────────────────────────────────────────────────────────
if ($SkipBuild) {
    Write-Step "Maven-сборка пропущена (-SkipBuild)"
} else {
    Write-Step "Maven clean install (без тестов)"
    Write-Info "Ожидайте ~1-2 мин..."
    & $mvnCmd -f "$RootDir\pom.xml" clean install -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Fail "Maven завершился с ошибкой (код $LASTEXITCODE)"
        exit 1
    }
    Write-OK "Maven-сборка успешна"
}
# ── 4. Сборка Docker-образов ──────────────────────────────────────────────────
$services = @(
    @{ Name = "eureka-server";        Dir = "eureka-server" },
    @{ Name = "api-gateway";          Dir = "api-gateway" },
    @{ Name = "client-service";       Dir = "client-service" },
    @{ Name = "billing-service";      Dir = "billing-service" },
    @{ Name = "gate-control-service"; Dir = "gate-control-service" },
    @{ Name = "management-service";   Dir = "management-service" },
    @{ Name = "reporting-service";    Dir = "reporting-service" }
)
if ($SkipBuild -or $SkipDockerBuild) {
    Write-Step "Сборка Docker-образов пропущена — проверяем наличие"
    $missing = @()
    foreach ($svc in $services) {
        $tmpImg = [System.IO.Path]::GetTempFileName()
        $tmpImgErr = [System.IO.Path]::GetTempFileName()
        Start-Process -FilePath "docker" -ArgumentList @("images","-q","$($svc.Name):latest") `
            -RedirectStandardOutput $tmpImg -RedirectStandardError $tmpImgErr `
            -Wait -NoNewWindow | Out-Null
        $id = (Get-Content $tmpImg -Raw -ErrorAction SilentlyContinue).Trim()
        Remove-Item $tmpImg, $tmpImgErr -Force -ErrorAction SilentlyContinue
        if (-not $id) { $missing += $svc.Name }
    }
    if ($missing.Count -gt 0) {
        Write-Fail "Образы отсутствуют: $($missing -join ', ')"
        Write-Fail "Запустите скрипт без флагов -SkipBuild / -SkipDockerBuild."
        exit 1
    }
    Write-OK "Все образы присутствуют"
} else {
    Write-Step "Сборка Docker-образов"
    foreach ($svc in $services) {
        $svcDir = Join-Path $BackendDir $svc.Dir
        if (-not (Test-Path "$svcDir\Dockerfile")) {
            Write-Warn "Dockerfile не найден для $($svc.Name) — пропускаем"
            continue
        }
        Write-Info "Сборка $($svc.Name):latest ..."
        $tmpBuildErr = [System.IO.Path]::GetTempFileName()
        $procBuild = Start-Process -FilePath "docker" `
            -ArgumentList @("build","-t","$($svc.Name):latest",$svcDir,"-q") `
            -RedirectStandardError $tmpBuildErr `
            -Wait -PassThru -NoNewWindow
        Remove-Item $tmpBuildErr -Force -ErrorAction SilentlyContinue
        if ($procBuild.ExitCode -ne 0) {
            Write-Fail "Ошибка сборки образа $($svc.Name)"
            exit 1
        }
        Write-OK "$($svc.Name):latest собран"
    }
}
# ── 5. Запуск E2E тестов ──────────────────────────────────────────────────────
Write-Step "Запуск E2E тестов (mvn test -Pe2e)"
Write-Info "Ожидайте ~2-3 мин (подъём контейнеров + тестовый сценарий)..."
$startTime = Get-Date
& $mvnCmd -f "$RootDir\pom.xml" test -Pe2e
$exitCode = $LASTEXITCODE
$elapsed  = [math]::Round(((Get-Date) - $startTime).TotalSeconds)
Write-Host ""
if ($exitCode -eq 0) {
    Write-Host "╔══════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "║  E2E ТЕСТЫ ПРОЙДЕНЫ УСПЕШНО  v           ║" -ForegroundColor Green
    Write-Host "╚══════════════════════════════════════════╝" -ForegroundColor Green
    Write-Host "  Время: ${elapsed}с" -ForegroundColor Green
} else {
    Write-Host "╔══════════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "║  E2E ТЕСТЫ ЗАВЕРШИЛИСЬ С ОШИБКОЙ  x      ║" -ForegroundColor Red
    Write-Host "╚══════════════════════════════════════════╝" -ForegroundColor Red
    Write-Host "  Время: ${elapsed}с" -ForegroundColor Red
    Write-Host "  Отчёт surefire: $E2EDir\target\surefire-reports\" -ForegroundColor Yellow
    exit 1
}
