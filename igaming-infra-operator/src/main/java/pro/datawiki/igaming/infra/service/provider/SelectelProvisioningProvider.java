package pro.datawiki.igaming.infra.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class SelectelProvisioningProvider implements CloudProvisioningProvider {

    private final RestClient identityClient = RestClient.builder()
            .baseUrl("https://api.selectel.ru/identity/v3")
            .build();

    private final RestClient novaClient = RestClient.builder()
            .baseUrl("https://ru-1.compute.selectel.ru/v2.1")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String provider) {
        return "Selectel".equalsIgnoreCase(provider);
    }

    @Override
    public String provisionNode(CloudAccount account, String model, String role, String machineType, String region, String zone) {
        String apiKey = getApiKey(account, "apiKey");
        String projectId = account.getProjectId();
        String token = getKeystoneToken(apiKey);

        String prefix = "SPOT".equalsIgnoreCase(model) ? "spot-" : "std-";
        String serverName = prefix + account.getProvider().toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Provisioning Selectel OpenStack server: {} in region: {}", serverName, region);

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
                " --node-label=provider=selectel" +
                " --node-label=node-type=" + model.toLowerCase() +
                " --node-label=role=" + role.toLowerCase() +
                " --flannel-iface=tailscale0\" \\\n" +
                "  sh -\n";

        String base64Script = Base64.getEncoder().encodeToString(startupScript.getBytes(StandardCharsets.UTF_8));

        // Format body for OpenStack Nova Create Server request
        Map<String, Object> serverDetails = new HashMap<>();
        serverDetails.put("name", serverName);
        serverDetails.put("imageRef", getApiKey(account, "imageRef")); // e.g. CentOS 9 image UUID
        serverDetails.put("flavorRef", machineType != null ? machineType : getApiKey(account, "flavorRef")); // e.g. configuration flavor
        serverDetails.put("user_data", base64Script);

        Map<String, Object> requestBody = Map.of("server", serverDetails);

        try {
            String response = novaClient.post()
                    .uri("/" + projectId + "/servers")
                    .header("X-Auth-Token", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String id = node.path("server").path("id").asText();
            log.info("Successfully created Selectel server: {} with ID: {}", serverName, id);
            return serverName;
        } catch (Exception e) {
            log.error("Failed to provision Selectel server: {}", serverName, e);
            throw new RuntimeException("Failed to provision Selectel node: " + e.getMessage(), e);
        }
    }

    @Override
    public void terminateNode(CloudAccount account, String instanceName, String region, String zone) {
        log.info("Terminating Selectel server: {}", instanceName);
        String apiKey = getApiKey(account, "apiKey");
        String projectId = account.getProjectId();
        String token = getKeystoneToken(apiKey);

        try {
            // 1. Find server ID
            String response = novaClient.get()
                    .uri("/" + projectId + "/servers")
                    .header("X-Auth-Token", token)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String serverId = null;
            for (JsonNode srv : node.path("servers")) {
                if (instanceName.equalsIgnoreCase(srv.path("name").asText())) {
                    serverId = srv.path("id").asText();
                    break;
                }
            }

            if (serverId == null) {
                log.warn("Selectel server {} not found, skipping termination.", instanceName);
                return;
            }

            // 2. Delete server
            novaClient.delete()
                    .uri("/" + projectId + "/servers/" + serverId)
                    .header("X-Auth-Token", token)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Selectel server {} termination initiated.", instanceName);
        } catch (Exception e) {
            log.error("Failed to terminate Selectel server {}", instanceName, e);
            throw new RuntimeException("Failed to terminate Selectel node", e);
        }
    }

    @Override
    public Map<String, CloudNodeInfo> getInstancesInfo(CloudAccount account) {
        Map<String, CloudNodeInfo> infoMap = new HashMap<>();
        String apiKey = getApiKey(account, "apiKey");
        String projectId = account.getProjectId();
        String token = getKeystoneToken(apiKey);

        try {
            String response = novaClient.get()
                    .uri("/" + projectId + "/servers/detail")
                    .header("X-Auth-Token", token)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            for (JsonNode srv : node.path("servers")) {
                String name = srv.path("name").asText();
                String status = srv.path("status").asText().toUpperCase(); // "ACTIVE", "BUILD", "SHUTOFF"
                
                String mappedStatus = "ACTIVE".equalsIgnoreCase(status) ? "RUNNING" : "PROVISIONING";
                
                Map<String, String> labels = new HashMap<>();
                // OpenStack metadata can be mapped to labels
                JsonNode metadata = srv.path("metadata");
                if (metadata.isObject()) {
                    metadata.fields().forEachRemaining(entry -> labels.put(entry.getKey(), entry.getValue().asText()));
                }
                
                infoMap.put(name, new CloudNodeInfo(mappedStatus, labels));
            }
        } catch (Exception e) {
            log.error("Failed to fetch Selectel instances info for account: {}", account.getName(), e);
        }
        return infoMap;
    }

    @Override
    public boolean resetInstance(CloudAccount account, String instanceName) {
        log.info("Resetting Selectel server: {}", instanceName);
        String apiKey = getApiKey(account, "apiKey");
        String projectId = account.getProjectId();
        String token = getKeystoneToken(apiKey);

        try {
            // 1. Find server ID
            String response = novaClient.get()
                    .uri("/" + projectId + "/servers")
                    .header("X-Auth-Token", token)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String serverId = null;
            for (JsonNode srv : node.path("servers")) {
                if (instanceName.equalsIgnoreCase(srv.path("name").asText())) {
                    serverId = srv.path("id").asText();
                    break;
                }
            }

            if (serverId == null) {
                log.warn("Selectel server {} not found for reset.", instanceName);
                return false;
            }

            // 2. Post reboot action
            Map<String, Object> body = Map.of("reboot", Map.of("type", "SOFT"));
            novaClient.post()
                    .uri("/" + projectId + "/servers/" + serverId + "/action")
                    .header("X-Auth-Token", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully triggered reboot for Selectel server: {}", instanceName);
            return true;
        } catch (Exception e) {
            log.error("Failed to reset Selectel server {}", instanceName, e);
        }
        return false;
    }

    private String getKeystoneToken(String apiKey) {
        // Authenticate with Selectel Identity API to exchange API Key for an IAM token
        try {
            ResponseEntity<String> response = identityClient.post()
                    .uri("/auth/tokens")
                    .header("X-Token", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")
                    .retrieve()
                    .toEntity(String.class);

            String token = response.getHeaders().getFirst("X-Subject-Token");
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("X-Subject-Token header is missing from Selectel response");
            }
            return token;
        } catch (Exception e) {
            log.error("Failed to authenticate with Selectel Identity API", e);
            throw new RuntimeException("Selectel authentication failed", e);
        }
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
