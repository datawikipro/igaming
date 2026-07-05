package pro.datawiki.accounts.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.accounts.domain.dto.CloudCredentialDto;
import pro.datawiki.accounts.domain.entity.Account;
import pro.datawiki.accounts.domain.entity.CloudCredential;
import pro.datawiki.accounts.service.mapper.AccountMapper;
import pro.datawiki.accounts.service.repository.AccountRepository;
import pro.datawiki.accounts.service.repository.CloudCredentialRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts/{accountId}/cloud-credentials")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CloudCredentialController {

    private final CloudCredentialRepository cloudCredentialRepository;
    private final AccountRepository accountRepository;

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/accounts/{accountId}/cloud-credentials
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<CloudCredentialDto>> listCloudCredentials(@PathVariable Long accountId) {
        log.info("Listing cloud credentials for accountId={}", accountId);
        if (!accountRepository.existsById(accountId)) {
            log.warn("Account not found: id={}", accountId);
            return ResponseEntity.notFound().build();
        }
        List<CloudCredentialDto> dtos = cloudCredentialRepository.findByAccountId(accountId)
                .stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/accounts/{accountId}/cloud-credentials
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping
    @Transactional
    public ResponseEntity<CloudCredentialDto> saveCloudCredential(
            @PathVariable Long accountId,
            @RequestBody CloudCredential body) {

        log.info("Saving cloud credential for accountId={}, provider={}", accountId, body.getProvider());
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            log.warn("Account not found: id={}", accountId);
            return ResponseEntity.notFound().build();
        }
        body.setAccount(accountOpt.get());
        CloudCredential saved = cloudCredentialRepository.save(body);
        log.info("Cloud credential saved: id={}, provider={}", saved.getId(), saved.getProvider());
        return ResponseEntity.ok(AccountMapper.toDto(saved));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/accounts/{accountId}/cloud-credentials/{credId}
    // ──────────────────────────────────────────────────────────────────────────

    @DeleteMapping("/{credId}")
    @Transactional
    public ResponseEntity<Void> deleteCloudCredential(
            @PathVariable Long accountId,
            @PathVariable Long credId) {

        log.info("Deleting cloud credential id={} for accountId={}", credId, accountId);
        Optional<CloudCredential> opt = cloudCredentialRepository.findById(credId);
        if (opt.isEmpty()) {
            log.warn("Cloud credential not found: id={}", credId);
            return ResponseEntity.notFound().build();
        }
        cloudCredentialRepository.deleteById(credId);
        log.info("Cloud credential deleted: id={}", credId);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/accounts/{accountId}/cloud-credentials/{credId}/toggle
    // ──────────────────────────────────────────────────────────────────────────

    @PatchMapping("/{credId}/toggle")
    @Transactional
    public ResponseEntity<CloudCredentialDto> toggleCloudCredential(
            @PathVariable Long accountId,
            @PathVariable Long credId) {

        log.info("Toggling cloud credential id={} for accountId={}", credId, accountId);
        Optional<CloudCredential> opt = cloudCredentialRepository.findById(credId);
        if (opt.isEmpty()) {
            log.warn("Cloud credential not found: id={}", credId);
            return ResponseEntity.notFound().build();
        }
        CloudCredential cred = opt.get();
        boolean newState = !Boolean.TRUE.equals(cred.getIsActive());
        cred.setIsActive(newState);
        CloudCredential saved = cloudCredentialRepository.save(cred);
        log.info("Cloud credential id={} toggled to isActive={}", credId, newState);
        return ResponseEntity.ok(AccountMapper.toDto(saved));
    }
}
