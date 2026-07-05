package pro.datawiki.accounts.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.accounts.domain.entity.CloudSnapshot;
import pro.datawiki.accounts.domain.entity.LlmQuotaSnapshot;
import pro.datawiki.accounts.service.repository.CloudSnapshotRepository;
import pro.datawiki.accounts.service.repository.LlmQuotaSnapshotRepository;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SnapshotController {

    private final CloudSnapshotRepository cloudSnapshotRepository;
    private final LlmQuotaSnapshotRepository llmQuotaSnapshotRepository;

    @PostMapping("/api/v1/cloud-snapshots")
    @Transactional
    public ResponseEntity<CloudSnapshot> saveCloudSnapshot(@RequestBody CloudSnapshot snapshot) {
        log.info("Saving CloudSnapshot for cloudCredentialId={}", snapshot.getCloudCredentialId());
        CloudSnapshot saved = cloudSnapshotRepository.save(snapshot);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/api/v1/cloud-credentials/{credId}/snapshots")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CloudSnapshot>> getCloudSnapshots(@PathVariable Long credId) {
        log.info("Fetching last 10 CloudSnapshots for cloudCredentialId={}", credId);
        List<CloudSnapshot> snapshots = cloudSnapshotRepository.findTop10ByCloudCredentialIdOrderByTimestampDesc(credId);
        return ResponseEntity.ok(snapshots);
    }

    @PostMapping("/api/v1/llm-snapshots")
    @Transactional
    public ResponseEntity<LlmQuotaSnapshot> saveLlmSnapshot(@RequestBody LlmQuotaSnapshot snapshot) {
        log.info("Saving LlmQuotaSnapshot for llmCredentialId={}", snapshot.getLlmCredentialId());
        LlmQuotaSnapshot saved = llmQuotaSnapshotRepository.save(snapshot);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/api/v1/llm-credentials/{credId}/snapshots")
    @Transactional(readOnly = true)
    public ResponseEntity<List<LlmQuotaSnapshot>> getLlmSnapshots(@PathVariable Long credId) {
        log.info("Fetching last 10 LlmQuotaSnapshots for llmCredentialId={}", credId);
        List<LlmQuotaSnapshot> snapshots = llmQuotaSnapshotRepository.findTop10ByLlmCredentialIdOrderByTimestampDesc(credId);
        return ResponseEntity.ok(snapshots);
    }
}
