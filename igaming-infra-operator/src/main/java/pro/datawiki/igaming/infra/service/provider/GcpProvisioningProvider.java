package pro.datawiki.igaming.infra.service.provider;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.infra.entity.CloudAccount;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
    public String provisionNode(CloudAccount account, String model, String role, String machineType, String region, String zone) {
        String prefix = "SPOT".equalsIgnoreCase(model) ? "spot-" : "std-";
        String instanceName = prefix + account.getProvider().toLowerCase() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        
        log.info("Provisioning GCP {} instance: {} in {}/{}", model.toUpperCase(), instanceName, account.getProjectId(), zone);

        // Resolve per-account overrides, falling back to global env values
        String tsKey = (account.getTailscaleAuthKey() != null && !account.getTailscaleAuthKey().isBlank())
                ? account.getTailscaleAuthKey() : globalTailscaleAuthKey;
        String k3sUrl = (account.getK3sServerUrl() != null && !account.getK3sServerUrl().isBlank())
                ? account.getK3sServerUrl() : globalK3sServerUrl;
        String k3sToken = (account.getK3sToken() != null && !account.getK3sToken().isBlank())
                ? account.getK3sToken() : globalK3sToken;

        if (tsKey == null || tsKey.isBlank()) {
            throw new RuntimeException("Tailscale auth key not configured for account: " + account.getName());
        }
        if (k3sToken == null || k3sToken.isBlank()) {
            throw new RuntimeException("K3s token not configured for account: " + account.getName());
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
                "# Add random jitter to avoid thundering herd on GHCR during mass provisioning\n" +
                "JITTER=$((RANDOM % 60))\n" +
                "echo \"Adding $JITTER seconds jitter before K3s installation...\"\n" +
                "sleep $JITTER\n" +
                "\n" +
                "curl -sfL https://get.k3s.io | \\\n" +
                "  K3S_URL=\"" + k3sUrl + "\" \\\n" +
                "  K3S_TOKEN=\"" + k3sToken + "\" \\\n" +
                "  INSTALL_K3S_EXEC=\"agent --node-ip=$TAILSCALE_IP --node-external-ip=$TAILSCALE_IP" +
                " --node-label=topology.kubernetes.io/region=" + region.toLowerCase() +
                " --node-label=provider=" + account.getProvider().toLowerCase() +
                " --node-label=node-type=" + model.toLowerCase() +
                " --node-label=role=" + role.toLowerCase() +
                " --flannel-iface=tailscale0\" \\\n" +
                "  sh -\n" +
                "echo \"K3s agent started successfully\"\n" +
                "\n" +
                "# Diagnostic: check registry connectivity\n" +
                "echo \"Checking GHCR connectivity...\"\n" +
                "curl -Is https://ghcr.io/v2/ | grep -i \"status\"\n";

        try {
            boolean isSpot = "SPOT".equalsIgnoreCase(model);
            try (InstancesClient client = createClient(account)) {
                Instance instanceResource = Instance.newBuilder()
                        .setName(instanceName)
                        .setMachineType(String.format("projects/%s/zones/%s/machineTypes/%s", account.getProjectId(), zone, machineType))
                        .setScheduling(Scheduling.newBuilder()
                                .setProvisioningModel(isSpot ? "SPOT" : "STANDARD")
                                .setPreemptible(isSpot)
                                .build())
                        .addNetworkInterfaces(NetworkInterface.newBuilder()
                                .setNetwork(String.format("projects/%s/global/networks/default", account.getProjectId()))
                                .setSubnetwork(String.format("projects/%s/regions/%s/subnetworks/default", account.getProjectId(), region))
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
                        .setProject(account.getProjectId())
                        .setZone(zone)
                        .setInstanceResource(instanceResource)
                        .build();

                log.info("Sending GCP insert request for {}", instanceName);
                Operation response = client.insertAsync(account.getProjectId(), zone, instanceResource).get();

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
    public void terminateNode(CloudAccount account, String instanceName, String region, String zone) {
        log.info("Terminating GCP instance: {} in {}/{}", instanceName, account.getProjectId(), zone);
        try (InstancesClient client = createClient(account)) {
            DeleteInstanceRequest request = DeleteInstanceRequest.newBuilder()
                    .setProject(account.getProjectId())
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
    public Map<String, CloudNodeInfo> getInstancesInfo(CloudAccount account) {
        Map<String, CloudNodeInfo> infoMap = new HashMap<>();
        try (InstancesClient client = createClient(account)) {
            AggregatedListInstancesRequest request = AggregatedListInstancesRequest.newBuilder()
                    .setProject(account.getProjectId())
                    .build();
            
            for (Map.Entry<String, InstancesScopedList> entry : client.aggregatedList(request).iterateAll()) {
                InstancesScopedList instances = entry.getValue();
                if (instances.getInstancesList() == null) continue;
                
                for (Instance instance : instances.getInstancesList()) {
                    String machineType = instance.getMachineType();
                    String typeName = machineType.substring(machineType.lastIndexOf("/") + 1);
                    
                    infoMap.put(instance.getName(), new CloudNodeInfo(
                            instance.getStatus(),
                            instance.getCpuPlatform(),
                            typeName,
                            instance.getScheduling() != null && instance.getScheduling().getPreemptible() ? "Spot" : "Standard",
                            instance.getLabelsMap()
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch GCP instances info for account: {}", account.getName(), e);
        }
        return infoMap;
    }

    @Override
    public boolean resetInstance(CloudAccount account, String instanceName) {
        try (InstancesClient client = createClient(account)) {
            AggregatedListInstancesRequest request = AggregatedListInstancesRequest.newBuilder()
                    .setProject(account.getProjectId())
                    .build();
            
            for (Map.Entry<String, InstancesScopedList> entry : client.aggregatedList(request).iterateAll()) {
                InstancesScopedList instances = entry.getValue();
                if (instances.getInstancesList() == null) continue;
                
                for (Instance instance : instances.getInstancesList()) {
                    if (instance.getName().equals(instanceName)) {
                        String zoneUrl = instance.getZone();
                        String zoneName = zoneUrl.substring(zoneUrl.lastIndexOf("/") + 1);
                        
                        log.warn("⚠️ Resetting GCE VM instance natively via Java: {} (Zone: {})", instanceName, zoneName);
                        com.google.cloud.compute.v1.ResetInstanceRequest resetRequest = com.google.cloud.compute.v1.ResetInstanceRequest.newBuilder()
                                .setProject(account.getProjectId())
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

    private InstancesClient createClient(CloudAccount account) throws IOException {
        InstancesSettings.Builder settingsBuilder = InstancesSettings.newBuilder();
        if (account.getCredentialsJson() != null && !account.getCredentialsJson().trim().isEmpty()) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(account.getCredentialsJson().getBytes(StandardCharsets.UTF_8))) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(bais)
                        .createScoped(java.util.List.of("https://www.googleapis.com/auth/cloud-platform"));
                settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
            }
        }
        return InstancesClient.create(settingsBuilder.build());
    }
}
