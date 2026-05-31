# rebuild-all-loaders.ps1
$modules = @("zenit", "betcity", "olimpbet", "marathonbet", "fon-bet-ru", "leon", "tennisi", "winline", "sportbet", "melbet", "ligastavok", "betboom", "baltbet", "xbet", "pinnacle")
foreach ($m in $modules) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host "=== Rebuilding and Restarting $m ===" -ForegroundColor Magenta
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host ""
    .\restart-ci.ps1 -Only $m
}
