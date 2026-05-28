# rebuild-all-loaders.ps1
$modules = @("winline", "zenit", "betcity", "bettery", "pari", "olimpbet", "marathonbet", "fon-bet-ru")
foreach ($m in $modules) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host "=== Rebuilding and Restarting $m ===" -ForegroundColor Magenta
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host ""
    .\restart-ci.ps1 -Only $m
}
