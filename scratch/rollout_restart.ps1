#!/usr/bin/env pwsh
# Force rollout restart for all igaming-source-* deployments
# This recreates pods with the new lower CPU requests

$namespace = "igaming-dev"

$deployments = kubectl get deployments -n $namespace -o jsonpath='{.items[*].metadata.name}' 2>&1
$deploymentList = ($deployments -split ' ') | Where-Object { $_ -match "igaming-source" }

Write-Host "Restarting $($deploymentList.Count) igaming-source deployments..."

foreach ($dep in $deploymentList) {
    kubectl rollout restart deployment/$dep -n $namespace 2>&1 | Out-Null
    Write-Host "  restarted: $dep"
}

# Also restart portal
kubectl rollout restart deployment/igaming-portal -n $namespace 2>&1
Write-Host "Portal restarted."
Write-Host "All restarts triggered."
