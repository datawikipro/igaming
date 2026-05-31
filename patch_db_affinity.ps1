$namespace = "igaming-dev"

# Affinity patch (merge) - adds affinity block
$affinityPatch = @'
{
  "spec": {
    "template": {
      "spec": {
        "affinity": {
          "nodeAffinity": {
            "preferredDuringSchedulingIgnoredDuringExecution": [
              {
                "weight": 10,
                "preference": {
                  "matchExpressions": [
                    { "key": "node-type", "operator": "In", "values": ["standard"] }
                  ]
                }
              }
            ],
            "requiredDuringSchedulingIgnoredDuringExecution": {
              "nodeSelectorTerms": [
                {
                  "matchExpressions": [
                    { "key": "node-type", "operator": "In", "values": ["spot", "standard"] }
                  ]
                }
              ]
            }
          }
        }
      }
    }
  }
}
'@

# nodeSelector removal patch (merge) - sets nodeSelector to null removes it
$removeSelectorPatch = @'
{
  "spec": {
    "template": {
      "spec": {
        "nodeSelector": null
      }
    }
  }
}
'@

# All source db StatefulSets to patch
$statefulsets = @(
  "igaming-source-1xbet-db",
  "igaming-source-888starz-db",
  "igaming-source-baltbet-db",
  "igaming-source-betandyou-db",
  "igaming-source-betboom-db",
  "igaming-source-betcity-com-db",
  "igaming-source-betcity-db",
  "igaming-source-betm-db",
  "igaming-source-bettery-db",
  "igaming-source-fansport-db",
  "igaming-source-fon-bet-by-db",
  "igaming-source-fon-bet-kz-db",
  "igaming-source-fon-bet-ru-db",
  "igaming-source-leon-db",
  "igaming-source-ligastavok-db",
  "igaming-source-linebet-db",
  "igaming-source-marathonbet-by-db",
  "igaming-source-marathonbet-com-db",
  "igaming-source-marathonbet-db",
  "igaming-source-megapari-db",
  "igaming-source-melbet-db",
  "igaming-source-olimpbet-db",
  "igaming-source-olimpbet-kz-db",
  "igaming-source-pari-db",
  "igaming-source-pinnacle-db",
  "igaming-source-sportbet-db",
  "igaming-source-tennisi-db",
  "igaming-source-winline-db",
  "igaming-source-zenit-db"
)

# StatefulSets that currently have nodeSelector: master (need removal)
$hasMasterSelector = @(
  "igaming-source-marathonbet-by-db",
  "igaming-source-marathonbet-com-db",
  "igaming-source-pinnacle-db"
)

$affinityFile  = [System.IO.Path]::GetTempFileName()
$selectorFile  = [System.IO.Path]::GetTempFileName()
$affinityPatch  | Set-Content $affinityFile  -Encoding UTF8
$removeSelectorPatch | Set-Content $selectorFile -Encoding UTF8

foreach ($sts in $statefulsets) {
  Write-Host "Patching $sts ..." -NoNewline

  # Remove nodeSelector if it was master
  if ($hasMasterSelector -contains $sts) {
    kubectl patch statefulset $sts -n $namespace --type=merge --patch-file $selectorFile 2>&1 | Out-Null
  }

  # Add affinity
  $result = kubectl patch statefulset $sts -n $namespace --type=merge --patch-file $affinityFile 2>&1
  if ($LASTEXITCODE -eq 0) {
    Write-Host " OK"
  } else {
    Write-Host " FAILED: $result"
  }
}

Remove-Item $affinityFile, $selectorFile -ErrorAction SilentlyContinue
Write-Host "`nDone. Checking pod status..."
kubectl get pods -n $namespace -o wide | Select-String "db-0"
