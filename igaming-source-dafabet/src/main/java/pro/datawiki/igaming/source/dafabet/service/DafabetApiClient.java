package pro.datawiki.igaming.source.dafabet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.dafabet.config.DafabetConfig;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DafabetApiClient {

    private static final int INTERCEPT_TIMEOUT_MS = 30000;

    private final BrowserService browserService;
    private final DafabetConfig dafabetConfig;
    private final DafabetApiErrorTracker errorTracker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchOdds(String sportName) {
        String baseUrl = dafabetConfig.getApi().getBaseUrl();
        String oddsPattern = dafabetConfig.getApi().getOddsPattern(); // e.g. "/sports-api/" or similar JSON router

        for (int attempt = 1; attempt <= 3; attempt++) {
            log.info("Fetching Dafabet odds for sport: {} (attempt {}/3)", sportName, attempt);
            errorTracker.recordAttempt();
            try {
                // Navigate to standard sports page.
                // Dafabet pages dynamically load odds via XHR requests.
                // We will navigate directly to the page and intercept these requests.
                String targetUrl = baseUrl + "/en/sports";
                log.info("Strategy 1: Navigating to {} and intercepting network calls...", targetUrl);

                // Dump all network calls to inspect URLs
                try {
                    browserService.dumpNetworkCalls(targetUrl, 25000);
                } catch (Exception e) {
                    log.error("Failed to dump network calls: {}", e.getMessage());
                }

                String intercepted = browserService.navigateAndInterceptResponse(
                        targetUrl,
                        url -> {
                            String lower = url.toLowerCase();
                            if (lower.contains("settings") || lower.contains("message") || lower.contains("status") || lower.contains("analytics") || lower.contains("facebook") || lower.contains("menu")) {
                                return false;
                            }
                            return lower.contains("odds") || lower.contains("event") || lower.contains("match") || lower.contains("fixture");
                        },
                        INTERCEPT_TIMEOUT_MS
                );

                if (intercepted != null && !intercepted.trim().startsWith("<")) {
                    log.info("Strategy 1 succeeded for sport: {} (size={})", sportName, intercepted.length());
                    return objectMapper.readTree(intercepted);
                }

                if (intercepted != null && intercepted.trim().startsWith("<")) {
                    log.warn("Strategy 1 intercepted HTML instead of JSON for sport: {}", sportName);
                } else {
                    log.warn("Strategy 1: no matching XHR intercepted for sport: {}", sportName);
                }

                // Fallback Strategy: Direct navigation to sports content URL (legacy/secondary check)
                log.info("Strategy 2 (fallback): Direct body retrieval from Dafabet sports URL");
                String fallbackUrl = baseUrl + "/en/sports";
                String responseStr = browserService.navigateAndGetBody(fallbackUrl, 10000);

                if (responseStr == null || responseStr.isEmpty()) {
                    log.warn("Empty response from Dafabet for sport: {} (attempt {}/3)", sportName, attempt);
                    errorTracker.recordError("Empty response");
                    waitRetry(attempt);
                    continue;
                }

                if (responseStr.trim().startsWith("<") && !responseStr.contains("{")) {
                    log.warn("Received non-JSON HTML response from Dafabet for sport: {} (attempt {}/3).", sportName, attempt);
                    errorTracker.recordError("HTML instead of JSON");
                    waitRetry(attempt);
                    continue;
                }

                // Try to find if JSON is embedded in the page
                int jsonStartIndex = responseStr.indexOf("{");
                if (jsonStartIndex != -1) {
                    int jsonEndIndex = responseStr.lastIndexOf("}");
                    if (jsonEndIndex > jsonStartIndex) {
                        String potentialJson = responseStr.substring(jsonStartIndex, jsonEndIndex + 1);
                        try {
                            return objectMapper.readTree(potentialJson);
                        } catch (Exception ex) {
                            log.debug("Failed parsing embedded JSON substring: {}", ex.getMessage());
                        }
                    }
                }

                errorTracker.recordError("No JSON found");
                waitRetry(attempt);

            } catch (Exception e) {
                log.error("Failed to fetch Dafabet odds for sport: {} (attempt {}/3): {}", sportName, attempt, e.getMessage());
                errorTracker.recordError(e.getClass().getSimpleName() + ": " + e.getMessage());
                waitRetry(attempt);
            }
        }
        return null;
    }

    private void waitRetry(int attempt) {
        if (attempt < 3) {
            try { Thread.sleep(3000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
    }
}
