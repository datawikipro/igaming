$namespace = "igaming-dev"

# Remove affinity patch (revert to no affinity)
$removeAffinityPatch = @'
{
  "spec": {
    "template": {
      "spec": {
        "affinity": null
      }
    }
  }
}
'@

# These StatefulSets have PVs on master-vm - must revert to allow master
$masterPVStatefulSets = @(
  "igaming-source-sportbet-db",
  "igaming-source-fon-bet-ru-db",
  "igaming-source-betm-db",
  "igaming-source-olimpbet-db",
  "igaming-source-winline-db",
  "igaming-source-melbet-db",
  "igaming-source-zenit-db",
  "igaming-source-fansport-db",
  "igaming-source-bettery-db",
  "igaming-source-olimpbet-kz-db",
  "igaming-source-betcity-db",
  "igaming-source-tennisi-db",
  "igaming-source-fon-bet-by-db",
  "igaming-source-megapari-db",
  "igaming-source-linebet-db",
  "igaming-source-betandyou-db",
  "igaming-source-baltbet-db",
  "igaming-source-marathonbet-db",
  "igaming-source-888starz-db",
  "igaming-source-ligastavok-db",
  "igaming-source-fon-bet-kz-db",
  "igaming-source-pari-db",
  "igaming-source-betboom-db",
  "igaming-source-leon-db"
)

$patchFile = [System.IO.Path]::GetTempFileName()
$removeAffinityPatch | Set-Content $patchFile -Encoding UTF8

Write-Host "=== Reverting affinity for master-vm PV StatefulSets ==="
foreach ($sts in $masterPVStatefulSets) {
  Write-Host "Reverting $sts ..." -NoNewline
  $result = kubectl patch statefulset $sts -n $namespace --type=merge --patch-file $patchFile 2>&1
  if ($LASTEXITCODE -eq 0) { Write-Host " OK" } else { Write-Host " FAILED: $result" }
}

Remove-Item $patchFile -ErrorAction SilentlyContinue

Write-Host "`n=== Status of standard-node DB pods (should schedule on std-gcp-a8502514) ==="
Start-Sleep -Seconds 5
kubectl get pods -n $namespace | Select-String "pinnacle-db|marathonbet-com-db|marathonbet-by-db|betcity-com-db|1xbet-db"

Write-Host "`n=== Status of reverted master-vm DB pods ==="
kubectl get pods -n $namespace | Select-String "sportbet-db|fon-bet-ru-db|betm-db|olimpbet-db|winline-db|melbet-db|zenit-db|fansport-db|bettery-db|olimpbet-kz|betcity-db|tennisi|fon-bet-by|megapari|linebet|betandyou|baltbet|marathonbet-db|888starz|ligastavok|fon-bet-kz|pari-db|betboom|leon-db"
