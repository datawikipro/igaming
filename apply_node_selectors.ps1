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
        [string]$nodeType,
        [string]$provider = ""
    )
    Write-Host "Patching $kind $namespace/$name -> node-type=$nodeType"
    if ($provider -ne "") {
        $patch = "{`"spec`":{`"template`":{`"spec`":{`"nodeSelector`":{`"node-type`":`"$nodeType`",`"provider`":`"$provider`"}}}}}"
    } else {
        $patch = "{`"spec`":{`"template`":{`"spec`":{`"nodeSelector`":{`"node-type`":`"$nodeType`"}}}}}"
    }
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
    $provider = ""
    
    if ($name -match "postgres|db|admin|ingress|llm-frontend|llm-gateway") {
        $targetNode = "master"
    } elseif ($name -eq "igaming-aggregator") {
        $targetNode = "stable"
        $provider = "gcp"
    } elseif ($name -match "llm-worker") {
        $targetNode = "stable"
    }

    Patch-NodeSelector "deployment" $ns $name $targetNode $provider
}

# Process StatefulSets
foreach ($sts in $statefulsets.items) {
    $ns = $sts.metadata.namespace
    $name = $sts.metadata.name

    # Skip kube-system
    if ($ns -eq "kube-system") { continue }

    $targetNode = "spot" # Default
    $provider = ""
    
    if ($name -match "postgres|db|admin|ingress|llm-frontend|llm-gateway") {
        $targetNode = "master"
    } elseif ($name -eq "igaming-aggregator") {
        $targetNode = "stable"
        $provider = "gcp"
    } elseif ($name -match "llm-worker") {
        $targetNode = "stable"
    }

    Patch-NodeSelector "statefulset" $ns $name $targetNode $provider
}

Write-Host "Topology nodeSelectors applied successfully."
