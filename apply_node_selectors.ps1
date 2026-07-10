# apply_node_selectors.ps1
# This script applies the target topology node selectors to all Deployments and StatefulSets.
# Strategy:
#   - master   : postgres, db, admin, ingress, cloudflare, auth
#   - stable   : aggregator, portal, all crawlers, all loaders, llm-workers (first pod guaranteed stable)
#   - spot     : everything else (smartbet, captures, proxy, etc.)

$env:KUBECONFIG = "C:\Users\chernousov_a\.kube\config"
$kubectl = "C:\Program Files\Lens\resources\x64\kubectl.exe"

# 1. Gather all deployments and statefulsets in all namespaces
$deployments = & $kubectl get deploy -A -o json | ConvertFrom-Json
$statefulsets = & $kubectl get sts -A -o json | ConvertFrom-Json

# Helper function to patch nodeSelector (without provider label)
function Patch-NodeSelector {
    param(
        [string]$kind,
        [string]$namespace,
        [string]$name,
        [string]$nodeType
    )
    Write-Host "Patching $kind $namespace/$name -> node-type=$nodeType"
    if ($nodeType -ne "master") {
        $patch = "{`"spec`":{`"template`":{`"spec`":{`"nodeSelector`":{`"node-type`":`"$nodeType`"},`"affinity`":null}}}}"
    } else {
        $patch = "{`"spec`":{`"template`":{`"spec`":{`"nodeSelector`":{`"node-type`":`"$nodeType`"}}}}}"
    }
    $tempFile = [System.IO.Path]::GetTempFileName()
    Set-Content -Path $tempFile -Value $patch
    & $kubectl patch $kind $name -n $namespace --patch-file $tempFile
    Remove-Item $tempFile
}

function Patch-CrawlerLoader {
    param(
        [string]$kind,
        [string]$namespace,
        [string]$name
    )
    Write-Host "Patching $kind $namespace/$name -> Split-Scheduling (1 stable, rest spot)"
    $patch = @{
        spec = @{
            template = @{
                spec = @{
                    nodeSelector = @{
                        # Remove node-type from nodeSelector by not specifying it,
                        # and explicitly set it to null in the JSON payload to clear existing values.
                    }
                    affinity = @{
                        nodeAffinity = @{
                            requiredDuringSchedulingIgnoredDuringExecution = @{
                                nodeSelectorTerms = @(
                                    @{
                                        matchExpressions = @(
                                            @{
                                                key = "node-type"
                                                operator = "In"
                                                values = @("standard", "stable", "spot")
                                            }
                                        )
                                    }
                                )
                            }
                            preferredDuringSchedulingIgnoredDuringExecution = @(
                                @{
                                    weight = 100
                                    preference = @{
                                        matchExpressions = @(
                                            @{
                                                key = "node-type"
                                                operator = "In"
                                                values = @("standard", "stable")
                                            }
                                        )
                                    }
                                },
                                @{
                                    weight = 10
                                    preference = @{
                                        matchExpressions = @(
                                            @{
                                                key = "node-type"
                                                operator = "In"
                                                values = @("spot")
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                    topologySpreadConstraints = @(
                        @{
                            maxSkew = 1
                            topologyKey = "node-type"
                            whenUnsatisfiable = "ScheduleAnyway"
                            labelSelector = @{
                                matchLabels = @{
                                    app = $name
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    
    # We must explicitly set spec.template.spec.nodeSelector to null to clear existing selection.
    # ConvertTo-Json doesn't represent $null inside a hashtable properly as null sometimes,
    # so we will construct the final json string with nodeSelector set to null.
    $jsonObj = ConvertTo-Json $patch -Depth 20
    $jsonObj = $jsonObj -replace '"nodeSelector":\s*\{\s*\}', '"nodeSelector": null'

    $tempFile = [System.IO.Path]::GetTempFileName()
    Set-Content -Path $tempFile -Value $jsonObj
    & $kubectl patch $kind $name -n $namespace --patch-file $tempFile
    Remove-Item $tempFile
}

# Process Deployments
foreach ($dep in $deployments.items) {
    $ns = $dep.metadata.namespace
    $name = $dep.metadata.name

    # Skip kube-system
    if ($ns -eq "kube-system") { continue }

    if ($name -match "-crawler|-loader|llm-worker") {
        Patch-CrawlerLoader "deployment" $ns $name
        continue
    }

    if ($name -match "proxy-vpn-pool") {
        # Do not apply any nodeSelector to proxy-vpn-pool as it runs on role=proxy VPN nodes
        continue
    }

    $targetNode = "standard" # Default

    if ($name -match "postgres|db|admin-backend|admin-frontend|admin-db|ingress|cloudflare-tunnel|igaming-auth-microservice|llm-admin|llm-frontend|llm-gateway|kafka|aggregator") {
        $targetNode = "master"
    } elseif ($name -match "igaming-portal|service-proxy-backend") {
        $targetNode = "standard"
    }
    # Everything else (smartbet-*, captures, proxy, etc.) stays on spot

    Patch-NodeSelector "deployment" $ns $name $targetNode
}

# Process StatefulSets
foreach ($sts in $statefulsets.items) {
    $ns = $sts.metadata.namespace
    $name = $sts.metadata.name

    # Skip kube-system
    if ($ns -eq "kube-system") { continue }

    if ($name -match "llm-worker") {
        Patch-CrawlerLoader "statefulset" $ns $name
        continue
    }

    $targetNode = "standard" # Default: StatefulSets (DBs) move to stable/standard nodes
    if ($name -match "postgres|db|admin-db") {
        # Keep central databases on master, but bookmaker DBs (igaming-source-*-db) go to stable/standard nodes
        if ($name -notmatch "igaming-source-[a-z0-9-]+-db" -or $name -eq "igaming-sources-db") {
            $targetNode = "master"
        }
    }

    Patch-NodeSelector "statefulset" $ns $name $targetNode
}

Write-Host ""
Write-Host "Topology nodeSelectors applied successfully." -ForegroundColor Green
