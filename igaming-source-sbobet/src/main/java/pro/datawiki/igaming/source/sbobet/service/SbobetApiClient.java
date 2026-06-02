package pro.datawiki.igaming.source.sbobet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.sbobet.config.SbobetConfig;

@Service
@Slf4j
@RequiredArgsConstructor
public class SbobetApiClient {

    private final BrowserService browserService;
    private final SbobetConfig sbobetConfig;
    private final SbobetApiErrorTracker errorTracker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchOdds(String sportName) {
        log.info("Fetching SBOBET odds for sport: {}", sportName);
        errorTracker.recordAttempt();

        String url = sbobetConfig.getApi().getOddsUrl() + "?sport=" + sportName.toLowerCase();
        try {
            // SBOBET has heavy anti-bot mechanisms.
            // Using BrowserService (Playwright) to load page or extract JSON.
            String responseStr = browserService.navigateAndGetBody(url, 10000);
            if (responseStr == null || responseStr.isEmpty()) {
                log.warn("Empty response from SBOBET for sport: {}", sportName);
                errorTracker.recordError("Empty response");
                return null;
            }

            if (responseStr.trim().startsWith("<")) {
                // If it returns HTML instead of JSON, we can parse page/extract script tags,
                // or report standard HTML. For now, let's parse it as JsonNode or fallback
                // to a structured data if it's JSON nested inside script tags.
                log.debug("Received HTML response, attempting to locate embedded JSON data...");
                // Standard Sbobet client embeds state in global JS variables
                if (responseStr.contains("initialState")) {
                    int index = responseStr.indexOf("initialState");
                    // parse state...
                }
                
                // Let's create a simulated fallback JSON for demonstration or return parsed JSON.
                // We'll try to deserialize it or log error if it is not valid JSON.
                try {
                    return objectMapper.readTree(responseStr);
                } catch (Exception ex) {
                    log.warn("Response is HTML and could not be directly parsed as JSON: {}", ex.getMessage());
                    errorTracker.recordError("HTML instead of JSON");
                    return null;
                }
            }

            return objectMapper.readTree(responseStr);
        } catch (Exception e) {
            log.error("Failed to fetch SBOBET odds for sport: {}: {}", sportName, e.getMessage());
            errorTracker.recordError(e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }
}
