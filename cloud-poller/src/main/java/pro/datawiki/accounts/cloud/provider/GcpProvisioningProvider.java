package pro.datawiki.accounts.cloud.provider;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.*;
import com.google.cloud.billing.v1.CloudBillingClient;
import com.google.cloud.billing.v1.CloudBillingSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.accounts.domain.entity.CloudCredential;
import pro.datawiki.accounts.domain.provider.CloudProvisioningProvider;
import pro.datawiki.accounts.domain.provider.ServerInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class GcpProvisioningProvider implements CloudProvisioningProvider {

    @Value("${tailscale.auth-key:}")
    private String globalTailscaleAuthKey;

    @Value("${k3s.server-url:https://master-vm-1:6443}")
    private String globalK3sServerUrl;

    @Value("${k3s.token:}")
    private String globalK3sToken;

    @Override
    public boolean supports(String provider) {
        return "GCP".equalsIgnoreCase(provider);
    }

    @Override
    public String provisionNode(CloudCredential cred, String model, String role, String machineType, String region, String zone) {
        String prefix = "SPOT".equalsIgnoreCase(model) ? "spot-" : "std-";
        String instanceName = prefix + cred.getProvider().toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Provisioning GCP {} instance: {} in {}/{}", model.toUpperCase(), instanceName, cred.getProjectId(), zone);

        String tsKey = (cred.getTailscaleAuthKey() != null && !cred.getTailscaleAuthKey().isBlank())
                ? cred.getTailscaleAuthKey() : globalTailscaleAuthKey;
        String k3sUrl = (cred.getK3sServerUrl() != null && !cred.getK3sServerUrl().isBlank())
                ? cred.getK3sServerUrl() : globalK3sServerUrl;
        String k3sToken = (cred.getK3sToken() != null && !cred.getK3sToken().isBlank())
                ? cred.getK3sToken() : globalK3sToken;

        if (tsKey == null || tsKey.isBlank()) {
            throw new RuntimeException("Tailscale auth key not configured for account: " + cred.getId());
        }
        if (k3sToken == null || k3sToken.isBlank()) {
            throw new RuntimeException("K3s token not configured for account: " + cred.getId());
        }

        String startupScript = "#!/bin/bash\n" +
                "set -e\n" +
                "exec > /var/log/startup-script.log 2>&1\n" +
                "sleep 10\n" +
                "curl -fsSL https://tailscale.com/install.sh | sh\n" +
                "tailscale up --authkey=\"" + tsKey + "\" --ssh --advertise-exit-node\n" +
                "TAILSCALE_IP=$(tailscale ip -4)\n" +
                "echo \"Tailscale IP: $TAILSCALE_IP\"\n" +
                "\n" +
                "JITTER=$((RANDOM % 60))\n" +
                "echo \"Adding $JITTER seconds jitter before K3s installation...\"\n" +
                "sleep $JITTER\n" +
                "\n" +
                "curl -sfL https://get.k3s.io | \\\n" +
                "  K3S_URL=\"" + k3sUrl + "\" \\\n" +
                "  K3S_TOKEN=\"" + k3sToken + "\" \\\n" +
                "  INSTALL_K3S_EXEC=\"agent --node-ip=$TAILSCALE_IP --node-external-ip=$TAILSCALE_IP" +
                " --node-label=topology.kubernetes.io/region=" + region.toLowerCase() +
                " --node-label=provider=" + cred.getProvider().toLowerCase() +
                " --node-label=node-type=" + model.toLowerCase() +
                " --node-label=role=" + role.toLowerCase() +
                " --flannel-iface=tailscale0\" \\\n" +
                "  sh -\n" +
                "echo \"K3s agent started successfully\"\n" +
                "\n" +
                "echo \"Checking GHCR connectivity...\"\n" +
                "curl -Is https://ghcr.io/v2/ | grep -i \"status\"\n";

        try {
            boolean isSpot = "SPOT".equalsIgnoreCase(model);
            try (InstancesClient client = createClient(cred)) {
                Instance instanceResource = Instance.newBuilder()
                        .setName(instanceName)
                        .setMachineType(String.format("projects/%s/zones/%s/machineTypes/%s", cred.getProjectId(), zone, machineType))
                        .setScheduling(Scheduling.newBuilder()
                                .setProvisioningModel(isSpot ? "SPOT" : "STANDARD")
                                .setPreemptible(isSpot)
                                .build())
                        .addNetworkInterfaces(NetworkInterface.newBuilder()
                                .setNetwork(String.format("projects/%s/global/networks/default", cred.getProjectId()))
                                .setSubnetwork(String.format("projects/%s/regions/%s/subnetworks/default", cred.getProjectId(), region))
                                .addAccessConfigs(AccessConfig.newBuilder()
                                        .setName("External NAT")
                                        .setType(AccessConfig.Type.ONE_TO_ONE_NAT.toString())
                                        .setNetworkTier(AccessConfig.NetworkTier.PREMIUM.toString())
                                        .build())
                                .build())
                        .addDisks(AttachedDisk.newBuilder()
                                .setBoot(true)
                                .setAutoDelete(true)
                                .setType(AttachedDisk.Type.PERSISTENT.toString())
                                .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
                                        .setSourceImage("projects/centos-cloud/global/images/family/centos-stream-9")
                                        .setDiskSizeGb(50L)
                                        .build())
                                .build())
                        .setMetadata(Metadata.newBuilder()
                                .addItems(Items.newBuilder()
                                        .setKey("startup-script")
                                        .setValue(startupScript)
                                        .build())
                                .build())
                        .putLabels("role", role.toLowerCase())
                        .putLabels("model", model.toLowerCase())
                        .putLabels("region", region.toLowerCase())
                        .build();

                InsertInstanceRequest request = InsertInstanceRequest.newBuilder()
                        .setProject(cred.getProjectId())
                        .setZone(zone)
                        .setInstanceResource(instanceResource)
                        .build();

                log.info("Sending GCP insert request for {}", instanceName);
                Operation response = client.insertAsync(cred.getProjectId(), zone, instanceResource).get();

                if (response.hasError()) {
                    throw new RuntimeException("GCP Operation error: " + response.getError());
                }
            }
            return instanceName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to provision GCP node: " + e.getMessage(), e);
        }
    }

    @Override
    public void terminateNode(CloudCredential cred, String instanceName, String region, String zone) {
        log.info("Terminating GCP instance: {} in {}/{}", instanceName, cred.getProjectId(), zone);
        try (InstancesClient client = createClient(cred)) {
            DeleteInstanceRequest request = DeleteInstanceRequest.newBuilder()
                    .setProject(cred.getProjectId())
                    .setZone(zone)
                    .setInstance(instanceName)
                    .build();
            client.deleteAsync(request);
            log.info("GCP Termination request sent for {}", instanceName);
        } catch (Exception e) {
            log.error("Failed to terminate GCP instance {}", instanceName, e);
            throw new RuntimeException("Failed to terminate GCP node", e);
        }
    }

    @Override
    public Map<String, ServerInfo> listServers(CloudCredential cred) {
        Map<String, ServerInfo> infoMap = new HashMap<>();
        try (InstancesClient client = createClient(cred)) {
            AggregatedListInstancesRequest request = AggregatedListInstancesRequest.newBuilder()
                    .setProject(cred.getProjectId())
                    .build();

            for (Map.Entry<String, InstancesScopedList> entry : client.aggregatedList(request).iterateAll()) {
                InstancesScopedList instances = entry.getValue();
                if (instances.getInstancesList() == null) continue;

                for (Instance instance : instances.getInstancesList()) {
                    String machineType = instance.getMachineType();
                    String typeName = machineType.substring(machineType.lastIndexOf("/") + 1);

                    infoMap.put(instance.getName(), new ServerInfo(
                            instance.getStatus(),
                            instance.getCpuPlatform(),
                            typeName,
                            instance.getScheduling() != null && instance.getScheduling().getPreemptible() ? "Spot" : "Standard",
                            instance.getLabelsMap()
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch GCP instances info for cred: {}", cred.getId(), e);
        }
        return infoMap;
    }

    @Override
    public Optional<BigDecimal> fetchBalance(CloudCredential cred) {
        if (cred.getCredentialsJson() == null || cred.getCredentialsJson().trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(cred.getCredentialsJson().getBytes(StandardCharsets.UTF_8));
            GoogleCredentials credentials = GoogleCredentials.fromStream(bais)
                    .createScoped(java.util.List.of("https://www.googleapis.com/auth/cloud-platform"));

            CloudBillingSettings settings = CloudBillingSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            try (CloudBillingClient client = CloudBillingClient.create(settings)) {
                // Since balance requires BigQuery export or other complex setups, 
                // we'll just return empty for now as a placeholder for proper billing integration.
                // The client works to verify permissions.
                return Optional.empty();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch balance from GCP billing API for cred: {}", cred.getId());
            return Optional.empty();
        }
    }

    @Override
    public boolean resetInstance(CloudCredential cred, String instanceName) {
        try (InstancesClient client = createClient(cred)) {
            AggregatedListInstancesRequest request = AggregatedListInstancesRequest.newBuilder()
                    .setProject(cred.getProjectId())
                    .build();

            for (Map.Entry<String, InstancesScopedList> entry : client.aggregatedList(request).iterateAll()) {
                InstancesScopedList instances = entry.getValue();
                if (instances.getInstancesList() == null) continue;

                for (Instance instance : instances.getInstancesList()) {
                    if (instance.getName().equals(instanceName)) {
                        String zoneUrl = instance.getZone();
                        String zoneName = zoneUrl.substring(zoneUrl.lastIndexOf("/") + 1);

                        log.warn("⚠️ Resetting GCE VM instance natively via Java: {} (Zone: {})", instanceName, zoneName);
                        ResetInstanceRequest resetRequest = ResetInstanceRequest.newBuilder()
                                .setProject(cred.getProjectId())
                                .setZone(zoneName)
                                .setInstance(instanceName)
                                .build();
                        client.resetAsync(resetRequest);
                        log.info("✅ GCP Reset Operation initiated successfully via resetAsync.");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to reset GCE instance: {}", instanceName, e);
        }
        return false;
    }

    private InstancesClient createClient(CloudCredential cred) throws IOException {
        InstancesSettings.Builder settingsBuilder = InstancesSettings.newBuilder();
        if (cred.getCredentialsJson() != null && !cred.getCredentialsJson().trim().isEmpty()) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(cred.getCredentialsJson().getBytes(StandardCharsets.UTF_8))) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(bais)
                        .createScoped(java.util.List.of("https://www.googleapis.com/auth/cloud-platform"));
                settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
            }
        }
        return InstancesClient.create(settingsBuilder.build());
    }
}
