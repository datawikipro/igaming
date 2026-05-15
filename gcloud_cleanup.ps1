# gcloud_cleanup.ps1
# Run this script to delete the old Spot and Worker VMs from GCP

Write-Host "This will delete all Spot and Worker instances in GCP."
Write-Host "Please ensure you have gcloud CLI installed and authenticated (gcloud auth login)."
Write-Host "You may need to provide your project ID or set it with 'gcloud config set project <PROJECT_ID>'"

$instances = @(
    "spot-gcp-527c217c",
    "spot-gcp-78788796",
    "spot-gcp-9c94d8c5",
    "spot-gcp-d1287c11",
    "spot-gcp-worker-1",
    "spot-worker-7390ed50",
    "spot-worker-ec4aa7f5",
    "worker-vm",
    "betting-stable-worker",
    "main-worker-gcp"
)

foreach ($instance in $instances) {
    # Replace '--zone' with your actual zone if needed. You can use '--quiet' to avoid prompts.
    # Note: If instances are in different zones, this command might fail without the exact zone.
    Write-Host "Deleting $instance..."
    gcloud compute instances delete $instance --quiet --zone "europe-west1-b" # Change zone as appropriate
}

Write-Host "Cleanup completed."
