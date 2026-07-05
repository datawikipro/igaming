package pro.datawiki.accounts.llm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.accounts.domain.entity.LlmCredential;
import pro.datawiki.accounts.domain.entity.LlmQuotaSnapshot;
import pro.datawiki.accounts.llm.client.AccountsServiceClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class LlmPollingScheduler {

    @Autowired
    private AccountsServiceClient accountsServiceClient;

    @Autowired
    private AgyUsageService agyUsageService;

    private final AtomicBoolean isPolling = new AtomicBoolean(false);
    private final AtomicReference<LocalDateTime> lastPollAt = new AtomicReference<>();
    private final AtomicReference<Integer> lastCredentialCount = new AtomicReference<>(0);

    @Scheduled(fixedDelayString = "${llm.poll.interval-ms:900000}")
    public void pollAllCredentials() {
        if (!isPolling.compareAndSet(false, true)) {
            log.info("Already polling, skipping scheduled execution.");
            return;
        }

        try {
            log.info("=== Scheduled quota poll starting ===");
            List<LlmCredential> creds = accountsServiceClient.getActiveLlmCredentials();
            lastCredentialCount.set(creds != null ? creds.size() : 0);
            
            if (creds == null || creds.isEmpty()) {
                log.info("No active credentials found to poll.");
                return;
            }
            
            log.info("Polling {} credentials via agy...", creds.size());

            for (LlmCredential cred : creds) {
                if (!"ANTIGRAVITY".equals(cred.getProvider())) {
                    log.debug("Skipping non-ANTIGRAVITY credential for email: {}", cred.getEmail());
                    continue;
                }
                
                try {
                    log.info("Polling via agy for credential: {}", cred.getEmail());
                    AgyUsageService.AgyUsageResult result = agyUsageService.fetchUsage(cred.getEmail(), cred.getRefreshToken());
                    
                    LlmQuotaSnapshot snapshot = LlmQuotaSnapshot.builder()
                            .llmCredentialId(cred.getId())
                            .timestamp(LocalDateTime.now())
                            .geminiWeeklyPct(result.geminiWeekly().remainingFraction())
                            .gemini5hPct(result.gemini5h().remainingFraction())
                            .geminiWeeklyResetsAt(result.geminiWeekly().resetsAt())
                            .gemini5hResetsAt(result.gemini5h().resetsAt())
                            .claudeWeeklyPct(result.claudeGptWeekly().remainingFraction())
                            .claude5hPct(result.claudeGpt5h().remainingFraction())
                            .claudeWeeklyResetsAt(result.claudeGptWeekly().resetsAt())
                            .rawOutput(result.rawOutput())
                            .build();
                            
                    accountsServiceClient.saveLlmSnapshot(snapshot);
                    
                    log.info("Saved quotas for {} — G.weekly={}% G.5h={}% C.weekly={}%",
                            cred.getEmail(),
                            pct(result.geminiWeekly()),
                            pct(result.gemini5h()),
                            pct(result.claudeGptWeekly()));
                            
                } catch (Exception e) {
                    log.error("Failed to poll credential for email: {}", cred.getEmail(), e);
                }
            }
            log.info("=== Poll complete ===");
        } catch (Exception e) {
            log.error("Error during scheduled poll: {}", e.getMessage(), e);
        } finally {
            lastPollAt.set(LocalDateTime.now());
            isPolling.set(false);
        }
    }

    private String pct(AgyUsageService.QuotaBucket b) {
        if (b == null) return "?";
        return String.format("%.0f", b.remainingFraction() * 100);
    }
    
    public boolean isPolling() {
        return isPolling.get();
    }
    
    public LocalDateTime getLastPollAt() {
        return lastPollAt.get();
    }
    
    public int getLastCredentialCount() {
        return lastCredentialCount.get();
    }
}
