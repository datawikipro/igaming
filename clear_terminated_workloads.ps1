# clear_terminated_workloads.ps1
# This script cleans up all dead K8s nodes (NotReady status) and force-deletes all Terminating pods to reclaim cluster state.

$env:KUBECONFIG = "C:\Users\chernousov_a\.kube\igaming-cluster.yaml"
$kubectl = "C:\Program Files\Lens\resources\x64\kubectl.exe"

Write-Host "=== Starting K8s Maintenance & Cleanup ===" -ForegroundColor Cyan

# 1. Clear dead NotReady nodes
Write-Host "`n[Phase 1] Searching for dead nodes (NotReady status)..." -ForegroundColor Yellow
$nodes = & $kubectl get nodes -o json | ConvertFrom-Json
$deadNodesCount = 0

foreach ($node in $nodes.items) {
    $nodeName = $node.metadata.name
    $status = "Ready"
    foreach ($cond in $node.status.conditions) {
        if ($cond.type -eq "Ready") {
            $status = $cond.status
        }
    }
    
    # If not ready, delete node resource
    if ($status -ne "True" -and $nodeName -ne "master-vm") {
        Write-Host "  > Node $nodeName is NotReady. Deleting..." -ForegroundColor Red
        & $kubectl delete node $nodeName | Out-Null
        $deadNodesCount++
    }
}
Write-Host "Cleared $deadNodesCount dead nodes." -ForegroundColor Green

# 2. Force delete Terminating pods
Write-Host "`n[Phase 2] Searching for stuck Terminating pods in all namespaces..." -ForegroundColor Yellow
$pods = & $kubectl get pods -A -o json | ConvertFrom-Json
$deletedPodsCount = 0

foreach ($pod in $pods.items) {
    $ns = $pod.metadata.namespace
    $name = $pod.metadata.name
    $deletionTimestamp = $pod.metadata.deletionTimestamp
    
    if ($deletionTimestamp) {
        Write-Host "  > Pod $ns/$name is stuck in Terminating. Force deleting..." -ForegroundColor Red
        & $kubectl delete pod $name -n $ns --force --grace-period=0 2>$null | Out-Null
        $deletedPodsCount++
    }
}

Write-Host "Force deleted $deletedPodsCount terminating pods." -ForegroundColor Green
Write-Host "`n=== Cleanup Completed Successfully ===" -ForegroundColor Cyan
