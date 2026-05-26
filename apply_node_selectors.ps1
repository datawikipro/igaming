# apply_node_selectors.ps1
# This script applies the target topology node selectors to all Deployments and StatefulSets.
# Strategy:
#   - master   : postgres, db, admin, ingress, cloudflare, auth
#   - stable   : aggregator, portal, all crawlers, all loaders, llm-workers (first pod guaranteed stable)
#   - spot     : everything else (smartbet, captures, proxy, etc.)

$env:KUBECONFIG = "C:\Users\chernousov_a\.kube\igaming-cluster.yaml"
$kubectl = "C:\Program Files\Lens\resources\x64\kubectl.exe"

# 1. Gather all deployments and statefulsets in all namespaces
$deployments = & $kubectl get deploy -A -o json | ConvertFrom-Json
$statefulsets = & $kubectl get sts -A -o json | ConvertFrom-Json

# Helper function to patch nodeSelector (without provider label)
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

    $targetNode = "spot" # Default

    if ($name -match "postgres|db|admin-backend|admin-frontend|admin-db|ingress|cloudflare-tunnel|igaming-auth-microservice|llm-admin|llm-frontend|llm-gateway") {
        $targetNode = "master"
    } elseif ($name -match "igaming-aggregator|igaming-portal") {
        $targetNode = "stable"
    } elseif ($name -match "-crawler|-loader") {
        $targetNode = "spot"
    } elseif ($name -match "llm-worker") {
        $targetNode = "stable"
    }
    # Everything else (smartbet-*, captures, proxy, etc.) stays on spot

    Patch-NodeSelector "deployment" $ns $name $targetNode
}

# Process StatefulSets
foreach ($sts in $statefulsets.items) {
    $ns = $sts.metadata.namespace
    $name = $sts.metadata.name

    # Skip kube-system
    if ($ns -eq "kube-system") { continue }

    $targetNode = "master" # StatefulSets (DBs) stay on master by default

    if ($name -match "llm-worker") {
        $targetNode = "stable"
    }

    Patch-NodeSelector "statefulset" $ns $name $targetNode
}

Write-Host ""
Write-Host "Topology nodeSelectors applied successfully." -ForegroundColor Green
