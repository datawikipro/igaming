package pro.datawiki.igaming.infra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.infra.entity.ManagedInstance;
import pro.datawiki.igaming.infra.repository.ManagedInstanceRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagedInstanceService {
    
    private final ManagedInstanceRepository repository;

    public List<ManagedInstance> getAllInstances() {
        return repository.findAll();
    }

    public ManagedInstance saveInstance(ManagedInstance instance) {
        if (instance.getCreatedAt() == null) {
            instance.setCreatedAt(LocalDateTime.now());
        }
        return repository.save(instance);
    }

    public void deleteInstance(String instanceName) {
        repository.deleteById(instanceName);
    }

    @org.springframework.transaction.annotation.Transactional
    public void syncFromCluster(List<KubernetesNodeService.NodeDetail> nodes) {
        List<ManagedInstance> existing = repository.findAll();
        System.out.println("Syncing from cluster. Existing in DB: " + existing.size() + ", Nodes in cluster: " + nodes.size());
        
        // 1. Mark existing nodes as OFFLINE if they are not in the cluster
        for (ManagedInstance inst : existing) {
            boolean inCluster = nodes.stream().anyMatch(n -> n.getName().equals(inst.getInstanceName()));
            if (!inCluster) {
                inst.setStatus("OFFLINE");
            } else {
                inst.setStatus("ACTIVE");
            }
        }

        // 2. Add new standard nodes from cluster if they are missing from DB
        for (KubernetesNodeService.NodeDetail node : nodes) {
            String model = node.getScheduling() != null ? node.getScheduling().toUpperCase() : "STANDARD";
            String name = node.getName().toLowerCase();
            
            // Ignore anything that is marked as SPOT or has 'spot' in its name
            if ("SPOT".equalsIgnoreCase(model) || name.contains("spot")) {
                System.out.println("Ignoring spot/ghost node: " + node.getName() + " (Model: " + model + ")");
                continue;
            }

            boolean known = existing.stream().anyMatch(m -> m.getInstanceName().equals(node.getName()));
            if (!known) {
                System.out.println("Adding new stable node: " + node.getName());
                ManagedInstance instance = ManagedInstance.builder()
                        .instanceName(node.getName())
                        .provisioningModel(model)
                        .machineType(node.getMachineType() != null ? node.getMachineType() : "unknown")
                        .role(node.getType() != null ? node.getType().toUpperCase() : "LOADER")
                        .isFallback("fallback".equalsIgnoreCase(node.getType()))
                        .status("ACTIVE")
                        .createdAt(LocalDateTime.now())
                        .build();
                existing.add(instance);
            }
        }

        // 3. Remove all spot instances from DB
        List<ManagedInstance> toRemove = existing.stream()
                .filter(m -> "SPOT".equalsIgnoreCase(m.getProvisioningModel()) || m.getInstanceName().toLowerCase().contains("spot"))
                .toList();
        
        if (!toRemove.isEmpty()) {
            System.out.println("Cleaning up " + toRemove.size() + " spot/ghost nodes from DB");
            repository.deleteAll(toRemove);
            existing.removeAll(toRemove);
        }

        repository.saveAll(existing);
        System.out.println("Sync finished. Final count in DB: " + existing.size());
    }

}
