package pro.datawiki.igaming.infra.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pro.datawiki.igaming.infra.entity.CloudAccount;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class VultrProvisioningProvider implements CloudProvisioningProvider {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.vultr.com/v2")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String provider) {
        return "Vultr".equalsIgnoreCase(provider);
    }

    @Override
    public String provisionNode(CloudAccount account, String model, String role, String machineType, String region, String zone) {
        String apiKey = getApiKey(account, "apiKey");
        String prefix = "SPOT".equalsIgnoreCase(model) ? "spot-" : "std-";
        String instanceName = prefix + account.getProvider().toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Provisioning Vultr instance: {} in region: {}", instanceName, region);

        String startupScript = "#!/bin/bash\n" +
                "set -e\n" +
                "exec > /var/log/startup-script.log 2>&1\n" +
                "sleep 10\n" +
                "curl -fsSL https://tailscale.com/install.sh | sh\n" +
                "tailscale up --authkey=\"" + account.getTailscaleAuthKey() + "\" --ssh --advertise-exit-node\n" +
                "TAILSCALE_IP=$(tailscale ip -4)\n" +
                "\n" +
                "curl -sfL https://get.k3s.io | \\\n" +
                "  K3S_URL=\"" + account.getK3sServerUrl() + "\" \\\n" +
                "  K3S_TOKEN=\"" + account.getK3sToken() + "\" \\\n" +
                "  INSTALL_K3S_EXEC=\"agent --node-ip=$TAILSCALE_IP --node-external-ip=$TAILSCALE_IP" +
                " --node-label=topology.kubernetes.io/region=" + region.toLowerCase() +
                " --node-label=provider=vultr" +
                " --node-label=node-type=" + model.toLowerCase() +
                " --node-label=role=" + role.toLowerCase() +
                " --flannel-iface=tailscale0\" \\\n" +
                "  sh -\n";

        String base64Script = Base64.getEncoder().encodeToString(startupScript.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("region", region.toLowerCase());
        requestBody.put("plan", machineType != null ? machineType : "vc2-4c-8gb");
        requestBody.put("os_id", 1867); // CentOS Stream 9 x64
        requestBody.put("label", instanceName);
        requestBody.put("user_data", base64Script);
        requestBody.put("backups", "disabled");

        try {
            String response = restClient.post()
                    .uri("/instances")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String id = node.path("instance").path("id").asText();
            log.info("Successfully created Vultr instance: {} with ID: {}", instanceName, id);
            return instanceName;
        } catch (Exception e) {
            log.error("Failed to provision Vultr instance: {}", instanceName, e);
            throw new RuntimeException("Failed to provision Vultr node: " + e.getMessage(), e);
        }
    }

    @Override
    public void terminateNode(CloudAccount account, String instanceName, String region, String zone) {
        log.info("Terminating Vultr instance: {}", instanceName);
        String apiKey = getApiKey(account, "apiKey");
        try {
            // 1. Find the instance ID
            String response = restClient.get()
                    .uri("/instances")
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String instanceId = null;
            for (JsonNode inst : node.path("instances")) {
                if (instanceName.equalsIgnoreCase(inst.path("label").asText())) {
                    instanceId = inst.path("id").asText();
                    break;
                }
            }

            if (instanceId == null) {
                log.warn("Vultr instance {} not found, skipping termination.", instanceName);
                return;
            }

            // 2. Delete the instance
            restClient.delete()
                    .uri("/instances/" + instanceId)
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Vultr instance {} termination initiated.", instanceName);
        } catch (Exception e) {
            log.error("Failed to terminate Vultr instance {}", instanceName, e);
            throw new RuntimeException("Failed to terminate Vultr node", e);
        }
    }

    @Override
    public Map<String, CloudNodeInfo> getInstancesInfo(CloudAccount account) {
        Map<String, CloudNodeInfo> infoMap = new HashMap<>();
        String apiKey = getApiKey(account, "apiKey");
        try {
            String response = restClient.get()
                    .uri("/instances")
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            for (JsonNode inst : node.path("instances")) {
                String name = inst.path("label").asText();
                String status = inst.path("status").asText().toUpperCase(); // "pending", "running", "suspended"
                String powerStatus = inst.path("power_status").asText().toUpperCase(); // "running", "stopped"
                
                String mappedStatus = ("running".equalsIgnoreCase(status) || "running".equalsIgnoreCase(powerStatus)) 
                        ? "RUNNING" : "PROVISIONING";
                
                Map<String, String> labels = new HashMap<>();
                // Label extraction can be added based on Vultr tags/labels if configured
                infoMap.put(name, new CloudNodeInfo(mappedStatus, labels));
            }
        } catch (Exception e) {
            log.error("Failed to fetch Vultr instances info for account: {}", account.getName(), e);
        }
        return infoMap;
    }

    @Override
    public boolean resetInstance(CloudAccount account, String instanceName) {
        log.info("Resetting Vultr instance: {}", instanceName);
        String apiKey = getApiKey(account, "apiKey");
        try {
            // 1. Find Droplet ID
            String response = restClient.get()
                    .uri("/instances")
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String instanceId = null;
            for (JsonNode inst : node.path("instances")) {
                if (instanceName.equalsIgnoreCase(inst.path("label").asText())) {
                    instanceId = inst.path("id").asText();
                    break;
                }
            }

            if (instanceId == null) {
                log.warn("Vultr instance {} not found for reset.", instanceName);
                return false;
            }

            // 2. Trigger reboot action
            restClient.post()
                    .uri("/instances/" + instanceId + "/reboot")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully triggered reboot for Vultr instance: {}", instanceName);
            return true;
        } catch (Exception e) {
            log.error("Failed to reset Vultr instance {}", instanceName, e);
        }
        return false;
    }

    private String getApiKey(CloudAccount account, String keyName) {
        String creds = account.getCredentialsJson();
        if (creds == null || creds.trim().isEmpty()) {
            return "";
        }
        try {
            JsonNode node = objectMapper.readTree(creds);
            if (node.has(keyName)) {
                return node.get(keyName).asText();
            }
        } catch (Exception e) {
            return creds.trim();
        }
        return creds.trim();
    }
}
