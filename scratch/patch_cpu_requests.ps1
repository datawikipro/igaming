#!/usr/bin/env pwsh
# Patch all igaming-dev deployments to reduce CPU requests so pods can schedule
# Sets cpu requests to 50m for crawlers/loaders (they barely use CPU at idle)

$namespace = "igaming-dev"
$patch = '{"spec":{"template":{"spec":{"containers":[{"name":"crawler","resources":{"requests":{"cpu":"50m","memory":"256Mi"}}},{"name":"loader","resources":{"requests":{"cpu":"50m","memory":"256Mi"}}}]}}}}'

Write-Host "Fetching all deployments in $namespace..."
$deployments = kubectl get deployments -n $namespace -o jsonpath='{.items[*].metadata.name}' 2>&1
$deploymentList = $deployments -split ' '

Write-Host "Found $($deploymentList.Count) deployments"

foreach ($dep in $deploymentList) {
    if ($dep -match "igaming-source") {
        Write-Host "Patching deployment: $dep"
        # Get container names for this deployment
        $containers = kubectl get deployment $dep -n $namespace -o jsonpath='{.spec.template.spec.containers[*].name}' 2>&1
        $containerList = $containers -split ' '
        
        # Build patch for each container
        $containerPatches = @()
        foreach ($container in $containerList) {
            if ($container -ne "wait-for-sidecar" -and $container -ne "xvfb") {
                $containerPatches += "{`"name`":`"$container`",`"resources`":{`"requests`":{`"cpu`":`"50m`",`"memory`":`"200Mi`"}}}"
            }
        }
        
        if ($containerPatches.Count -gt 0) {
            $containersJson = $containerPatches -join ','
            $patchJson = "{`"spec`":{`"template`":{`"spec`":{`"containers`":[$containersJson]}}}}"
            kubectl patch deployment $dep -n $namespace --type=strategic -p $patchJson 2>&1 | Out-Null
            Write-Host "  -> patched: $($containerList -join ', ')"
        }
    }
}

Write-Host "Done! Patching igaming-portal..."
kubectl patch deployment igaming-portal -n $namespace -p '{"spec":{"template":{"spec":{"containers":[{"name":"igaming-portal","resources":{"requests":{"cpu":"100m","memory":"512Mi"}}}]}}}}' 2>&1

Write-Host "All done."
