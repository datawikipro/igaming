# tunnel-k8s.ps1
# Поднимает SSH-туннель к K8s API на порту 16443 (чтобы не конфликтовать с основным на 6443)
# Позволяет Antigravity самостоятельно работать с kubectl без зависимости от твоего SSH-сессии.
#
# Usage: .\tunnel-k8s.ps1          — запустить туннель в фоне
#        .\tunnel-k8s.ps1 -Stop    — остановить туннель

param([switch]$Stop)

$SshHost    = "34.51.180.33"
$SshUser    = "Aleksei"
$SshKey     = "C:\Users\chernousov_a\.ssh\google_compute_engine"
$LocalPort  = 16443
$RemotePort = 6443
$PidFile    = "$env:TEMP\k8s-tunnel.pid"

if ($Stop) {
    if (Test-Path $PidFile) {
        $pid = Get-Content $PidFile
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        Remove-Item $PidFile
        Write-Host "K8s tunnel stopped." -ForegroundColor Yellow
    } else {
        Write-Host "No tunnel PID file found." -ForegroundColor Gray
    }
    exit 0
}

# Проверяем, уже ли запущен
if (Test-Path $PidFile) {
    $existingPid = Get-Content $PidFile
    if (Get-Process -Id $existingPid -ErrorAction SilentlyContinue) {
        Write-Host "Tunnel already running (PID $existingPid) on localhost:$LocalPort" -ForegroundColor Green
        exit 0
    }
    Remove-Item $PidFile
}

Write-Host "Starting K8s SSH tunnel: localhost:$LocalPort -> $SshHost`:$RemotePort" -ForegroundColor Cyan

$proc = Start-Process -NoNewWindow -FilePath "ssh" -ArgumentList @(
    "-N",
    "-L", "${LocalPort}:127.0.0.1:${RemotePort}",
    "-i", $SshKey,
    "-o", "ServerAliveInterval=30",
    "-o", "ServerAliveCountMax=10",
    "-o", "ExitOnForwardFailure=yes",
    "-o", "StrictHostKeyChecking=no",
    "${SshUser}@${SshHost}"
) -PassThru

$proc.Id | Out-File $PidFile
Write-Host "Tunnel started (PID $($proc.Id)). kubectl via localhost:$LocalPort" -ForegroundColor Green
Write-Host ""
Write-Host "To use with kubectl, run:" -ForegroundColor Yellow
Write-Host "  kubectl config set-cluster default --server=https://127.0.0.1:$LocalPort" -ForegroundColor White
