$files = Get-ChildItem -Recurse -Filter igaming-source-*.yaml | ? { $_.DirectoryName -like "*k8s\dev*" }

foreach ($file in $files) {
    Write-Host "Optimizing $($file.FullName)..."
    $content = Get-Content $file.FullName -Raw
    
    # Define standard resource blocks
    $crawlerRes = "        resources:`r`n          requests:`r`n            cpu: 200m`r`n            memory: 1Gi`r`n          limits:`r`n            cpu: 800m`r`n            memory: 2Gi"
    $loaderRes = "        resources:`r`n          requests:`r`n            cpu: 50m`r`n            memory: 256Mi`r`n          limits:`r`n            cpu: 200m`r`n            memory: 512Mi"

    # 1. Handle Crawler
    # If resources: exists, replace the whole block until ports:
    if ($content -match "name: igaming-source-[\w-]+-crawler.*?resources:") {
        $content = $content -replace "(?s)(name: igaming-source-[\w-]+-crawler.*?env:.*?)\s+resources:.*?\s+ports:", "`$1`r`n$crawlerRes`r`n        ports:"
    } else {
        # If resources: missing, add it before ports:
        $content = $content -replace "(?s)(name: igaming-source-[\w-]+-crawler.*?env:.*?)\s+ports:", "`$1`r`n$crawlerRes`r`n        ports:"
    }

    # 2. Handle Loader
    if ($content -match "name: igaming-source-[\w-]+-loader.*?resources:") {
        $content = $content -replace "(?s)(name: igaming-source-[\w-]+-loader.*?env:.*?)\s+resources:.*?\s+ports:", "`$1`r`n$loaderRes`r`n        ports:"
    } else {
        $content = $content -replace "(?s)(name: igaming-source-[\w-]+-loader.*?env:.*?)\s+ports:", "`$1`r`n$loaderRes`r`n        ports:"
    }
    
    $content | Set-Content $file.FullName
    Write-Host "Applying $($file.FullName)..."
    kubectl apply -f $file.FullName
}
