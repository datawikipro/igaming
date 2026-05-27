# add_main_worker.ps1
# Creates the main stable worker VM in europe-north2-b and joins it to k3s as a stable-worker.
# Run: .\add_main_worker.ps1

$Project     = "project-ec074516-4738-4d28-ad5"
$Zone        = "europe-north2-b"
$MachineType = "e2-custom-4-16384"  # 4 vCPU, 16 GB RAM
$NodeName    = "main-worker-gcp"

$TailscaleKey = "tskey-auth-kZotQnypWz11CNTRL-tZfx6Wiowm2GwL6euwP5n2Ti4487uRHab"
$MasterIP     = "master-vm"
$K3sToken     = "K10c7e23826108f187591e9533b0f508af002ac6670d728c57e0b4919cac2725b15::server:84cc0f96d1b83f1620ba81127b42d739"

$StartupScript = @"
#!/bin/bash
set -e
exec > /var/log/startup-script.log 2>&1
sleep 10
curl -fsSL https://tailscale.com/install.sh | sh
tailscale up --authkey='$TailscaleKey' --ssh

# Wait up to 60 seconds for Tailscale IP to become active
for i in {1..30}; do
  TAILSCALE_IP=\$(tailscale ip -4)
  if [ -n "\$TAILSCALE_IP" ]; then
    break
  fi
  sleep 2
done

if [ -z "\$TAILSCALE_IP" ]; then
  echo "FATAL: Tailscale IP is empty after 60s!"
  exit 1
fi

curl -sfL https://get.k3s.io | \
  KUBECONFIG_MODE="644" \
  K3S_URL="https://$($MasterIP):6443" \
  K3S_TOKEN="$K3sToken" \
  INSTALL_K3S_EXEC="agent --node-ip=\$TAILSCALE_IP --node-external-ip=\$TAILSCALE_IP --node-label=node-type=stable --node-label=provisioning=stable --node-label=role=worker --node-label=provider=gcp" \
  sh -
"@

$StartupScriptPath = [System.IO.Path]::GetTempFileName() + ".sh"
[System.IO.File]::WriteAllText($StartupScriptPath, ($StartupScript -replace "`r`n", "`n"), [System.Text.Encoding]::UTF8)

Write-Host "Creating Stable GCP VM: $NodeName in zone $Zone ..." -ForegroundColor Yellow

gcloud compute instances create $NodeName `
    --project=$Project `
    --zone=$Zone `
    --machine-type=$MachineType `
    --image-project=centos-cloud `
    --image-family=centos-stream-9 `
    --boot-disk-size=100GB `
    --boot-disk-type=pd-standard `
    --metadata-from-file=startup-script=$StartupScriptPath `
    --tags=k3s-worker

Remove-Item $StartupScriptPath -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "Waiting 90 seconds for VM to boot and join cluster..." -ForegroundColor Cyan
Start-Sleep -Seconds 90

$env:KUBECONFIG = "C:\Users\chernousov_a\.kube\igaming-cluster.yaml"
$kubectl = "C:\Program Files\Lens\resources\x64\kubectl.exe"
Write-Host "Checking nodes status..." -ForegroundColor Green
& $kubectl get nodes -o wide
