$files = Get-ChildItem -Recurse -Filter igaming-source-*.yaml | ? { $_.DirectoryName -like "*k8s\dev*" }

foreach ($file in $files) {
    Write-Host "Fixing $($file.FullName)..."
    $content = Get-Content $file.FullName -Raw
    
    # 1. Remove any existing (potentially broken) APP_PROXY_BACKEND_URL blocks
    # This handles both 8 and 10 space indentations for name and value
    $content = $content -replace "\s+-\s+name:\s+APP_PROXY_BACKEND_URL\r?\n\s+value:\s+\S+", ""
    
    # 2. Add it correctly after APP_PERSISTENCE_USE_REDIS_FACTORS
    # $1 = indent for '-', $2 = indent for 'value'
    $content = $content -replace "(\s+)-\s+name:\s+APP_PERSISTENCE_USE_REDIS_FACTORS\r?\n(\s+)value:\s+'?true'?", "`$1- name: APP_PERSISTENCE_USE_REDIS_FACTORS`r`n`$2value: 'true'`r`n`$1- name: APP_PROXY_BACKEND_URL`r`n`$2value: http://service-proxy-backend.service-proxy.svc.cluster.local"
    
    # 3. Remove legacy APP_PROXY_HOST
    $content = $content -replace "(\s+)-\s+name:\s+APP_PROXY_HOST\r?\n\s+value:\s+\S+", ""
    
    # 4. Clean up JAVA_OPTS (remove proxy flags)
    $content = $content -replace "-Dhttp\.proxyHost=\S+\s*", ""
    $content = $content -replace "-Dhttp\.proxyPort=\S+\s*", ""
    $content = $content -replace "-Dhttps\.proxyHost=\S+\s*", ""
    $content = $content -replace "-Dhttps\.proxyPort=\S+\s*", ""
    $content = $content -replace "-Dhttp\.nonProxyHosts=\S+\s*", ""
    
    # 5. Save and Apply
    $content | Set-Content $file.FullName
    Write-Host "Applying $($file.FullName)..."
    kubectl apply -f $file.FullName
}
