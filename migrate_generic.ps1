param(
    [string]$pvName,
    [string]$newNode
)

Write-Host "Migrating PV $pvName to $newNode..."

# Get PVC name and namespace
$pvcName = (& "C:\Program Files\Lens\resources\x64\kubectl.exe" get pv $pvName -o jsonpath='{.spec.claimRef.name}')
$pvcNamespace = (& "C:\Program Files\Lens\resources\x64\kubectl.exe" get pv $pvName -o jsonpath='{.spec.claimRef.namespace}')

if (-not $pvcName -or -not $pvcNamespace) {
    Write-Host "PVC details not found for PV $pvName"
    exit 1
}

Write-Host "Found PVC: $pvcName in namespace $pvcNamespace"

# Find if it belongs to a StatefulSet
$stsName = ""
if ($pvcName -match "^(?:pgdata|postgres-data)-(.*?)-([0-9]+)$") {
    $stsName = $matches[1]
    # Check if STS exists
    $stsExists = & "C:\Program Files\Lens\resources\x64\kubectl.exe" get sts $stsName -n $pvcNamespace --ignore-not-found
    if ($stsExists) {
        Write-Host "Scaling down StatefulSet $stsName..."
        & "C:\Program Files\Lens\resources\x64\kubectl.exe" scale sts $stsName --replicas=0 -n $pvcNamespace
        Start-Sleep -Seconds 5
    }
}

# Find if it belongs to a Deployment
$deployName = ""
if (-not $stsExists) {
    # If not an STS, try finding a pod using it
    $podNames = (& "C:\Program Files\Lens\resources\x64\kubectl.exe" get pods -n $pvcNamespace -o jsonpath="{range .items[?(@.spec.volumes[*].persistentVolumeClaim.claimName=='$pvcName')]}{.metadata.name}{'\n'}{end}")
    if ($podNames) {
        Write-Host "Pods using PVC: $podNames. Deleting them..."
        foreach ($pod in ($podNames -split "`n" | Where-Object {$_})) {
            & "C:\Program Files\Lens\resources\x64\kubectl.exe" delete pod $pod -n $pvcNamespace --force
        }
    }
}

# 3. Retain
Write-Host "Setting Retain on $pvName..."
& "C:\Program Files\Lens\resources\x64\kubectl.exe" patch pv $pvName -p '{\"spec\":{\"persistentVolumeReclaimPolicy\":\"Retain\"}}'

# 4. Dump and prepare PV json
Write-Host "Preparing new PV spec..."
$json = & "C:\Program Files\Lens\resources\x64\kubectl.exe" get pv $pvName -o json | ConvertFrom-Json
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
& "C:\Program Files\Lens\resources\x64\kubectl.exe" delete pvc $pvcName -n $pvcNamespace
Write-Host "Deleting PV $pvName..."
& "C:\Program Files\Lens\resources\x64\kubectl.exe" delete pv $pvName

# 6. Recreate PV
Write-Host "Recreating PV $pvName..."
& "C:\Program Files\Lens\resources\x64\kubectl.exe" apply -f "$pvName.json"

# 7. Scale up
if ($stsExists) {
    Write-Host "Scaling up StatefulSet $stsName..."
    & "C:\Program Files\Lens\resources\x64\kubectl.exe" scale sts $stsName --replicas=1 -n $pvcNamespace
} else {
    Write-Host "Note: Deployment/Pod needs to be scaled up or recreated manually if applicable."
}
Write-Host "Done migrating $pvName."
