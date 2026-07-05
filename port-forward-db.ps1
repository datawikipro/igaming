$ErrorActionPreference = "Stop"

$namespace = "igaming-dev"
$service = "svc/portal-postgres"
$ports = "5432:5432"

Write-Host "Starting port-forwarding for $service on ports $ports in namespace $namespace" -ForegroundColor Green
Write-Host "Press Ctrl+C to stop." -ForegroundColor Yellow

while ($true) {
    try {
        kubectl port-forward $service $ports -n $namespace
    } catch {
        Write-Host "Port-forwarding process exited or failed." -ForegroundColor Red
    }
    
    Write-Host "Connection dropped. Restarting port-forward in 3 seconds..." -ForegroundColor Yellow
    Start-Sleep -Seconds 3
}
