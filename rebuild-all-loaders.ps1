# rebuild-all-loaders.ps1
$modules = @("zenit", "betcity", "bettery", "pari", "olimpbet", "marathonbet", "fon-bet-ru", "leon", "tennisi", "winline", "fon-bet-kz", "fon-bet-by", "olimpbet-kz", "sportbet", "melbet", "ligastavok", "betboom", "baltbet", "betm")
foreach ($m in $modules) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host "=== Rebuilding and Restarting $m ===" -ForegroundColor Magenta
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host ""
    .\restart-ci.ps1 -Only $m
}
