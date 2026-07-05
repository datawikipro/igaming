package pro.datawiki.accounts.cloud.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import pro.datawiki.accounts.domain.entity.CloudCredential;
import pro.datawiki.accounts.domain.provider.CloudProvisioningProvider;
import pro.datawiki.accounts.domain.provider.ServerInfo;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AzureProvisioningProvider implements CloudProvisioningProvider {

    private final RestClient armClient = RestClient.builder()
            .baseUrl("https://management.azure.com")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String provider) {
        return "Azure".equalsIgnoreCase(provider);
    }

    @Override
    public String provisionNode(CloudCredential cred, String model, String role, String machineType, String region, String zone) {
        String token = getAccessToken(cred);
        String subId = getApiKey(cred, "subscriptionId");
        String rg = getApiKey(cred, "resourceGroup");
        
        String prefix = "SPOT".equalsIgnoreCase(model) ? "spot-" : "std-";
        String vmName = prefix + cred.getProvider().toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Provisioning Azure VM: {} in region: {}, resourceGroup: {}", vmName, region, rg);

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
                " --node-label=provider=azure" +
                " --node-label=node-type=" + model.toLowerCase() +
                " --node-label=role=" + role.toLowerCase() +
                " --flannel-iface=tailscale0\" \\\n" +
                "  sh -\n";

        String base64Script = Base64.getEncoder().encodeToString(startupScript.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> properties = new HashMap<>();
        properties.put("hardwareProfile", Map.of("vmSize", machineType != null ? machineType : "Standard_D4s_v5"));
        
        properties.put("storageProfile", Map.of(
                "imageReference", Map.of(
                        "publisher", "OpenLogic",
                        "offer", "CentOS",
                        "sku", "8_5-gen2",
                        "version", "latest"
                ),
                "osDisk", Map.of(
                        "createOption", "FromImage",
                        "diskSizeGB", 50,
                        "managedDisk", Map.of("storageAccountType", "Premium_LRS")
                )
        ));

        properties.put("osProfile", Map.of(
                "computerName", vmName,
                "adminUsername", "azureuser",
                "customData", base64Script,
                "linuxConfiguration", Map.of(
                        "disablePasswordAuthentication", true,
                        "ssh", Map.of("publicKeys", java.util.List.of(
                                Map.of(
                                        "path", "/home/azureuser/.ssh/authorized_keys",
                                        "keyData", getApiKey(cred, "sshPublicKey")
                                )
                        ))
                )
        ));

        properties.put("networkProfile", Map.of(
                "networkInterfaces", java.util.List.of(
                        Map.of(
                                "id", "/subscriptions/" + subId + "/resourceGroups/" + rg + "/providers/Microsoft.Network/networkInterfaces/" + vmName + "-nic",
                                "properties", Map.of("primary", true)
                        )
                )
        ));

        if ("SPOT".equalsIgnoreCase(model)) {
            properties.put("priority", "Spot");
            properties.put("evictionPolicy", "Deallocate");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("location", region.toLowerCase());
        requestBody.put("properties", properties);
        requestBody.put("tags", Map.of("role", role.toLowerCase(), "model", model.toLowerCase()));

        try {
            armClient.put()
                    .uri("/subscriptions/" + subId + "/resourceGroups/" + rg + "/providers/Microsoft.Compute/virtualMachines/" + vmName + "?api-version=2023-09-01")
                    .headers(headers -> headers.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("Successfully initiated Azure VM creation: {}", vmName);
            return vmName;
        } catch (Exception e) {
            log.error("Failed to provision Azure VM: {}", vmName, e);
            throw new RuntimeException("Failed to provision Azure node: " + e.getMessage(), e);
        }
    }

    @Override
    public void terminateNode(CloudCredential cred, String instanceName, String region, String zone) {
        log.info("Terminating Azure VM: {}", instanceName);
        String token = getAccessToken(cred);
        String subId = getApiKey(cred, "subscriptionId");
        String rg = getApiKey(cred, "resourceGroup");

        try {
            armClient.delete()
                    .uri("/subscriptions/" + subId + "/resourceGroups/" + rg + "/providers/Microsoft.Compute/virtualMachines/" + instanceName + "?api-version=2023-09-01")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Azure VM {} termination initiated.", instanceName);
        } catch (Exception e) {
            log.error("Failed to terminate Azure VM {}", instanceName, e);
            throw new RuntimeException("Failed to terminate Azure node", e);
        }
    }

    @Override
    public Map<String, ServerInfo> listServers(CloudCredential cred) {
        Map<String, ServerInfo> infoMap = new HashMap<>();
        String token = getAccessToken(cred);
        String subId = getApiKey(cred, "subscriptionId");
        String rg = getApiKey(cred, "resourceGroup");

        try {
            String response = armClient.get()
                    .uri("/subscriptions/" + subId + "/resourceGroups/" + rg + "/providers/Microsoft.Compute/virtualMachines?api-version=2023-09-01")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            for (JsonNode vm : node.path("value")) {
                String name = vm.path("name").asText();
                String status = vm.path("properties").path("provisioningState").asText().toUpperCase();
                
                String mappedStatus = "SUCCEEDED".equalsIgnoreCase(status) ? "RUNNING" : "PROVISIONING";
                
                Map<String, String> labels = new HashMap<>();
                JsonNode tags = vm.path("tags");
                if (tags.isObject()) {
                    tags.fields().forEachRemaining(entry -> labels.put(entry.getKey(), entry.getValue().asText()));
                }
                
                infoMap.put(name, new ServerInfo(mappedStatus, "", "", "", labels));
            }
        } catch (Exception e) {
            log.error("Failed to fetch Azure VM instances info for cred: {}", cred.getId(), e);
        }
        return infoMap;
    }

    @Override
    public Optional<BigDecimal> fetchBalance(CloudCredential cred) {
        return Optional.empty(); // Placeholder
    }

    @Override
    public boolean resetInstance(CloudCredential cred, String instanceName) {
        log.info("Resetting Azure VM: {}", instanceName);
        String token = getAccessToken(cred);
        String subId = getApiKey(cred, "subscriptionId");
        String rg = getApiKey(cred, "resourceGroup");

        try {
            armClient.post()
                    .uri("/subscriptions/" + subId + "/resourceGroups/" + rg + "/providers/Microsoft.Compute/virtualMachines/" + instanceName + "/restart?api-version=2023-09-01")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully triggered restart for Azure VM: {}", instanceName);
            return true;
        } catch (Exception e) {
            log.error("Failed to reset Azure VM {}", instanceName, e);
        }
        return false;
    }

    private String getAccessToken(CloudCredential cred) {
        String tenantId = getApiKey(cred, "tenantId");
        String clientId = getApiKey(cred, "clientId");
        String clientSecret = getApiKey(cred, "clientSecret");

        RestClient tokenClient = RestClient.builder()
                .baseUrl("https://login.microsoftonline.com/" + tenantId)
                .build();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("scope", "https://management.azure.com/.default");

        try {
            String response = tokenClient.post()
                    .uri("/oauth2/v2.0/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            return node.path("access_token").asText();
        } catch (Exception e) {
            log.error("Failed to fetch Azure token", e);
            throw new RuntimeException("Azure token retrieval failed", e);
        }
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
