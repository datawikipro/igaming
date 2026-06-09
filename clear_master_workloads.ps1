# clear_master_workloads.ps1
# This script scales non-master Deployments down to 0 and back up to force K8s to kill old pods remaining on the master node due to rolling updates.

$env:KUBECONFIG = "C:\Users\chernousov_a\.kube\config"
$kubectl = "C:\Program Files\Lens\resources\x64\kubectl.exe"

$deployments = & $kubectl get deploy -A -o json | ConvertFrom-Json

foreach ($dep in $deployments.items) {
    $ns = $dep.metadata.namespace
    $name = $dep.metadata.name
    
    # Skip kube-system and master workloads
    if ($ns -eq "kube-system") { continue }
    if ($name -match "postgres|db|admin|kafka") { continue }
    
    Write-Host "Scaling down $name in $ns..."
    & $kubectl scale deploy $name --replicas=0 -n $ns
}

Start-Sleep -Seconds 5

foreach ($dep in $deployments.items) {
    $ns = $dep.metadata.namespace
    $name = $dep.metadata.name
    
    if ($ns -eq "kube-system") { continue }
    if ($name -match "postgres|db|admin|kafka") { continue }
    
    Write-Host "Scaling up $name in $ns..."
    & $kubectl scale deploy $name --replicas=1 -n $ns
}

Write-Host "Master node cleared of spot/worker workloads."
