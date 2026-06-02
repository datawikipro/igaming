# rebuild-remaining.ps1
$modules = @("marathonbet", "fon-bet-ru", "leon", "tennisi", "winline", "sportbet", "betb2b", "ligastavok", "betboom", "baltbet", "pinnacle")
foreach ($m in $modules) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host "=== Rebuilding and Restarting $m ===" -ForegroundColor Magenta
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host ""
    .\restart-ci.ps1 -Only $m
}
