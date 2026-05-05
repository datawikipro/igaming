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
# 0. Git Sync & Branch Detection
# ---------------------------------------------------------------
Write-Host "[Phase 0] Syncing with Git..." -ForegroundColor Cyan

# Get current branch name
$currentBranch = git rev-parse --abbrev-ref HEAD
if ($LASTEXITCODE -ne 0) { throw "Could not detect Git branch" }
Write-Host "  > Current branch: $currentBranch" -ForegroundColor DarkGray

# Check for uncommitted changes and auto-commit
$status = git status --porcelain
if ($status) {
    Write-Host "  > Uncommitted changes detected. Auto-committing..." -ForegroundColor DarkGray
    git add .
    git commit -m "ci: auto-sync before remote build" | Out-Null
    if ($LASTEXITCODE -ne 0) { Write-Host "  ! Warning: Auto-commit failed." -ForegroundColor Yellow }
} else {
    Write-Host "  > No changes to commit." -ForegroundColor DarkGray
}

# Push to remote
Write-Host "  > Pushing to origin $currentBranch..." -ForegroundColor DarkGray
git push origin $currentBranch | Out-Null
if ($LASTEXITCODE -ne 0) { 
    Write-Host "  ! Git push failed. Please check your internet or permissions." -ForegroundColor Yellow
}

# ---------------------------------------------------------------
# 1. Ensure GHCR authentication (needed for both Jib and Docker)
# ---------------------------------------------------------------
Write-Host "`n[Phase 1] Authenticating..." -ForegroundColor Cyan
$ghToken = (gh auth token 2>$null)
if (-not $ghToken) {
    Write-Host "FATAL: Cannot get GitHub token. Run 'gh auth login' first." -ForegroundColor Red
    exit 1
}

# Login to GHCR on remote Docker daemon (for docker build/push)
Write-Host "Setting Docker context to 'remote-srv'..." -ForegroundColor DarkGray
$oldEAP = $ErrorActionPreference
$ErrorActionPreference = 'SilentlyContinue'
& docker context use remote-srv 2>$null | Out-Null
$ErrorActionPreference = $oldEAP

if ($LASTEXITCODE -ne 0) {
    Write-Host "WARNING: Could not switch to 'remote-srv' context. Using default." -ForegroundColor Yellow
}

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
$jibServices = @("igaming-aggregator", "igaming-bot", "igaming-portal", "igaming-admin-backend")

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
            mvn -pl $module "com.google.cloud.tools:jib-maven-plugin:3.4.4:build" "-Djib.from.image=eclipse-temurin:21-jre-jammy" "-Djib.to.image=ghcr.io/datawikipro/${module}:latest" "-Djib.to.auth.username=datawikipro" "-Djib.to.auth.password=$ghToken" "-DskipTests" "-Dmaven.test.skip=true"
            if ($LASTEXITCODE -ne 0) { throw "Jib build failed" }
            
            Pop-Location
        }
        else {
            # --- Remote Docker build (source code is sent to daemon, JAR is built inside Docker) ---
            # No local Maven package needed!
            
            $imageTag = "ghcr.io/datawikipro/${module}:latest"
            
            # Run build from root to include all modules in context (needed for dto/core)
            Push-Location $root
            
            Write-Host "  > Building image remotely directly from Git (Zero Local Context)..." -ForegroundColor DarkGray
            # Use Git URL as context. Docker daemon will clone it itself.
            $gitUrl = "https://x-access-token:${ghToken}@github.com/datawikipro/igaming.git#${currentBranch}"
            docker build -f "$module/Dockerfile" -t $imageTag $gitUrl
            if ($LASTEXITCODE -ne 0) { throw "Remote Git build failed" }
            
            Write-Host "  > Pushing image..." -ForegroundColor DarkGray
            docker push $imageTag
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
