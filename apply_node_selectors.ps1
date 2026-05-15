# apply_node_selectors.ps1
# This script applies the target topology node selectors to all Deployments and StatefulSets.

$env:KUBECONFIG = "C:\Users\chernousov_a\.kube\igaming-cluster.yaml"
$kubectl = "C:\Program Files\Lens\resources\x64\kubectl.exe"

# 1. Gather all deployments and statefulsets in all namespaces
$deployments = & $kubectl get deploy -A -o json | ConvertFrom-Json
$statefulsets = & $kubectl get sts -A -o json | ConvertFrom-Json

# Helper function to patch nodeSelector
function Patch-NodeSelector {
    param(
        [string]$kind,
        [string]$namespace,
        [string]$name,
        [string]$nodeType
    )
    Write-Host "Patching $kind $namespace/$name -> node-type=$nodeType"
    $patch = "{`"spec`":{`"template`":{`"spec`":{`"nodeSelector`":{`"node-type`":`"$nodeType`"}}}}}"
    $tempFile = [System.IO.Path]::GetTempFileName()
    Set-Content -Path $tempFile -Value $patch
    & $kubectl patch $kind $name -n $namespace --patch-file $tempFile
    Remove-Item $tempFile
}

# Process Deployments
foreach ($dep in $deployments.items) {
    $ns = $dep.metadata.namespace
    $name = $dep.metadata.name
    
    # Skip kube-system
    if ($ns -eq "kube-system") { continue }

    $targetNode = "spot" # Default for Java apps
    
    if ($name -match "postgres|db|admin") {
        $targetNode = "master"
    } elseif ($name -eq "igaming-aggregator") {
        $targetNode = "worker"
    }

    Patch-NodeSelector "deployment" $ns $name $targetNode
}

# Process StatefulSets
foreach ($sts in $statefulsets.items) {
    $ns = $sts.metadata.namespace
    $name = $sts.metadata.name

    # Skip kube-system
    if ($ns -eq "kube-system") { continue }

    $targetNode = "spot" # Default
    
    if ($name -match "postgres|db|admin") {
        $targetNode = "master"
    } elseif ($name -eq "igaming-aggregator") {
        $targetNode = "worker"
    }

    Patch-NodeSelector "statefulset" $ns $name $targetNode
}

Write-Host "Topology nodeSelectors applied successfully."
