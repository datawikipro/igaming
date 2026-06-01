# fix_s3dev_node_selectors.ps1
# DBs + stateful (PVC) -> master-vm
# Compute / stateless  -> node-type=standard

$env:KUBECONFIG = "C:\Users\chernousov_a\.kube\igaming-cluster.yaml"
$kubectl = "C:\Program Files\Lens\resources\x64\kubectl.exe"
$ns = "s3-dev"

function Patch-NodeSelector {
    param(
        [string]$kind,
        [string]$name,
        [string]$nodeType
    )
    Write-Host "Patching $kind $name -> node-type=$nodeType" -ForegroundColor Cyan
    $patch = "{`"spec`":{`"template`":{`"spec`":{`"nodeSelector`":{`"node-type`":`"$nodeType`"},`"affinity`":null}}}}"
    $tempFile = [System.IO.Path]::GetTempFileName()
    Set-Content -Path $tempFile -Value $patch
    & $kubectl patch $kind $name -n $ns --patch-file $tempFile
    Remove-Item $tempFile
}

Write-Host ""
Write-Host "=== DB + Stateful (PVC) -> master ===" -ForegroundColor Yellow

Patch-NodeSelector "statefulset" "airflow-postgresql"    "master"
Patch-NodeSelector "statefulset" "airflow-triggerer"     "master"
Patch-NodeSelector "statefulset" "superset-postgresql"   "master"
Patch-NodeSelector "statefulset" "superset-redis-master" "master"

Write-Host ""
Write-Host "=== Compute (stateless) -> standard ===" -ForegroundColor Yellow

Patch-NodeSelector "deployment" "trino-coordinator"     "standard"
Patch-NodeSelector "deployment" "trino-worker"          "standard"
Patch-NodeSelector "deployment" "airflow-scheduler"     "standard"
Patch-NodeSelector "deployment" "airflow-worker"        "standard"
Patch-NodeSelector "deployment" "airflow-api-server"    "standard"
Patch-NodeSelector "deployment" "airflow-dag-processor" "standard"
Patch-NodeSelector "deployment" "airflow-statsd"        "standard"
Patch-NodeSelector "deployment" "superset"              "standard"
Patch-NodeSelector "deployment" "superset-worker"       "standard"

Write-Host ""
Write-Host "Done! Waiting for pods..." -ForegroundColor Green
Start-Sleep -Seconds 8

Write-Host ""
Write-Host "=== Pod status s3-dev ===" -ForegroundColor Yellow
& $kubectl get pods -n $ns -o wide
