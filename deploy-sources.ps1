# deploy-sources.ps1
# Batch build and deploy script for SmartBet.guru bookmaker crawlers
# Usage: .\deploy-sources.ps1 -Modules "betman","winner","interwetten","bet9ja","veikkaus","iddaa","norsktipping","swisslos","tab","crickex"

param(
    [string[]]$Modules = @(
        "betman", "winner", "interwetten", "bet9ja", "veikkaus",
        "iddaa", "norsktipping", "swisslos", "tab", "crickex"
    ),
    [string]$Namespace = "igaming-source",
    [switch]$ApplyManifestsOnly,
    [switch]$SkipTests
)

$ErrorActionPreference = "Continue"
$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  SmartBet.guru — Batch Deploy National Champions Sources" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Target Modules: $($Modules -join ', ')" -ForegroundColor Yellow
Write-Host "Namespace:      $Namespace" -ForegroundColor Yellow
Write-Host ""

# 1. Check GHCR Authentication
if (-not $ApplyManifestsOnly) {
    Write-Host "[1/3] Checking GHCR credentials..." -ForegroundColor Cyan
    $ghToken = (gh auth token 2>$null)
    if (-not $ghToken) {
        Write-Host "WARNING: 'gh auth token' empty. Checking DOCKER_AUTH or environment..." -ForegroundColor Yellow
    } else {
        Write-Host "✓ GitHub authentication token detected" -ForegroundColor Green
    }
}

# 2. Build & Push via Maven Jib
if (-not $ApplyManifestsOnly) {
    Write-Host "`n[2/3] Building & pushing OCI container images via Jib..." -ForegroundColor Cyan
    foreach ($m in $Modules) {
        $modDir = "igaming-source-$m"
        $imageName = "ghcr.io/datawikipro/$modDir:latest"
        Write-Host "-> Building module: $modDir -> $imageName" -ForegroundColor White

        $jibArgs = @(
            "-pl", $modDir,
            "compile",
            "jib:build",
            "-Djib.to.image=$imageName"
        )
        if ($ghToken) {
            $jibArgs += "-Djib.to.auth.username=datawikipro"
            $jibArgs += "-Djib.to.auth.password=$ghToken"
        }
        if ($SkipTests -or $true) {
            $jibArgs += "-DskipTests"
        }

        & mvn.cmd @jibArgs
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ $modDir image pushed successfully" -ForegroundColor Green
        } else {
            Write-Host "  ✗ $modDir Jib build failed with exit code $LASTEXITCODE" -ForegroundColor Red
        }
    }
}

# 3. Apply Kubernetes manifests
Write-Host "`n[3/3] Applying Kubernetes manifests in namespace '$Namespace'..." -ForegroundColor Cyan
foreach ($m in $Modules) {
    $manifest = Join-Path $rootDir "igaming-k8s\$m.yaml"
    if (Test-Path $manifest) {
        Write-Host "-> Applying manifest: $m.yaml" -ForegroundColor White
        kubectl apply -f $manifest
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ $m manifests applied" -ForegroundColor Green
        } else {
            Write-Host "  ✗ Failed to apply $m.yaml" -ForegroundColor Red
        }
    } else {
        Write-Host "  ! Manifest not found: $manifest" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Deployment Batch Complete! Check pod status with:" -ForegroundColor Green
Write-Host "  kubectl get pods -n $Namespace" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan
