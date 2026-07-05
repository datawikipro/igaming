package pro.datawiki.accounts.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.accounts.service.repository.AccountRepository;
import pro.datawiki.accounts.service.repository.CloudCredentialRepository;
import pro.datawiki.accounts.service.repository.LlmCredentialRepository;
import pro.datawiki.accounts.service.repository.ManagedServerRepository;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/overview")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OverviewController {

    private final AccountRepository accountRepository;
    private final CloudCredentialRepository cloudCredentialRepository;
    private final LlmCredentialRepository llmCredentialRepository;
    private final ManagedServerRepository managedServerRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getOverview() {
        log.info("Fetching overview stats");
        
        long totalAccounts = accountRepository.count();
        long totalCloudCredentials = cloudCredentialRepository.count();
        long totalLlmCredentials = llmCredentialRepository.count();
        long totalServers = managedServerRepository.count();
        long activeServers = managedServerRepository.findByStatusNot("TERMINATED").size();
        
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalAccounts", totalAccounts);
        stats.put("totalCloudCredentials", totalCloudCredentials);
        stats.put("totalLlmCredentials", totalLlmCredentials);
        stats.put("totalServers", totalServers);
        stats.put("activeServers", activeServers);
        
        return ResponseEntity.ok(stats);
    }
}
