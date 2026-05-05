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
Write-Host "[Phase 0] Syncing with Git (including submodules)..." -ForegroundColor Cyan

# Get current branch name
$currentBranch = git rev-parse --abbrev-ref HEAD
if ($LASTEXITCODE -ne 0) { throw "Could not detect Git branch" }
Write-Host "  > Current branch: $currentBranch" -ForegroundColor DarkGray

# 1. Sync all submodules
Write-Host "  > Checking submodules for changes..." -ForegroundColor DarkGray
# Use submodule foreach to add, commit and push in each submodule
# We use '|| true' to ignore submodules without changes or on detached HEADs
git submodule foreach --recursive "git add . && (git diff-index --quiet HEAD || git commit -m 'ci: auto-sync local changes' --quiet) && git push origin HEAD --quiet 2>/dev/null || true" | Out-Null

# 2. Sync parent repo
if (git status --porcelain) {
    Write-Host "  > Parent repo changes detected. Auto-committing..." -ForegroundColor DarkGray
    git add .
    git commit -m "ci: auto-sync before remote build" --quiet
}

# 3. Push parent repo
Write-Host "  > Pushing parent to origin $currentBranch..." -ForegroundColor DarkGray
git push origin $currentBranch --quiet
if ($LASTEXITCODE -ne 0) { 
    Write-Host "  ! Git push failed. Please check your internet or permissions." -ForegroundColor Yellow
}

# ---------------------------------------------------------------
# 1. Ensure GHCR authentication on remote server
# ---------------------------------------------------------------
Write-Host "`n[Phase 1] Authenticating..." -ForegroundColor Cyan
$ghToken = (gh auth token 2>$null)
if (-not $ghToken) {
    Write-Host "FATAL: Cannot get GitHub token. Run 'gh auth login' first." -ForegroundColor Red
    exit 1
}

# Login to GHCR on the remote server's Docker daemon via SSH
# (all docker build + push now runs there, not locally)
Write-Host "  > Logging in to GHCR on remote server..." -ForegroundColor DarkGray
$loginResult = echo $ghToken | ssh chernousov_a@100.86.137.112 "docker login ghcr.io -u datawikipro --password-stdin" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "FATAL: Remote Docker GHCR login failed: $loginResult" -ForegroundColor Red
    exit 1
}
Write-Host "  > Remote GHCR login: OK" -ForegroundColor DarkGray

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
$jvmServices = @("igaming-aggregator", "igaming-bot", "igaming-portal", "igaming-admin-backend")

$crawlerServices = Get-ChildItem -Path $rootDir -Directory -Filter "igaming-source-*" |
    Where-Object { $_.Name -ne "igaming-source-core" } |
    Select-Object -ExpandProperty Name

if ($Only -ne "") {
    # Filter to only the requested module
    $match = "igaming-source-$Only"
    if ($Only -in $jvmServices) { $match = $Only }
    $allModules = @($match)
    Write-Host "[Phase 2] Building single module: $match" -ForegroundColor Yellow
} else {
    $allModules = $jvmServices + $crawlerServices
    Write-Host "[Phase 2] Building $($allModules.Count) modules (parallel=$Parallel)..." -ForegroundColor Cyan
}

# ---------------------------------------------------------------
# 3. Build & Push Docker images (all via remote server SSH)
# ---------------------------------------------------------------
Write-Host ""
Write-Host "[Phase 3] Building and pushing images..." -ForegroundColor Cyan

$success = [System.Collections.Concurrent.ConcurrentBag[string]]::new()
$failed  = [System.Collections.Concurrent.ConcurrentBag[string]]::new()

$allModules | ForEach-Object {
    $module = $_
    $root = $rootDir

    try {
        # All modules: build on remote server via SSH, push from remote Docker daemon
        # This avoids slow local upload of blobs to GHCR
        $imageTag = "ghcr.io/datawikipro/${module}:latest"
        $remotePath = "build/igaming"
        $remoteCmd = "cd $remotePath && git fetch origin && git checkout $currentBranch && git pull origin $currentBranch && git submodule update --init --recursive && docker build -f $module/Dockerfile -t $imageTag . && docker push $imageTag"

        Write-Host "  > [$module] Building and pushing on remote server..." -ForegroundColor DarkGray
        ssh chernousov_a@100.86.137.112 $remoteCmd
        if ($LASTEXITCODE -ne 0) { throw "Remote build/push failed" }

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

$oldEap = $ErrorActionPreference
$ErrorActionPreference = 'SilentlyContinue'

foreach ($module in $success) {
    if ($module -like "igaming-source-*") {
        kubectl rollout restart deployment "$module-crawler" -n igaming-dev 2>$null | Out-Null
        kubectl rollout restart deployment "$module-loader"  -n igaming-dev 2>$null | Out-Null
    } else {
        kubectl rollout restart deployment $module -n igaming-dev 2>$null | Out-Null
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
