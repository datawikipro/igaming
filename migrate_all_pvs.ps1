# migrate_all_pvs.ps1
# Migrates all PVs still bound to dead nodes to master-vm

$env:KUBECONFIG = "C:\Users\chernousov_a\.kube\igaming-cluster.yaml"
$kubectl = "C:\Program Files\Lens\resources\x64\kubectl.exe"
$targetNode = "master-vm"

$validNodes = @("master-vm") # Currently only master exists

$pvs = & $kubectl get pv -o json | ConvertFrom-Json

foreach ($pv in $pvs.items) {
    $pvName = $pv.metadata.name
    $currentNode = $pv.spec.nodeAffinity.required.nodeSelectorTerms[0].matchExpressions[0].values[0]

    if ($validNodes -contains $currentNode) {
        Write-Host "[$pvName] OK -> $currentNode (skipping)" -ForegroundColor DarkGray
        continue
    }

    Write-Host "[$pvName] Needs migration: $currentNode -> $targetNode" -ForegroundColor Yellow
    & ".\migrate_generic.ps1" -pvName $pvName -newNode $targetNode
    Write-Host ""
}

Write-Host "All stale PVs migrated." -ForegroundColor Green
