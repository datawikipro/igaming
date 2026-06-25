package com.antigravity.overlay.service;

import com.antigravity.overlay.model.Account;
import com.antigravity.overlay.model.Quota;
import com.antigravity.overlay.repository.AccountRepository;
import com.antigravity.overlay.repository.QuotaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuotaPollingService {

    private static final Logger log = LoggerFactory.getLogger(QuotaPollingService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private QuotaRepository quotaRepository;

    @Autowired
    private GoogleOAuthService googleOAuthService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Poll all accounts every 5 minutes.
     */
    @Scheduled(fixedDelay = 300000)
    public void pollAllAccountsScheduled() {
        log.info("Starting scheduled quota polling for all accounts...");
        pollAllAccounts();
    }

    /**
     * Polls all registered accounts immediately.
     */
    @Transactional
    public void pollAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            try {
                pollAccount(account);
            } catch (Exception e) {
                log.error("Failed to poll quota for account: {}", account.getEmail(), e);
            }
        }
    }

    /**
     * Polls quota for a single account and updates DB.
     */
    @Transactional
    public void pollAccount(Account account) {
        log.info("Polling account: {}", account.getEmail());

        // 1. Refresh token if expired or close to expiry (within 5 minutes)
        boolean tokenRefreshed = false;
        if (account.getAccessToken() == null || account.getExpiryTime() == null 
                || account.getExpiryTime().isBefore(LocalDateTime.now().plusMinutes(5))) {
            try {
                log.info("Token for {} is expired or close to expiry. Refreshing...", account.getEmail());
                GoogleOAuthService.TokenResponse tokenRes = googleOAuthService.refreshToken(account.getRefreshToken());
                account.setAccessToken(tokenRes.accessToken);
                account.setExpiryTime(LocalDateTime.now().plusSeconds(tokenRes.expiresIn));
                if (tokenRes.refreshToken != null) {
                    account.setRefreshToken(tokenRes.refreshToken);
                }
                account.setUpdatedAt(LocalDateTime.now());
                accountRepository.save(account);
                tokenRefreshed = true;
                log.info("Token for {} refreshed successfully.", account.getEmail());
            } catch (Exception e) {
                log.error("Failed to refresh token for account: {}", account.getEmail(), e);
                return; // cannot proceed without active token
            }
        }

        // 2. Call Google Quota Endpoint
        String quotaUrl = "https://daily-cloudcode-pa.googleapis.com/v1internal:retrieveUserQuota";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(account.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(quotaUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                parseAndStoreQuota(account, response.getBody());
            } else {
                log.warn("Quota request for {} failed with status: {}", account.getEmail(), response.getStatusCode());
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Unauthorized request for {}. If the token was just refreshed, this token might have been invalidated.", account.getEmail());
            // If we didn't just refresh the token, try refreshing and retrying once
            if (!tokenRefreshed) {
                log.info("Retrying fetch after forcing token refresh for {}", account.getEmail());
                try {
                    GoogleOAuthService.TokenResponse tokenRes = googleOAuthService.refreshToken(account.getRefreshToken());
                    account.setAccessToken(tokenRes.accessToken);
                    account.setExpiryTime(LocalDateTime.now().plusSeconds(tokenRes.expiresIn));
                    account.setUpdatedAt(LocalDateTime.now());
                    accountRepository.save(account);
                    
                    headers.setBearerAuth(account.getAccessToken());
                    request = new HttpEntity<>("{}", headers);
                    ResponseEntity<String> retryResponse = restTemplate.postForEntity(quotaUrl, request, String.class);
                    if (retryResponse.getStatusCode().is2xxSuccessful()) {
                        parseAndStoreQuota(account, retryResponse.getBody());
                    }
                } catch (Exception retryEx) {
                    log.error("Retry failed for account {}", account.getEmail(), retryEx);
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch quota for account {}", account.getEmail(), e);
        }
    }

    private void parseAndStoreQuota(Account account, String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode bucketsNode = root.path("buckets");
        if (!bucketsNode.isArray()) {
            log.warn("No buckets array found in response for {}", account.getEmail());
            return;
        }

        // Map existing quotas by modelId + tokenType for quick lookup
        Map<String, Quota> existingMap = new HashMap<>();
        for (Quota q : account.getQuotas()) {
            String key = q.getModelId() + "|" + q.getTokenType();
            existingMap.put(key, q);
        }

        LocalDateTime now = LocalDateTime.now();

        for (JsonNode bucketNode : bucketsNode) {
            String modelId = bucketNode.path("modelId").asText();
            String tokenType = bucketNode.path("tokenType").asText();
            double remainingFraction = bucketNode.path("remainingFraction").asDouble();
            String resetTimeStr = bucketNode.path("resetTime").asText();

            LocalDateTime resetTime = null;
            if (resetTimeStr != null && !resetTimeStr.isEmpty()) {
                try {
                    Instant instant = Instant.parse(resetTimeStr);
                    resetTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                } catch (Exception ex) {
                    log.error("Failed to parse reset time string: {}", resetTimeStr, ex);
                }
            }

            String key = modelId + "|" + tokenType;
            Quota quota = existingMap.get(key);
            if (quota == null) {
                quota = new Quota();
                quota.setAccount(account);
                quota.setModelId(modelId);
                quota.setTokenType(tokenType);
                account.getQuotas().add(quota);
            }
            quota.setRemainingFraction(remainingFraction);
            quota.setResetTime(resetTime);
            quota.setUpdatedAt(now);
            quotaRepository.save(quota);
        }

        account.setUpdatedAt(now);
        accountRepository.save(account);
        log.info("Successfully updated quota cache for {}", account.getEmail());
    }
}
