package pro.datawiki.accounts.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.accounts.domain.dto.AccountDashboardDto;
import pro.datawiki.accounts.domain.dto.CloudCredentialDto;
import pro.datawiki.accounts.domain.dto.LlmCredentialDto;
import pro.datawiki.accounts.domain.entity.*;
import pro.datawiki.accounts.service.mapper.AccountMapper;
import pro.datawiki.accounts.service.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final CloudCredentialRepository cloudCredentialRepository;
    private final LlmCredentialRepository llmCredentialRepository;
    private final CloudSnapshotRepository cloudSnapshotRepository;
    private final LlmQuotaSnapshotRepository llmQuotaSnapshotRepository;
    private final ManagedServerRepository managedServerRepository;

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/accounts  — list all accounts (basic info)
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listAccounts() {
        log.info("Listing all accounts");
        List<Map<String, Object>> result = accountRepository.findAll().stream()
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("name", a.getName());
                    m.put("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
                    m.put("updatedAt", a.getUpdatedAt() != null ? a.getUpdatedAt().toString() : null);
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/accounts  — create account
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping
    @Transactional
    public ResponseEntity<Account> createAccount(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        log.info("Creating account with name={}", name);
        Account account = Account.builder()
                .name(name)
                .build();
        Account saved = accountRepository.save(account);
        log.info("Account created: id={}, name={}", saved.getId(), saved.getName());
        return ResponseEntity.ok(saved);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/accounts/{id}  — get account by id
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable Long id) {
        log.info("Fetching account id={}", id);
        return accountRepository.findById(id)
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("name", a.getName());
                    m.put("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
                    m.put("updatedAt", a.getUpdatedAt() != null ? a.getUpdatedAt().toString() : null);
                    return ResponseEntity.ok(m);
                })
                .orElseGet(() -> {
                    log.warn("Account not found: id={}", id);
                    return ResponseEntity.notFound().<Map<String, Object>>build();
                });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/accounts/{id}
    // ──────────────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        log.info("Deleting account id={}", id);
        if (!accountRepository.existsById(id)) {
            log.warn("Account not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
        accountRepository.deleteById(id);
        log.info("Account deleted: id={}", id);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/accounts/{id}/dashboard  — full dashboard view
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/dashboard")
    @Transactional(readOnly = true)
    public ResponseEntity<AccountDashboardDto> getDashboard(@PathVariable Long id) {
        log.info("Building dashboard for account id={}", id);

        Optional<Account> opt = accountRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("Account not found for dashboard: id={}", id);
            return ResponseEntity.notFound().build();
        }
        Account account = opt.get();

        // Cloud credentials → DTOs
        List<CloudCredential> cloudCreds = cloudCredentialRepository.findByAccountId(id);
        List<CloudCredentialDto> cloudDtos = cloudCreds.stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());

        // LLM credentials → DTOs
        List<LlmCredential> llmCreds = llmCredentialRepository.findByAccountId(id);
        List<LlmCredentialDto> llmDtos = llmCreds.stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());

        // Latest cloud snapshot per credential
        List<CloudSnapshot> latestCloudSnapshots = cloudCreds.stream()
                .map(c -> cloudSnapshotRepository.findTopByCloudCredentialIdOrderByTimestampDesc(c.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // Latest LLM quota snapshot per credential
        List<LlmQuotaSnapshot> latestLlmSnapshots = llmCreds.stream()
                .map(c -> llmQuotaSnapshotRepository.findTopByLlmCredentialIdOrderByTimestampDesc(c.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // Managed servers for this account
        List<ManagedServer> servers = managedServerRepository.findByAccount_Id(id);

        AccountDashboardDto dto = AccountDashboardDto.builder()
                .id(account.getId())
                .name(account.getName())
                .createdAt(account.getCreatedAt() != null ? account.getCreatedAt().toString() : null)
                .updatedAt(account.getUpdatedAt() != null ? account.getUpdatedAt().toString() : null)
                .cloudCredentials(cloudDtos)
                .llmCredentials(llmDtos)
                .latestCloudSnapshots(latestCloudSnapshots)
                .latestLlmSnapshots(latestLlmSnapshots)
                .servers(servers)
                .build();

        log.info("Dashboard built for account id={}: {} cloud creds, {} llm creds, {} servers",
                id, cloudDtos.size(), llmDtos.size(), servers.size());

        return ResponseEntity.ok(dto);
    }
}
