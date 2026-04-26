# restart-ci.ps1
# Monorepo CI/CD: Build changed modules + Push Docker images + K8s Restart
# Usage: .\restart-ci.ps1              (build all)
#        .\restart-ci.ps1 -Only leon   (build only igaming-source-leon)
#        .\restart-ci.ps1 -Only base   (build and push base image)

param(
    [string]$Only = "",
    [int]$Parallel = 4
)

$ErrorActionPreference = "Stop"
$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host ""
Write-Host "=== iGaming Monorepo CI ===" -ForegroundColor Cyan
Write-Host ""

# ---------------------------------------------------------------
# 0. Ensure GHCR authentication (needed for both Jib and Docker)
# ---------------------------------------------------------------
$ghToken = (gh auth token 2>$null)
if (-not $ghToken) {
    Write-Host "FATAL: Cannot get GitHub token. Run 'gh auth login' first." -ForegroundColor Red
    exit 1
}

# Login to GHCR on remote Docker daemon (for docker build/push)
$ghToken | docker login ghcr.io -u datawikipro --password-stdin 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "WARNING: Docker GHCR login failed. Docker push may fail." -ForegroundColor Yellow
}

# ---------------------------------------------------------------
# 1.5 Build and push base image (if requested)
# ---------------------------------------------------------------
if ($Only -eq "base") {
    Write-Host "[Phase 1.5] Building and pushing base image..." -ForegroundColor Cyan
    Push-Location "$rootDir/igaming-source-core"
    docker build -t ghcr.io/datawikipro/igaming-source-base:latest -f Dockerfile.base .
    if ($LASTEXITCODE -ne 0) {
        Write-Host "FATAL: Base image build failed!" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    docker push ghcr.io/datawikipro/igaming-source-base:latest
    if ($LASTEXITCODE -ne 0) {
        Write-Host "FATAL: Base image push failed!" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Pop-Location
    Write-Host "  Base image: OK" -ForegroundColor Green
    exit 0
}

# ---------------------------------------------------------------
# 2. Determine which modules to build
# ---------------------------------------------------------------
$jibServices = @("igaming-aggregator", "igaming-bot", "igaming-portal")

$crawlerServices = Get-ChildItem -Path $rootDir -Directory -Filter "igaming-source-*" |
    Where-Object { $_.Name -ne "igaming-source-core" } |
    Select-Object -ExpandProperty Name

if ($Only -ne "") {
    # Filter to only the requested module
    $match = "igaming-source-$Only"
    if ($Only -in $jibServices) { $match = $Only }
    $allModules = @($match)
    Write-Host "[Phase 2] Building single module: $match" -ForegroundColor Yellow
} else {
    $allModules = $jibServices + $crawlerServices
    Write-Host "[Phase 2] Building $($allModules.Count) modules (parallel=$Parallel)..." -ForegroundColor Cyan
}

# ---------------------------------------------------------------
# 3. Build & Push Docker images
# ---------------------------------------------------------------
Write-Host ""
Write-Host "[Phase 3] Building and pushing images..." -ForegroundColor Cyan

$success = [System.Collections.Concurrent.ConcurrentBag[string]]::new()
$failed  = [System.Collections.Concurrent.ConcurrentBag[string]]::new()

$allModules | ForEach-Object {
    $module = $_
    $root = $rootDir
    $jibList = $jibServices

    try {
        if ($module -in $jibList) {
            # --- Jib build (for aggregator, bot, portal) ---
            Push-Location $root
            
            # Compile everything needed first
            mvn -pl $module -am install "-DskipTests" "-Dmaven.test.skip=true" -q
            if ($LASTEXITCODE -ne 0) { throw "Maven install failed" }
            
            # Use jib:build to push directly to GHCR (no Docker daemon required)
            mvn -pl $module "com.google.cloud.tools:jib-maven-plugin:3.4.1:build" "-Djib.from.image=eclipse-temurin:21-jre-jammy" "-Djib.to.image=ghcr.io/datawikipro/${module}:latest" "-Djib.to.auth.username=datawikipro" "-Djib.to.auth.password=$ghToken" "-DskipTests" "-Dmaven.test.skip=true"
            if ($LASTEXITCODE -ne 0) { throw "Jib build failed" }
            
            Pop-Location
        }
        else {
            # --- Docker build (for Playwright crawlers, uses remote Docker daemon) ---
            Push-Location "$root/$module"
            
            # Package JAR
            Push-Location $root
            mvn -pl $module -am package -DskipTests "-Dmaven.test.skip=true"
            if ($LASTEXITCODE -ne 0) { throw "Maven package failed" }
            Pop-Location
            
            # Docker build & push on remote daemon
            $imageTag = "ghcr.io/datawikipro/${module}:latest"
            docker pull ghcr.io/datawikipro/igaming-source-base:latest -q 2>&1 | Out-Null
            docker build -t $imageTag . -q
            if ($LASTEXITCODE -ne 0) { throw "Docker build failed" }
            
            docker push $imageTag -q
            if ($LASTEXITCODE -ne 0) { throw "Docker push failed" }
            
            Pop-Location
        }

        Write-Host "  [$module] OK" -ForegroundColor Green
        $success.Add($module)
    }
    catch {
        Write-Host "  [$module] FAIL: $($_.Exception.Message)" -ForegroundColor Red
        $failed.Add($module)
    }
}

# ---------------------------------------------------------------
# 4. Kubernetes rollout restart
# ---------------------------------------------------------------
Write-Host ""
Write-Host "[Phase 4] Kubernetes rollout restart..." -ForegroundColor Cyan

foreach ($module in $success) {
    if ($module -like "igaming-source-*") {
        kubectl rollout restart deployment "$module-crawler" -n igaming-dev 2>&1 | Out-Null
        kubectl rollout restart deployment "$module-loader"  -n igaming-dev 2>&1 | Out-Null
    } else {
        kubectl rollout restart deployment $module -n igaming-dev 2>&1 | Out-Null
    }
    Write-Host "  [$module] restarted" -ForegroundColor DarkGray
}

# ---------------------------------------------------------------
# Summary
# ---------------------------------------------------------------
Write-Host ""
Write-Host "==============================" -ForegroundColor Cyan
Write-Host "  Success: $($success.Count)" -ForegroundColor Green
Write-Host "  Failed:  $($failed.Count)" -ForegroundColor $(if ($failed.Count -gt 0) { "Red" } else { "Green" })
if ($failed.Count -gt 0) {
    Write-Host "  Failed modules:" -ForegroundColor Red
    foreach ($f in $failed) { Write-Host "    - $f" -ForegroundColor Red }
}
Write-Host "==============================" -ForegroundColor Cyan
