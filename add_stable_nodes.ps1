# add_stable_nodes.ps1
# Creates 2 stable GCP VMs (europe-north1) and joins them to k3s as stable-workers
# Run: .\add_stable_nodes.ps1

param(
    [int]$Count = 2
)

$Project  = "project-ec074516-4738-4d28-ad5"
$Zone     = "europe-north1-b"
$Region   = "europe-north1"
# e2-standard-2: 2 vCPU, 8 GB RAM — standard (non-spot) GCP VM
$MachineType = "e2-standard-2"
$Image    = "projects/centos-cloud/global/images/family/centos-stream-9"
$K3sToken = "K10c7e23826108f187591e9533b0f508af002ac6670d728c57e0b4919cac2725b15::server:84cc0f96d1b83f1620ba81127b42d739"
$MasterIP = "100.86.137.112"   # Tailscale IP of master-vm
$K3sUrl   = "https://$($MasterIP):6443"

# Tailscale auth key for joining VPN
$TailscaleKey = "tskey-auth-kZotQnypWz11CNTRL-tZfx6Wiowm2GwL6euwP5n2Ti4487uRHab"

$NodeNames = @()
for ($i = 1; $i -le $Count; $i++) {
    $suffix = [System.Guid]::NewGuid().ToString("N").Substring(0, 8)
    $NodeNames += "stable-worker-gcp-$suffix"
}

Write-Host "=== Creating $Count stable GCP VM(s) ===" -ForegroundColor Cyan
Write-Host "Project: $Project | Zone: $Zone | Machine: $MachineType" -ForegroundColor DarkGray

# cloud-init startup script: install Tailscale + k3s agent
$StartupScript = @"
#!/bin/bash
set -e

# 1. Install Tailscale
curl -fsSL https://tailscale.com/install.sh | sh
systemctl enable --now tailscaled
tailscale up --authkey='$TailscaleKey' --hostname=\$(hostname) --accept-routes

# Wait for Tailscale to get an IP
sleep 10

# 2. Install k3s agent
export K3S_URL='$K3sUrl'
export K3S_TOKEN='$K3sToken'
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC='agent' sh -

systemctl enable k3s-agent
"@

$StartupScriptPath = [System.IO.Path]::GetTempFileName() + ".sh"
Set-Content -Path $StartupScriptPath -Value $StartupScript -Encoding UTF8

foreach ($NodeName in $NodeNames) {
    Write-Host ""
    Write-Host "Creating VM: $NodeName ..." -ForegroundColor Yellow

    gcloud compute instances create $NodeName `
        --project=$Project `
        --zone=$Zone `
        --machine-type=$MachineType `
        --image-project=centos-cloud `
        --image-family=centos-stream-9 `
        --boot-disk-size=40GB `
        --boot-disk-type=pd-standard `
        --metadata-from-file=startup-script=$StartupScriptPath `
        --tags=k3s-worker

    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to create VM $NodeName" -ForegroundColor Red
        continue
    }

    Write-Host "  VM $NodeName created. Waiting 60s for k3s agent to join..." -ForegroundColor DarkGray
}

Remove-Item $StartupScriptPath -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "Waiting 90 seconds for nodes to join the cluster..." -ForegroundColor Cyan
Start-Sleep -Seconds 90

# Label the new nodes
$env:KUBECONFIG = "C:\Users\chernousov_a\.kube\igaming-cluster.yaml"
$kubectl = "C:\Program Files\Lens\resources\x64\kubectl.exe"

Write-Host ""
Write-Host "=== Labeling new stable nodes ===" -ForegroundColor Cyan

$allNodes = & $kubectl get nodes -o json | ConvertFrom-Json
foreach ($node in $allNodes.items) {
    $nodeName = $node.metadata.name
    $labels   = $node.metadata.labels

    # Only process unlabeled new nodes (no node-type yet)
    if ($null -eq $labels."node-type" -and $nodeName -like "stable-worker-gcp-*") {
        Write-Host "Labeling node: $nodeName -> node-type=stable, provider=gcp, role=core" -ForegroundColor Green
        & $kubectl label node $nodeName `
            node-type=stable `
            provider=gcp `
            role=core `
            "failure-domain.beta.kubernetes.io/region=$Region" `
            "topology.kubernetes.io/region=$Region" `
            --overwrite
    }
}

Write-Host ""
Write-Host "=== Done! Current cluster nodes: ===" -ForegroundColor Green
& $kubectl get nodes --show-labels
