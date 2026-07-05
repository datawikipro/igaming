package pro.datawiki.igaming.infra.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pro.datawiki.igaming.infra.entity.CloudAccount;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class OracleProvisioningProvider implements CloudProvisioningProvider {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String provider) {
        return "Oracle".equalsIgnoreCase(provider) || "OCI".equalsIgnoreCase(provider);
    }

    @Override
    public String provisionNode(CloudAccount account, String model, String role, String machineType, String region, String zone) {
        String prefix = "SPOT".equalsIgnoreCase(model) ? "spot-" : "std-";
        String vmName = prefix + account.getProvider().toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String tenancyId = getApiKey(account, "tenancyId");
        String compartmentId = getApiKey(account, "compartmentId");

        log.info("Provisioning Oracle Cloud (OCI) Instance: {} in region: {}", vmName, region);

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
                " --node-label=provider=oracle" +
                " --node-label=node-type=" + model.toLowerCase() +
                " --node-label=role=" + role.toLowerCase() +
                " --flannel-iface=tailscale0\" \\\n" +
                "  sh -\n";

        String base64Script = Base64.getEncoder().encodeToString(startupScript.getBytes(StandardCharsets.UTF_8));

        // Format OCI LaunchInstanceDetails body
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_data", base64Script);
        metadata.put("ssh_authorized_keys", getApiKey(account, "sshPublicKey"));

        Map<String, Object> shapeConfig = new HashMap<>();
        shapeConfig.put("ocpus", 4);
        shapeConfig.put("memoryInGBs", 16);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("compartmentId", compartmentId != null ? compartmentId : tenancyId);
        requestBody.put("displayName", vmName);
        requestBody.put("shape", machineType != null ? machineType : "VM.Standard.A1.Flex");
        requestBody.put("shapeConfig", shapeConfig);
        requestBody.put("metadata", metadata);
        
        Map<String, Object> sourceDetails = new HashMap<>();
        sourceDetails.put("sourceType", "image");
        sourceDetails.put("imageId", getApiKey(account, "imageId")); // e.g. Oracle Linux or CentOS image OCID
        requestBody.put("sourceDetails", sourceDetails);

        try {
            RestClient restClient = getOciClient(account, region);
            String response = restClient.post()
                    .uri("/instances")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String id = node.path("id").asText();
            log.info("Successfully created OCI instance: {} with ID: {}", vmName, id);
            return vmName;
        } catch (Exception e) {
            log.error("Failed to provision OCI instance: {}", vmName, e);
            throw new RuntimeException("Failed to provision OCI node: " + e.getMessage(), e);
        }
    }

    @Override
    public void terminateNode(CloudAccount account, String instanceName, String region, String zone) {
        log.info("Terminating OCI instance: {}", instanceName);
        try {
            RestClient restClient = getOciClient(account, region);
            String instanceId = findInstanceIdByName(account, restClient, instanceName);
            if (instanceId == null) {
                log.warn("OCI instance {} not found, skipping termination.", instanceName);
                return;
            }

            restClient.delete()
                    .uri("/instances/" + instanceId)
                    .retrieve()
                    .toBodilessEntity();

            log.info("OCI instance {} termination initiated.", instanceName);
        } catch (Exception e) {
            log.error("Failed to terminate OCI instance {}", instanceName, e);
            throw new RuntimeException("Failed to terminate OCI node", e);
        }
    }

    @Override
    public Map<String, CloudNodeInfo> getInstancesInfo(CloudAccount account) {
        Map<String, CloudNodeInfo> infoMap = new HashMap<>();
        String region = account.getDefaultRegion();
        if (region == null) region = "us-ashburn-1";

        try {
            RestClient restClient = getOciClient(account, region);
            String tenancyId = getApiKey(account, "tenancyId");
            String compartmentId = getApiKey(account, "compartmentId");
            String actualCompartment = (compartmentId != null && !compartmentId.isEmpty()) ? compartmentId : tenancyId;

            String response = restClient.get()
                    .uri("/instances?compartmentId=" + actualCompartment)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            for (JsonNode inst : node) {
                String name = inst.path("displayName").asText();
                String status = inst.path("lifecycleState").asText().toUpperCase(); // "RUNNING", "PROVISIONING", "TERMINATED"
                
                String mappedStatus = "RUNNING".equalsIgnoreCase(status) ? "RUNNING" : "PROVISIONING";
                
                Map<String, String> labels = new HashMap<>();
                JsonNode tags = inst.path("freeformTags");
                if (tags.isObject()) {
                    tags.fields().forEachRemaining(entry -> labels.put(entry.getKey(), entry.getValue().asText()));
                }
                
                infoMap.put(name, new CloudNodeInfo(mappedStatus, labels));
            }
        } catch (Exception e) {
            log.error("Failed to fetch OCI instances info for account: {}", account.getName(), e);
        }
        return infoMap;
    }

    @Override
    public boolean resetInstance(CloudAccount account, String instanceName) {
        log.info("Resetting OCI instance: {}", instanceName);
        String region = account.getDefaultRegion();
        if (region == null) region = "us-ashburn-1";

        try {
            RestClient restClient = getOciClient(account, region);
            String instanceId = findInstanceIdByName(account, restClient, instanceName);
            if (instanceId == null) {
                log.warn("OCI instance {} not found for reset.", instanceName);
                return false;
            }

            restClient.post()
                    .uri("/instances/" + instanceId + "/actions?action=SOFTRESET")
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully triggered soft reset for OCI instance: {}", instanceName);
            return true;
        } catch (Exception e) {
            log.error("Failed to reset OCI instance {}", instanceName, e);
        }
        return false;
    }

    private String findInstanceIdByName(CloudAccount account, RestClient restClient, String instanceName) throws Exception {
        String tenancyId = getApiKey(account, "tenancyId");
        String compartmentId = getApiKey(account, "compartmentId");
        String actualCompartment = (compartmentId != null && !compartmentId.isEmpty()) ? compartmentId : tenancyId;

        String response = restClient.get()
                .uri("/instances?compartmentId=" + actualCompartment)
                .retrieve()
                .body(String.class);

        JsonNode node = objectMapper.readTree(response);
        for (JsonNode inst : node) {
            if (instanceName.equalsIgnoreCase(inst.path("displayName").asText())) {
                return inst.path("id").asText();
            }
        }
        return null;
    }

    private RestClient getOciClient(CloudAccount account, String region) {
        String tenancyId = getApiKey(account, "tenancyId");
        String userId = getApiKey(account, "userId");
        String fingerprint = getApiKey(account, "fingerprint");
        String privateKeyPem = getApiKey(account, "privateKey");

        // OCI REST Clients use Request Signing. To keep this integration lightweight and functional,
        // we sign OCI requests dynamically. OCI requires standard Signature header based on tenancy, user, fingerprint and RSA private key.
        return RestClient.builder()
                .baseUrl("https://iaas." + region.toLowerCase() + ".oraclecloud.com/20160918")
                .requestInterceptor((request, body, execution) -> {
                    try {
                        String dateHeader = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).format(new Date());
                        request.getHeaders().add("date", dateHeader);

                        // OCI request signature format
                        String keyId = String.format("%s/%s/%s", tenancyId, userId, fingerprint);
                        String signingText = "date: " + dateHeader;
                        
                        PrivateKey pk = loadPrivateKey(privateKeyPem);
                        Signature sig = Signature.getInstance("SHA256withRSA");
                        sig.initSign(pk);
                        sig.update(signingText.getBytes(StandardCharsets.UTF_8));
                        String signature = Base64.getEncoder().encodeToString(sig.sign());

                        String authHeader = String.format(
                                "Signature headers=\"date\",keyId=\"%s\",algorithm=\"rsa-sha256\",signature=\"%s\",v=\"1\"",
                                keyId, signature
                        );
                        request.getHeaders().add("Authorization", authHeader);
                    } catch (Exception e) {
                        log.error("Failed to sign OCI request", e);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    private PrivateKey loadPrivateKey(String pem) throws Exception {
        String cleanPem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(cleanPem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
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
