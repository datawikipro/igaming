package pro.datawiki.igaming.source.sbobet.service;
 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.core.service.VpnManagerService;
import pro.datawiki.igaming.source.sbobet.config.SbobetConfig;
 
@Service
@Slf4j
@RequiredArgsConstructor
public class SbobetApiClient {
 
    private final BrowserService browserService;
    private final SbobetConfig sbobetConfig;
    private final SbobetApiErrorTracker errorTracker;
    private final VpnManagerService vpnManagerService;
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    public JsonNode fetchOdds(String sportName) {
        String url = sbobetConfig.getApi().getOddsUrl() + "?sport=" + sportName.toLowerCase();
        
        for (int attempt = 1; attempt <= 3; attempt++) {
            log.info("Fetching SBOBET odds for sport: {} (attempt {}/3)", sportName, attempt);
            errorTracker.recordAttempt();
            try {
                String responseStr = browserService.navigateAndGetBody(url, 10000);
                if (responseStr == null || responseStr.isEmpty()) {
                    log.warn("Empty response from SBOBET for sport: {} (attempt {}/3)", sportName, attempt);
                    errorTracker.recordError("Empty response");
                    if (attempt < 3) {
                        vpnManagerService.reportFailureAndRotate();
                        Thread.sleep(2000);
                        continue;
                    }
                    return null;
                }
 
                if (responseStr.trim().startsWith("<")) {
                    log.warn("Received HTML response from SBOBET for sport: {} (attempt {}/3). Access likely restricted.", sportName, attempt);
                    errorTracker.recordError("HTML instead of JSON");
                    if (attempt < 3) {
                        vpnManagerService.reportFailureAndRotate();
                        Thread.sleep(2000);
                        continue;
                    }
                    return null;
                }
 
                return objectMapper.readTree(responseStr);
            } catch (Exception e) {
                log.error("Failed to fetch SBOBET odds for sport: {} (attempt {}/3): {}", sportName, attempt, e.getMessage());
                errorTracker.recordError(e.getClass().getSimpleName() + ": " + e.getMessage());
                if (attempt < 3) {
                    vpnManagerService.reportFailureAndRotate();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }
            }
        }
        return null;
    }
}
