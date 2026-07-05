package pro.datawiki.accounts.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.accounts.domain.entity.LlmCredential;
import pro.datawiki.accounts.service.repository.LlmCredentialRepository;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GlobalLlmCredentialController {

    private final LlmCredentialRepository llmCredentialRepository;

    @GetMapping("/api/v1/llm-credentials/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<LlmCredential>> getActiveLlmCredentials() {
        log.info("Fetching all active llm credentials globally");
        List<LlmCredential> activeCreds = llmCredentialRepository.findByIsActiveTrue();
        return ResponseEntity.ok(activeCreds);
    }
}

