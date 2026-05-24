param(
    [string]$dbName,
    [string]$newNode
)

$env:KUBECONFIG = "C:\Users\chernousov_a\.kube\igaming-cluster.yaml"
$kubectl = "C:\Program Files\Lens\resources\x64\kubectl.exe"

Write-Host "Migrating $dbName to $newNode..."

$pvcName = ""
if ($dbName -eq "leon") {
    $pvcName = "postgres-data-igaming-source-leon-db-0"
} else {
    $pvcName = "pgdata-igaming-source-$dbName-db-0"
}
$stsName = "igaming-source-$dbName-db"

# 1. Scale down
Write-Host "Scaling down $stsName..."
& $kubectl scale sts $stsName --replicas=0 -n igaming-dev
Start-Sleep -Seconds 5

# 2. Get PV
$pvName = (& $kubectl get pvc $pvcName -n igaming-dev -o jsonpath='{.spec.volumeName}')
if (-not $pvName) {
    Write-Host "PV not found for $pvcName"
    exit 1
}

# 3. Retain
Write-Host "Setting Retain on $pvName..."
& $kubectl patch pv $pvName -p '{\"spec\":{\"persistentVolumeReclaimPolicy\":\"Retain\"}}'

# 4. Dump and prepare PV json
Write-Host "Preparing new PV spec..."
$json = & $kubectl get pv $pvName -o json | ConvertFrom-Json
$json.metadata.PSObject.Properties.Remove("creationTimestamp")
$json.metadata.PSObject.Properties.Remove("resourceVersion")
$json.metadata.PSObject.Properties.Remove("uid")
if ($json.metadata.managedFields) {
    $json.metadata.PSObject.Properties.Remove("managedFields")
}
if ($json.spec.claimRef) {
    $json.spec.claimRef.PSObject.Properties.Remove("uid")
    $json.spec.claimRef.PSObject.Properties.Remove("resourceVersion")
}
$json.spec.nodeAffinity.required.nodeSelectorTerms[0].matchExpressions[0].values[0] = $newNode

$json | ConvertTo-Json -Depth 10 | Out-File "$pvName.json"

# 5. Delete PVC and PV
Write-Host "Deleting PVC $pvcName..."
& $kubectl delete pvc $pvcName -n igaming-dev
Write-Host "Deleting PV $pvName..."
& $kubectl delete pv $pvName

# 6. Recreate PV
Write-Host "Recreating PV $pvName..."
& $kubectl apply -f "$pvName.json"

# 7. Scale up
Write-Host "Scaling up $stsName..."
& $kubectl scale sts $stsName --replicas=1 -n igaming-dev
Write-Host "Done migrating $dbName."
