package pro.datawiki.accounts.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.accounts.domain.entity.ManagedServer;
import pro.datawiki.accounts.service.repository.ManagedServerRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ManagedServerController {

    private final ManagedServerRepository managedServerRepository;

    @GetMapping("/api/v1/accounts/{accountId}/servers")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ManagedServer>> listServersForAccount(@PathVariable Long accountId) {
        log.info("Listing servers for accountId={}", accountId);
        List<ManagedServer> servers = managedServerRepository.findByAccount_Id(accountId);
        return ResponseEntity.ok(servers);
    }

    @PostMapping("/api/v1/servers")
    @Transactional
    public ResponseEntity<ManagedServer> saveServer(@RequestBody ManagedServer server) {
        log.info("Saving/upserting server: instanceName={}", server.getInstanceName());
        ManagedServer saved = managedServerRepository.save(server);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/api/v1/servers/{instanceName}")
    @Transactional
    public ResponseEntity<ManagedServer> updateServerStatus(@PathVariable String instanceName, @RequestBody ManagedServer serverUpdates) {
        log.info("Updating status for server: instanceName={}", instanceName);
        Optional<ManagedServer> opt = managedServerRepository.findById(instanceName);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ManagedServer existing = opt.get();
        if (serverUpdates.getStatus() != null) {
            existing.setStatus(serverUpdates.getStatus());
        }

        ManagedServer saved = managedServerRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/api/v1/servers/{instanceName}")
    @Transactional
    public ResponseEntity<Void> deleteServer(@PathVariable String instanceName) {
        log.info("Deleting server: instanceName={}", instanceName);
        if (!managedServerRepository.existsById(instanceName)) {
            return ResponseEntity.notFound().build();
        }
        managedServerRepository.deleteById(instanceName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/servers")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ManagedServer>> listAllActiveServers() {
        log.info("Listing all non-TERMINATED servers");
        List<ManagedServer> servers = managedServerRepository.findByStatusNot("TERMINATED");
        return ResponseEntity.ok(servers);
    }
}
