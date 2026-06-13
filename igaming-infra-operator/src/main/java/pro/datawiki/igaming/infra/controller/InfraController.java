package pro.datawiki.igaming.infra.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.infra.entity.ManagedInstance;
import pro.datawiki.igaming.infra.service.GcpHardwareService;
import pro.datawiki.igaming.infra.service.GeoScalingBalancerService;
import pro.datawiki.igaming.infra.service.KubernetesNodeService;
import pro.datawiki.igaming.infra.service.ManagedInstanceService;
import pro.datawiki.igaming.infra.service.SmartScalingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/infra")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InfraController {

    private final KubernetesNodeService nodeService;
    private final SmartScalingService smartScalingService;
    private final GcpHardwareService gcpHardwareService;
    private final GeoScalingBalancerService geoScalingBalancerService;
    private final ManagedInstanceService managedInstanceService;

    @GetMapping("/nodes/stats")
    public KubernetesNodeService.NodeStats getNodesStats() {
        return nodeService.getNodesStats();
    }

    @GetMapping("/nodes")
    public List<KubernetesNodeService.NodeDetail> getDetailedNodes() {
        return nodeService.getDetailedNodes();
    }

    @GetMapping("/hardware/stats")
    public Map<String, GcpHardwareService.GcpNodeInfo> getHardwareStats() {
        return gcpHardwareService.getGcpInstancesInfo();
    }

    @PostMapping("/nodes/provision")
    public String provisionNode() {
        return geoScalingBalancerService.provisionNode("ANY", "SPOT", "LOADER", "e2-standard-4", false);
    }

    @PostMapping("/nodes/provisionAccount")
    public String provisionNodeByAccount(@RequestParam String accountName) {
        return geoScalingBalancerService.provisionNodeByAccount(accountName, "SPOT", "LOADER", "e2-standard-4");
    }

    @DeleteMapping("/nodes/{instanceName}")
    public void terminateNode(@PathVariable String instanceName) {
        geoScalingBalancerService.terminateInstance(instanceName);
    }

    @GetMapping("/inventory")
    public List<ManagedInstance> getInventory() {
        return managedInstanceService.getAllInstances();
    }

    @PostMapping("/inventory")
    public ManagedInstance saveInventory(@RequestBody ManagedInstance instance) {
        return managedInstanceService.saveInstance(instance);
    }

    @DeleteMapping("/inventory/{instanceName}")
    public void deleteInventory(@PathVariable String instanceName) {
        geoScalingBalancerService.terminateInstance(instanceName);
    }

    @PostMapping("/inventory/sync")
    public void syncInventory() {
        managedInstanceService.syncFromCluster(nodeService.getDetailedNodes());
    }

    @PostMapping("/infrastructure/cleanup-nodes")
    public ResponseEntity<Map<String, Object>> cleanupNodes() {
        int cleaned = nodeService.cleanupNotReadyNodes();
        return ResponseEntity.ok(Map.of("cleanedCount", cleaned, "status", "success"));
    }

    @PostMapping("/scaling/spot")
    public void scaleSpotNodes(@RequestParam int size) {
        smartScalingService.setDesiredCount(size);
    }

    @GetMapping("/scaling/target")
    public int getTargetCount() {
        return smartScalingService.getDesiredCount();
    }

    @PostMapping("/scaling/sync-target")
    public void syncTarget() {
        smartScalingService.syncTargetWithCurrentCount();
    }

    @GetMapping("/scaling/stable-target")
    public int getStableTargetCount() {
        return smartScalingService.getDesiredStableCount();
    }

    @PostMapping("/scaling/stable")
    public void scaleStableNodes(@RequestParam int size) {
        smartScalingService.setDesiredStableCount(size);
    }
}
