# Script to configure routing for Airflow (s3-dev) through Nginx in the llm namespace
# And update the local hosts file with the corresponding domains

$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition

Write-Host "=== Setting up Ingress and Routing for Airflow in s3-dev ===" -ForegroundColor Cyan

$nginxConfPath = Join-Path $scriptDir "nginx-airflow-routing.conf"

if (-not (Test-Path $nginxConfPath)) {
    Write-Error "Nginx config file not found at: $nginxConfPath"
    exit 1
}

try {
    Write-Host "Updating ConfigMap local-ingress-nginx-config in namespace llm from $nginxConfPath..." -ForegroundColor Yellow
    
    # Create ConfigMap from nginx.conf file and apply in cluster
    kubectl create configmap local-ingress-nginx-config -n llm --from-file=nginx.conf="$nginxConfPath" -o yaml --dry-run=client | kubectl apply -f -
    
    # Restart nginx deployment to apply changes
    Write-Host "Restarting Nginx proxy deployment..." -ForegroundColor Yellow
    kubectl rollout restart deployment local-ingress-nginx -n llm
    kubectl rollout status deployment local-ingress-nginx -n llm
    Write-Host "Nginx routing has been successfully configured!" -ForegroundColor Green
}
catch {
    Write-Error "An error occurred during Kubernetes configuration update: $_"
}

# Update local hosts file
Write-Host "`n=== Updating hosts file ===" -ForegroundColor Cyan
$hostsPath = "C:\Windows\System32\drivers\etc\hosts"
$ip = "100.86.137.112"
$domains = @(
    "airflow.smartbet.guru.local", "airflow.s3-dev.smartbet.guru.local",
    "minio.smartbet.guru.local", "minio.s3-dev.smartbet.guru.local",
    "minio-console.smartbet.guru.local", "minio-api.smartbet.guru.local", "minio-api.s3-dev.smartbet.guru.local",
    "superset.smartbet.guru.local", "superset.s3-dev.smartbet.guru.local"
)

if ($isAdmin) {
    foreach ($domain in $domains) {
        if (Select-String -Path $hostsPath -Pattern $domain -Quiet) {
            Write-Host "[Hosts] Domain $domain is already mapped in hosts file." -ForegroundColor Yellow
        } else {
            Write-Host "[Hosts] Adding mapping for $domain to $ip in hosts file..." -ForegroundColor Green
            Add-Content -Path $hostsPath -Value "`r`n$ip $domain"
        }
    }
    Write-Host "Hosts file successfully updated!" -ForegroundColor Green
} else {
    Write-Warning "Script is not running as Administrator. Cannot automatically update the hosts file."
    Write-Host "Please manually add the following lines to your hosts file at $hostsPath" -ForegroundColor Cyan
    foreach ($domain in $domains) {
        Write-Host "$ip $domain" -ForegroundColor White
    }
}

Write-Host "`nDone! You can now access:" -ForegroundColor Green
Write-Host " - Airflow:  http://airflow.smartbet.guru.local" -ForegroundColor White
Write-Host " - MinIO:    http://minio.smartbet.guru.local" -ForegroundColor White
Write-Host " - Superset: http://superset.smartbet.guru.local" -ForegroundColor White
