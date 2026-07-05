package pro.datawiki.accounts.cloud.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.accounts.cloud.service.GeoScalingService;
import pro.datawiki.accounts.cloud.service.KubernetesNodeService;
import pro.datawiki.accounts.cloud.service.SmartScalingService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cloud")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CloudController {

    private final KubernetesNodeService kubernetesNodeService;
    private final GeoScalingService geoScalingService;
    private final SmartScalingService smartScalingService;

    @GetMapping("/nodes/stats")
    public ResponseEntity<KubernetesNodeService.NodeStats> getNodesStats() {
        return ResponseEntity.ok(kubernetesNodeService.getNodesStats());
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<KubernetesNodeService.NodeDetail>> getDetailedNodes() {
        return ResponseEntity.ok(kubernetesNodeService.getDetailedNodes());
    }

    @PostMapping("/nodes/provision")
    public ResponseEntity<String> provisionNode(
            @RequestParam(defaultValue = "ANY") String region,
            @RequestParam(defaultValue = "SPOT") String model,
            @RequestParam(defaultValue = "LOADER") String role,
            @RequestParam(defaultValue = "e2-standard-4") String machineType,
            @RequestParam(defaultValue = "false") boolean isFallback) {
        
        String instanceName = geoScalingService.provisionNode(region, model, role, machineType, isFallback);
        return ResponseEntity.ok(instanceName);
    }

    @PostMapping("/nodes/provisionAccount")
    public ResponseEntity<String> provisionNodeByAccount(
            @RequestParam Long credentialId,
            @RequestParam(defaultValue = "SPOT") String model,
            @RequestParam(defaultValue = "LOADER") String role,
            @RequestParam(defaultValue = "e2-standard-4") String machineType) {
        
        String instanceName = geoScalingService.provisionNodeByAccount(credentialId, model, role, machineType);
        return ResponseEntity.ok(instanceName);
    }

    @DeleteMapping("/nodes/{instanceName}")
    public ResponseEntity<Void> terminateInstance(@PathVariable String instanceName) {
        geoScalingService.terminateInstance(instanceName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/nodes/cleanup")
    public ResponseEntity<Integer> cleanupNotReadyNodes() {
        int count = kubernetesNodeService.cleanupNotReadyNodes();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/scaling/spot")
    public ResponseEntity<Integer> getDesiredCount() {
        return ResponseEntity.ok(smartScalingService.getDesiredCount());
    }

    @PostMapping("/scaling/spot")
    public ResponseEntity<Void> setDesiredCount(@RequestParam int size) {
        smartScalingService.setDesiredCount(size);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/scaling/stable")
    public ResponseEntity<Integer> getDesiredStableCount() {
        return ResponseEntity.ok(smartScalingService.getDesiredStableCount());
    }

    @PostMapping("/scaling/stable")
    public ResponseEntity<Void> setDesiredStableCount(@RequestParam int size) {
        smartScalingService.setDesiredStableCount(size);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/scaling/sync-target")
    public ResponseEntity<Void> syncTarget() {
        smartScalingService.syncTargetWithCurrentCount();
        return ResponseEntity.ok().build();
    }
}
