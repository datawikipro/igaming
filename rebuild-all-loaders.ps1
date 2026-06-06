# rebuild-all-loaders.ps1
$modules = @("zenit", "betcity", "olimpbet", "marathonbet", "fon-bet-ru", "leon", "tennisi", "winline", "sportbet", "betb2b", "ligastavok", "betboom", "baltbet", "pinnacle", "sbobet", "unibet")
foreach ($m in $modules) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host "=== Rebuilding and Restarting $m ===" -ForegroundColor Magenta
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host ""
    .\restart-ci.ps1 -Only $m
}
