package pro.datawiki.accounts.cloud.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pro.datawiki.accounts.domain.entity.CloudCredential;
import pro.datawiki.accounts.domain.provider.CloudProvisioningProvider;
import pro.datawiki.accounts.domain.provider.ServerInfo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class DigitalOceanProvisioningProvider implements CloudProvisioningProvider {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.digitalocean.com/v2")
            .build();
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String provider) {
        return "DigitalOcean".equalsIgnoreCase(provider);
    }

    @Override
    public String provisionNode(CloudCredential cred, String model, String role, String machineType, String region, String zone) {
        String token = getApiKey(cred, "token");
        String prefix = "SPOT".equalsIgnoreCase(model) ? "spot-" : "std-";
        String dropletName = prefix + cred.getProvider().toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Provisioning DigitalOcean Droplet: {} in region: {}", dropletName, region);

        String startupScript = "#!/bin/bash\n" +
                "set -e\n" +
                "exec > /var/log/startup-script.log 2>&1\n" +
                "sleep 10\n" +
                "curl -fsSL https://tailscale.com/install.sh | sh\n" +
                "tailscale up --authkey=\"" + cred.getTailscaleAuthKey() + "\" --ssh --advertise-exit-node\n" +
                "TAILSCALE_IP=$(tailscale ip -4)\n" +
                "\n" +
                "curl -sfL https://get.k3s.io | \\\n" +
                "  K3S_URL=\"" + cred.getK3sServerUrl() + "\" \\\n" +
                "  K3S_TOKEN=\"" + cred.getK3sToken() + "\" \\\n" +
                "  INSTALL_K3S_EXEC=\"agent --node-ip=$TAILSCALE_IP --node-external-ip=$TAILSCALE_IP" +
                " --node-label=topology.kubernetes.io/region=" + region.toLowerCase() +
                " --node-label=provider=digitalocean" +
                " --node-label=node-type=" + model.toLowerCase() +
                " --node-label=role=" + role.toLowerCase() +
                " --flannel-iface=tailscale0\" \\\n" +
                "  sh -\n";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", dropletName);
        requestBody.put("region", region.toLowerCase());
        requestBody.put("size", machineType != null ? machineType : "s-4vcpu-8gb");
        requestBody.put("image", "centos-stream-9-x64");
        requestBody.put("user_data", startupScript);
        requestBody.put("tags", java.util.List.of(role.toLowerCase(), model.toLowerCase()));

        try {
            String response = restClient.post()
                    .uri("/droplets")
                    .headers(headers -> headers.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String id = node.path("droplet").path("id").asText();
            log.info("Successfully created Droplet: {} with ID: {}", dropletName, id);
            return dropletName;
        } catch (Exception e) {
            log.error("Failed to provision Droplet: {}", dropletName, e);
            throw new RuntimeException("Failed to provision DigitalOcean node: " + e.getMessage(), e);
        }
    }

    @Override
    public void terminateNode(CloudCredential cred, String instanceName, String region, String zone) {
        log.info("Terminating DigitalOcean Droplet: {}", instanceName);
        String token = getApiKey(cred, "token");
        try {
            String response = restClient.get()
                    .uri("/droplets")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String dropletId = null;
            for (JsonNode droplet : node.path("droplets")) {
                if (instanceName.equalsIgnoreCase(droplet.path("name").asText())) {
                    dropletId = droplet.path("id").asText();
                    break;
                }
            }

            if (dropletId == null) {
                log.warn("Droplet {} not found on DigitalOcean, skipping termination.", instanceName);
                return;
            }

            restClient.delete()
                    .uri("/droplets/" + dropletId)
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("DigitalOcean Droplet {} termination initiated.", instanceName);
        } catch (Exception e) {
            log.error("Failed to terminate DigitalOcean Droplet {}", instanceName, e);
            throw new RuntimeException("Failed to terminate DigitalOcean node", e);
        }
    }

    @Override
    public Map<String, ServerInfo> listServers(CloudCredential cred) {
        Map<String, ServerInfo> infoMap = new HashMap<>();
        String token = getApiKey(cred, "token");
        try {
            String response = restClient.get()
                    .uri("/droplets")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            for (JsonNode droplet : node.path("droplets")) {
                String name = droplet.path("name").asText();
                String status = droplet.path("status").asText().toUpperCase();
                String mappedStatus = "active".equalsIgnoreCase(status) ? "RUNNING" : "PROVISIONING";
                
                Map<String, String> labels = new HashMap<>();
                for (JsonNode tag : droplet.path("tags")) {
                    labels.put(tag.asText(), "true");
                }
                
                infoMap.put(name, new ServerInfo(mappedStatus, "", "", "", labels));
            }
        } catch (Exception e) {
            log.error("Failed to fetch DigitalOcean instances info for cred: {}", cred.getId(), e);
        }
        return infoMap;
    }

    @Override
    public Optional<BigDecimal> fetchBalance(CloudCredential cred) {
        return Optional.empty(); // Placeholder
    }

    @Override
    public boolean resetInstance(CloudCredential cred, String instanceName) {
        log.info("Resetting DigitalOcean Droplet: {}", instanceName);
        String token = getApiKey(cred, "token");
        try {
            String response = restClient.get()
                    .uri("/droplets")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String dropletId = null;
            for (JsonNode droplet : node.path("droplets")) {
                if (instanceName.equalsIgnoreCase(droplet.path("name").asText())) {
                    dropletId = droplet.path("id").asText();
                    break;
                }
            }

            if (dropletId == null) {
                log.warn("Droplet {} not found for reset.", instanceName);
                return false;
            }

            Map<String, String> body = Map.of("type", "reboot");
            restClient.post()
                    .uri("/droplets/" + dropletId + "/actions")
                    .headers(headers -> headers.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Successfully triggered reboot for Droplet: {}", instanceName);
            return true;
        } catch (Exception e) {
            log.error("Failed to reset DigitalOcean Droplet {}", instanceName, e);
        }
        return false;
    }

    private String getApiKey(CloudCredential cred, String keyName) {
        String creds = cred.getCredentialsJson();
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
