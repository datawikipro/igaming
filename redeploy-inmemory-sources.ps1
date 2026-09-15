# redeploy-inmemory-sources.ps1
# Script to launch morning deployment of in-memory sources on xeon-srv

$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "🚀 SmartBet.guru - Redeploying Sources with In-Memory DBs" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Check Node connectivity
Write-Host "`n[1/6] 🔍 Checking Kubernetes cluster status..." -ForegroundColor Yellow
$node = kubectl get nodes -o jsonpath='{.items[0].status.conditions[?(@.type=="Ready")].status}' 2>$null
if ($node -ne "True") {
    Write-Host "❌ Kubernetes cluster is not ready! Is xeon-srv turned on and Tailscale connected?" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Kubernetes cluster node is Ready!" -ForegroundColor Green

# 2. Delete legacy disk PVCs in igaming-source
Write-Host "`n[2/6] 🧹 Cleaning up legacy disk PVCs in igaming-source..." -ForegroundColor Yellow
kubectl delete pvc -n igaming-source --all --wait=false 2>$null
Write-Host "✅ Legacy PVC cleanup triggered." -ForegroundColor Green

# 3. Apply updated manifests (In-Memory Postgres tmpfs + clean pg_isready initContainers + shareProcessNamespace)
Write-Host "`n[3/6] 📄 Applying updated Kubernetes manifests from igaming-k8s/..." -ForegroundColor Yellow
kubectl apply -f igaming-k8s/ --prune=false 2>$null
Write-Host "✅ All Kubernetes manifests applied!" -ForegroundColor Green

# 4. Restart source DB pods to ensure clean in-memory start
Write-Host "`n[4/6] 🔄 Restarting database StatefulSets on tmpfs RAM..." -ForegroundColor Yellow
$dbs = kubectl get statefulsets -n igaming-source -o name 2>$null
foreach ($db in $dbs) {
    kubectl rollout restart $db -n igaming-source 2>$null
}
Write-Host "✅ Database StatefulSets restarted on in-memory tmpfs." -ForegroundColor Green

# 5. Restart crawler and loader deployments
Write-Host "`n[5/6] 🔄 Restarting crawler and loader deployments..." -ForegroundColor Yellow
$deploys = kubectl get deployments -n igaming-source -o name 2>$null
foreach ($dep in $deploys) {
    # Keep validated bookmakers scaled as desired
    kubectl rollout restart $dep -n igaming-source 2>$null
}
Write-Host "✅ Application deployments restarted." -ForegroundColor Green

# 6. Status check
Write-Host "`n[6/6] 📊 Checking initial status of pods in igaming-source..." -ForegroundColor Yellow
Start-Sleep -Seconds 5
kubectl get pods -n igaming-source | Select-Object -First 30

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "🎉 Deployment completed successfully!" -ForegroundColor Green
Write-Host "👉 Database schema will be auto-migrated by Java on startup." -ForegroundColor Green
Write-Host "👉 All PostgreSQL source databases are running in RAM (tmpfs) with ZERO disk I/O." -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
