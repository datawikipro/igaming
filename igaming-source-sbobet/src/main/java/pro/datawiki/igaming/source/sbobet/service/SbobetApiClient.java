package pro.datawiki.igaming.source.sbobet.service;
 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.core.service.VpnManagerService;
import pro.datawiki.igaming.source.sbobet.config.SbobetConfig;
 
import java.util.Map;
 
/**
 * Fetches SBOBET odds using two strategies:
 * <ol>
 *   <li><b>Primary</b>: Navigate to the sport's betting page and intercept the JSON XHR
 *       response that SBOBET's own frontend makes (pattern: {@code /web-root/delivery/odds}).
 *       This works because SBOBET blocks direct API access but allows the request when it
 *       originates from within the page context with proper session cookies.</li>
 *   <li><b>Fallback</b>: Direct URL navigation to the odds endpoint (legacy behaviour).</li>
 * </ol>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SbobetApiClient {
 
    private static final int INTERCEPT_TIMEOUT_MS = 25000;

    /** Map from sport name (lower-case) to SBOBET URL segment */
    private static final Map<String, String> SPORT_URL_SEGMENTS = Map.of(
            "football",   "football",
            "soccer",     "football",
            "tennis",     "tennis",
            "basketball", "basketball",
            "volleyball", "volleyball",
            "hockey",     "ice-hockey",
            "baseball",   "baseball",
            "cricket",    "cricket"
    );

    private final BrowserService browserService;
    private final SbobetConfig sbobetConfig;
    private final SbobetApiErrorTracker errorTracker;
    private final VpnManagerService vpnManagerService;
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    public JsonNode fetchOdds(String sportName) {
        String baseUrl = sbobetConfig.getApi().getBaseUrl();
        String oddsApiPattern = "/web-root/delivery/odds";

        for (int attempt = 1; attempt <= 3; attempt++) {
            log.info("Fetching SBOBET odds for sport: {} (attempt {}/3)", sportName, attempt);
            errorTracker.recordAttempt();
            try {
                // --- Primary strategy: navigate to betting page and intercept XHR ---
                String sportSegment = SPORT_URL_SEGMENTS.getOrDefault(sportName.toLowerCase(), sportName.toLowerCase());
                String bettingPageUrl = baseUrl + "/ru-RU/euro/" + sportSegment;
                String oddsPattern = oddsApiPattern + "?sport=" + sportName.toLowerCase();

                log.info("Strategy 1: Navigating to {} and intercepting XHR {}", bettingPageUrl, oddsPattern);
                String intercepted = browserService.navigateAndInterceptResponse(
                        bettingPageUrl,
                        url -> url.contains(oddsApiPattern) && url.toLowerCase().contains(sportName.toLowerCase()),
                        INTERCEPT_TIMEOUT_MS
                );

                if (intercepted != null && !intercepted.trim().startsWith("<")) {
                    log.info("Strategy 1 succeeded for sport: {} (size={})", sportName, intercepted.length());
                    return objectMapper.readTree(intercepted);
                }

                if (intercepted != null && intercepted.trim().startsWith("<")) {
                    log.warn("Strategy 1 intercepted HTML for sport: {} — SBOBET blocking XHR too", sportName);
                } else {
                    log.warn("Strategy 1: no matching XHR intercepted for sport: {}", sportName);
                }

                // --- Fallback: try direct URL navigation (legacy) ---
                log.info("Strategy 2 (fallback): Direct navigation to odds URL for sport: {}", sportName);
                String directUrl = sbobetConfig.getApi().getOddsUrl() + "?sport=" + sportName.toLowerCase();
                String responseStr = browserService.navigateAndGetBody(directUrl, 10000);

                if (responseStr == null || responseStr.isEmpty()) {
                    log.warn("Empty response from SBOBET for sport: {} (attempt {}/3)", sportName, attempt);
                    errorTracker.recordError("Empty response");
                    rotateAndWait(attempt);
                    continue;
                }

                if (responseStr.trim().startsWith("<")) {
                    log.warn("Received HTML response from SBOBET for sport: {} (attempt {}/3). Access likely restricted.", sportName, attempt);
                    errorTracker.recordError("HTML instead of JSON");
                    rotateAndWait(attempt);
                    continue;
                }

                return objectMapper.readTree(responseStr);

            } catch (Exception e) {
                log.error("Failed to fetch SBOBET odds for sport: {} (attempt {}/3): {}", sportName, attempt, e.getMessage());
                errorTracker.recordError(e.getClass().getSimpleName() + ": " + e.getMessage());
                rotateAndWait(attempt);
            }
        }
        return null;
    }

    private void rotateAndWait(int attempt) {
        if (attempt < 3) {
            vpnManagerService.reportFailureAndRotate();
            try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
    }
}
