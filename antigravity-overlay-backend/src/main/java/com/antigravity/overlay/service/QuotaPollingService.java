package com.antigravity.overlay.service;

import com.antigravity.overlay.model.Account;
import com.antigravity.overlay.model.Quota;
import com.antigravity.overlay.repository.AccountRepository;
import com.antigravity.overlay.repository.QuotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Polls all registered accounts on a schedule.
 * For each account it calls AgyUsageService, which:
 *   - swaps ~/.gemini/oauth_creds.json
 *   - launches agy.exe in a real PTY
 *   - sends /usage, captures TUI output
 *   - parses Gemini + Claude/GPT quota %
 *
 * Results are stored as Quota rows with synthetic modelId keys:
 *   gemini-weekly | gemini-5h | claude-gpt-weekly | claude-gpt-5h
 */
@Service
public class QuotaPollingService {

    private static final Logger log = LoggerFactory.getLogger(QuotaPollingService.class);

    /** modelId constants stored in the DB */
    public static final String GEMINI_WEEKLY     = "gemini-weekly";
    public static final String GEMINI_5H         = "gemini-5h";
    public static final String CLAUDE_GPT_WEEKLY = "claude-gpt-weekly";
    public static final String CLAUDE_GPT_5H     = "claude-gpt-5h";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private QuotaRepository quotaRepository;

    @Autowired
    private AgyUsageService agyUsageService;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    // ── Scheduled polling ─────────────────────────────────────────────────────

    /** Runs every 15 minutes. AgyUsageService is already serialised per account. */
    @Scheduled(fixedDelay = 900_000)
    public void pollAllAccountsScheduled() {
        log.info("=== Scheduled quota poll starting ===");
        pollAllAccounts();
    }

    // ── Public API (callable from controller for manual sync) ─────────────────

    public void pollAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        log.info("Polling {} account(s) via agy...", accounts.size());
        for (Account account : accounts) {
            try {
                pollAccount(account);
            } catch (Exception e) {
                log.error("Failed to poll account: {}", account.getEmail(), e);
            }
        }
        log.info("=== Poll complete ===");
    }

    public void pollAccount(Account detachedAccount) {
        log.info("Polling via agy: {}", detachedAccount.getEmail());
        try {
            AgyUsageService.AgyUsageResult result =
                    agyUsageService.fetchUsage(detachedAccount.getEmail(), detachedAccount.getRefreshToken());

            new org.springframework.transaction.support.TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                Account account = accountRepository.findById(detachedAccount.getId())
                        .orElse(detachedAccount);

                saveQuotaBucket(account, GEMINI_WEEKLY,     result.geminiWeekly());
                saveQuotaBucket(account, GEMINI_5H,         result.gemini5h());
                saveQuotaBucket(account, CLAUDE_GPT_WEEKLY, result.claudeGptWeekly());
                saveQuotaBucket(account, CLAUDE_GPT_5H,     result.claudeGpt5h());

                account.setUpdatedAt(LocalDateTime.now());
                accountRepository.save(account);
            });

            log.info("Saved quotas for {} — G.weekly={}% G.5h={}% C.weekly={}%",
                    detachedAccount.getEmail(),
                    pct(result.geminiWeekly()),
                    pct(result.gemini5h()),
                    pct(result.claudeGptWeekly()));

        } catch (Exception e) {
            log.error("agy scrape failed for {}: {}", detachedAccount.getEmail(), e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    protected void saveQuotaBucket(Account account,
                                   String modelId,
                                   AgyUsageService.QuotaBucket bucket) {
        if (bucket == null) return;

        // Find existing row or create new
        Quota quota = quotaRepository
                .findByAccountAndModelId(account, modelId)
                .orElseGet(() -> {
                    Quota q = new Quota();
                    q.setAccount(account);
                    q.setModelId(modelId);
                    q.setTokenType("REQUESTS");
                    account.getQuotas().add(q);
                    return q;
                });

        quota.setRemainingFraction(bucket.remainingFraction());
        quota.setResetTime(bucket.resetsAt());
        quota.setUpdatedAt(LocalDateTime.now());
        quotaRepository.save(quota);
    }

    private String pct(AgyUsageService.QuotaBucket b) {
        if (b == null) return "?";
        return String.format("%.0f", b.remainingFraction() * 100);
    }
}
