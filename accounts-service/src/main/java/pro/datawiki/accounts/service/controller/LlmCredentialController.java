package pro.datawiki.accounts.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.accounts.domain.dto.LlmCredentialDto;
import pro.datawiki.accounts.domain.entity.Account;
import pro.datawiki.accounts.domain.entity.LlmCredential;
import pro.datawiki.accounts.service.mapper.AccountMapper;
import pro.datawiki.accounts.service.repository.AccountRepository;
import pro.datawiki.accounts.service.repository.LlmCredentialRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts/{accountId}/llm-credentials")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmCredentialController {

    private final LlmCredentialRepository llmCredentialRepository;
    private final AccountRepository accountRepository;

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/accounts/{accountId}/llm-credentials
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<LlmCredentialDto>> listLlmCredentials(@PathVariable Long accountId) {
        log.info("Listing LLM credentials for accountId={}", accountId);
        if (!accountRepository.existsById(accountId)) {
            log.warn("Account not found: id={}", accountId);
            return ResponseEntity.notFound().build();
        }
        List<LlmCredentialDto> dtos = llmCredentialRepository.findByAccountId(accountId)
                .stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/accounts/{accountId}/llm-credentials
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping
    @Transactional
    public ResponseEntity<LlmCredentialDto> saveLlmCredential(
            @PathVariable Long accountId,
            @RequestBody LlmCredential body) {

        log.info("Saving LLM credential for accountId={}, provider={}, email={}",
                accountId, body.getProvider(), body.getEmail());
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            log.warn("Account not found: id={}", accountId);
            return ResponseEntity.notFound().build();
        }
        body.setAccount(accountOpt.get());
        LlmCredential saved = llmCredentialRepository.save(body);
        log.info("LLM credential saved: id={}, provider={}", saved.getId(), saved.getProvider());
        return ResponseEntity.ok(AccountMapper.toDto(saved));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/accounts/{accountId}/llm-credentials/{credId}
    // ──────────────────────────────────────────────────────────────────────────

    @DeleteMapping("/{credId}")
    @Transactional
    public ResponseEntity<Void> deleteLlmCredential(
            @PathVariable Long accountId,
            @PathVariable Long credId) {

        log.info("Deleting LLM credential id={} for accountId={}", credId, accountId);
        if (!llmCredentialRepository.existsById(credId)) {
            log.warn("LLM credential not found: id={}", credId);
            return ResponseEntity.notFound().build();
        }
        llmCredentialRepository.deleteById(credId);
        log.info("LLM credential deleted: id={}", credId);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/accounts/{accountId}/llm-credentials/{credId}/toggle
    // ──────────────────────────────────────────────────────────────────────────

    @PatchMapping("/{credId}/toggle")
    @Transactional
    public ResponseEntity<LlmCredentialDto> toggleLlmCredential(
            @PathVariable Long accountId,
            @PathVariable Long credId) {

        log.info("Toggling LLM credential id={} for accountId={}", credId, accountId);
        Optional<LlmCredential> opt = llmCredentialRepository.findById(credId);
        if (opt.isEmpty()) {
            log.warn("LLM credential not found: id={}", credId);
            return ResponseEntity.notFound().build();
        }
        LlmCredential cred = opt.get();
        boolean newState = !Boolean.TRUE.equals(cred.getIsActive());
        cred.setIsActive(newState);
        LlmCredential saved = llmCredentialRepository.save(cred);
        log.info("LLM credential id={} toggled to isActive={}", credId, newState);
        return ResponseEntity.ok(AccountMapper.toDto(saved));
    }
}
