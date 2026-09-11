package com.antigravity.overlay.controller;

import com.antigravity.overlay.model.Account;
import com.antigravity.overlay.repository.AccountRepository;
import com.antigravity.overlay.service.GoogleOAuthService;
import com.antigravity.overlay.service.QuotaPollingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@CrossOrigin(origins = "*") // Allows calls from localhost:3000
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private QuotaPollingService quotaPollingService;

    @Value("${google.client.id}")
    private String clientId;

    private static final String REDIRECT_URI = "http://localhost:8080/oauth2/callback";
    private static final String FRONTEND_URL = "http://localhost:3000";

    /**
     * Get all accounts and their quotas.
     */
    @GetMapping("/api/accounts")
    @ResponseBody
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    /**
     * Get the best account with the highest remaining quota for a given model (e.g. gemini-5h, claude-gpt-5h).
     */
    @GetMapping({"/api/accounts/best-account", "/api/accounts/best"})
    @ResponseBody
    public ResponseEntity<?> getBestAccount(
            @RequestParam(value = "modelId", defaultValue = "gemini-5h") String modelId) {
        List<Account> accounts = accountRepository.findAll();
        if (accounts.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "No accounts registered in overlay DB"
            ));
        }

        Account bestAccount = null;
        double maxQuota = -1.0;

        for (Account account : accounts) {
            double remaining = 1.0; // default assumed 100% if no quotas recorded yet
            if (account.getQuotas() != null && !account.getQuotas().isEmpty()) {
                for (com.antigravity.overlay.model.Quota q : account.getQuotas()) {
                    if (modelId.equalsIgnoreCase(q.getModelId()) ||
                        (modelId.startsWith("gemini") && q.getModelId() != null && q.getModelId().startsWith("gemini")) ||
                        (modelId.startsWith("claude") && q.getModelId() != null && q.getModelId().startsWith("claude"))) {
                        if (q.getRemainingFraction() != null) {
                            remaining = q.getRemainingFraction();
                        }
                    }
                }
            }
            if (remaining > maxQuota) {
                maxQuota = remaining;
                bestAccount = account;
            }
        }

        if (bestAccount == null || maxQuota <= 0.01) {
            return ResponseEntity.status(429).body(Map.of(
                "error", "No accounts with available quota found for modelId: " + modelId,
                "maxRemainingQuota", maxQuota
            ));
        }

        return ResponseEntity.ok(Map.of(
            "email", bestAccount.getEmail(),
            "remainingQuota", maxQuota,
            "modelId", modelId,
            "accountId", bestAccount.getId(),
            "refreshToken", bestAccount.getRefreshToken() != null ? bestAccount.getRefreshToken() : ""
        ));
    }

    /**
     * Delete an account.
     */
    @DeleteMapping({"/api/accounts/delete/{id}", "/api/accounts/id/{id}"})
    @ResponseBody
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        if (!accountRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        accountRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

    /**
     * Manually add an account by pasting email and refresh token.
     */
    @PostMapping("/api/accounts/add-manual")
    @ResponseBody
    public ResponseEntity<?> addManualAccount(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String refreshToken = request.get("refreshToken");

        if (email == null || email.trim().isEmpty() || refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and refreshToken are required"));
        }

        Account account = accountRepository.findByEmail(email).orElse(new Account());
        account.setEmail(email);
        account.setRefreshToken(refreshToken);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        try {
            // Trigger immediate quota polling
            quotaPollingService.pollAccount(account);
        } catch (Exception e) {
            log.error("Failed to poll manually added account", e);
        }

        return ResponseEntity.ok(account);
    }

    /**
     * Trigger manual sync of all accounts.
     */
    @PostMapping("/api/accounts/refresh-all")
    @ResponseBody
    public ResponseEntity<?> refreshAllAccounts() {
        quotaPollingService.pollAllAccounts();
        return ResponseEntity.ok(Map.of("message", "All accounts refreshed successfully"));
    }

    /**
     * Returns Google login URL.
     */
    @GetMapping("/api/oauth/login-url")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getLoginUrl() {
        String scopes = String.join(" ",
                "openid",
                "https://www.googleapis.com/auth/userinfo.email",
                "https://www.googleapis.com/auth/userinfo.profile",
                "https://www.googleapis.com/auth/cloud-platform",
                "https://www.googleapis.com/auth/cclog",
                "https://www.googleapis.com/auth/experimentsandconfigs"
        );

        String url = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                "&access_type=offline" +
                "&prompt=consent";

        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Handles Google OAuth callback.
     */
    @GetMapping("/oauth2/callback")
    public RedirectView oauthCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error) {

        if (error != null) {
            log.error("OAuth error received from Google: {}", error);
            return new RedirectView(FRONTEND_URL + "?status=error&message=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
        }

        if (code == null) {
            return new RedirectView(FRONTEND_URL + "?status=error&message=No+code+received");
        }

        try {
            // Exchange code for tokens
            GoogleOAuthService.TokenResponse tokenRes = googleOAuthService.exchangeCode(code, REDIRECT_URI);
            String email = googleOAuthService.extractEmailFromIdToken(tokenRes.idToken);

            if (email == null) {
                return new RedirectView(FRONTEND_URL + "?status=error&message=Could+not+extract+email");
            }

            Account account = accountRepository.findByEmail(email).orElse(new Account());
            account.setEmail(email);
            account.setAccessToken(tokenRes.accessToken);
            account.setExpiryTime(LocalDateTime.now().plusSeconds(tokenRes.expiresIn));
            if (tokenRes.refreshToken != null) {
                account.setRefreshToken(tokenRes.refreshToken);
            }
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);

            // Populate quota immediately
            quotaPollingService.pollAccount(account);

            return new RedirectView(FRONTEND_URL + "?status=success&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("OAuth callback failed", e);
            return new RedirectView(FRONTEND_URL + "?status=error&message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
