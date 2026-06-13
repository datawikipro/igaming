package pro.datawiki.igaming.infra.service;

import com.google.cloud.compute.v1.AggregatedListInstancesRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesScopedList;
import com.google.cloud.compute.v1.ListInstancesRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class GcpHardwareService {

    private final pro.datawiki.igaming.infra.repository.CloudAccountRepository cloudAccountRepository;

    public GcpHardwareService(pro.datawiki.igaming.infra.repository.CloudAccountRepository cloudAccountRepository) {
        this.cloudAccountRepository = cloudAccountRepository;
    }

    public Map<String, GcpNodeInfo> getGcpInstancesInfo() {
        Map<String, GcpNodeInfo> infoMap = new HashMap<>();
        java.util.List<pro.datawiki.igaming.infra.entity.CloudAccount> accounts = cloudAccountRepository.findByIsActiveTrueOrderByPriorityAsc();
        
        for (pro.datawiki.igaming.infra.entity.CloudAccount account : accounts) {
            if (!"GCP".equalsIgnoreCase(account.getProvider())) continue;
            
            try (InstancesClient client = createClient(account)) {
                AggregatedListInstancesRequest request = AggregatedListInstancesRequest.newBuilder()
                        .setProject(account.getProjectId())
                        .build();
                
                for (java.util.Map.Entry<String, InstancesScopedList> entry : client.aggregatedList(request).iterateAll()) {
                    InstancesScopedList instances = entry.getValue();
                    if (instances.getInstancesList() == null) continue;
                    
                    for (Instance instance : instances.getInstancesList()) {
                        String machineType = instance.getMachineType();
                        // Extract type name from URL like .../machineTypes/e2-standard-4
                        String typeName = machineType.substring(machineType.lastIndexOf("/") + 1);
                        
                        log.info("Found GCP instance: {} (Project: {}, Status: {}, Type: {})", 
                                instance.getName(), account.getProjectId(), instance.getStatus(), typeName);
                        
                        infoMap.put(instance.getName(), new GcpNodeInfo(
                                instance.getCpuPlatform(),
                                typeName,
                                instance.getStatus(),
                                instance.getScheduling().getPreemptible() ? "Spot" : "Standard",
                                instance.getLabelsMap()
                        ));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to fetch GCP hardware info for account: {}", account.getName(), e);
            }
        }
        return infoMap;
    }

    public boolean resetInstance(String instanceName) {
        java.util.List<pro.datawiki.igaming.infra.entity.CloudAccount> accounts = cloudAccountRepository.findByIsActiveTrueOrderByPriorityAsc();
        
        for (pro.datawiki.igaming.infra.entity.CloudAccount account : accounts) {
            if (!"GCP".equalsIgnoreCase(account.getProvider())) continue;
            
            try (InstancesClient client = createClient(account)) {
                AggregatedListInstancesRequest request = AggregatedListInstancesRequest.newBuilder()
                        .setProject(account.getProjectId())
                        .build();
                
                for (java.util.Map.Entry<String, InstancesScopedList> entry : client.aggregatedList(request).iterateAll()) {
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
        }
        return false;
    }

    private InstancesClient createClient(pro.datawiki.igaming.infra.entity.CloudAccount account) throws java.io.IOException {
        com.google.cloud.compute.v1.InstancesSettings.Builder settingsBuilder = com.google.cloud.compute.v1.InstancesSettings.newBuilder();
        if (account.getCredentialsJson() != null && !account.getCredentialsJson().trim().isEmpty()) {
            try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(account.getCredentialsJson().getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                com.google.auth.oauth2.GoogleCredentials credentials = com.google.auth.oauth2.GoogleCredentials.fromStream(bais)
                        .createScoped(java.util.List.of("https://www.googleapis.com/auth/cloud-platform"));
                settingsBuilder.setCredentialsProvider(com.google.api.gax.core.FixedCredentialsProvider.create(credentials));
            }
        }
        return InstancesClient.create(settingsBuilder.build());
    }

    public record GcpNodeInfo(String cpuPlatform, String machineType, String status, String scheduling, Map<String, String> labels) {}
}
