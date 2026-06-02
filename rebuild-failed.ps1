# rebuild-failed.ps1
$modules = @("marathonbet", "winline", "sportbet")
foreach ($m in $modules) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host "=== Rebuilding and Restarting $m ===" -ForegroundColor Magenta
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host ""
    .\restart-ci.ps1 -Only $m
}
