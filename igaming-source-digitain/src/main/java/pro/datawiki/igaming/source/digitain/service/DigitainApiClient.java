package pro.datawiki.igaming.source.digitain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserExecutor;
import pro.datawiki.igaming.source.digitain.dto.DigitainEvent;
import pro.datawiki.igaming.source.digitain.utils.DigitainCryptoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class DigitainApiClient {

    private static final Logger log = LoggerFactory.getLogger(DigitainApiClient.class);

    private final BrowserExecutor browserExecutor;
    private final ObjectMapper objectMapper;

    // UUID for the Melbet Digitain API endpoint
    private static final String API_UUID = "35c6d708-2caa-464a-bab5-a6656b2b80f3";
    private static final String LIVE_URL = "https://sport.melbet.ru/" + API_UUID + "/live/geteventslist?sportId=10&stTypes=1&stTypes=702&stTypes=2&stTypes=3&stTypes=46&stTypes=992&stTypes=447974&langId=1&partnerId=3000057&countryCode=";
    private static final String PREMATCH_URL = "https://sport.melbet.ru/" + API_UUID + "/prematch/gettopeventslist?stakeTypes=1&stakeTypes=702&stakeTypes=2&stakeTypes=3&stakeTypes=46&stakeTypes=992&langId=1&partnerId=3000057&countryCode=";

    public DigitainApiClient(BrowserExecutor browserExecutor, ObjectMapper objectMapper) {
        this.browserExecutor = browserExecutor;
        this.objectMapper = objectMapper;
    }

    public void fetchSports() {
        log.info("Fetching sports from Digitain API...");
        
        try {
            // Use browser executor to intercept the JSON response or evaluate fetch on the page
            String encryptedResponse = browserExecutor.navigateAndGetBody(null, LIVE_URL, 10000, null);
            
            if (encryptedResponse != null && !encryptedResponse.isEmpty()) {
                log.info("Successfully fetched encrypted response (length: {})", encryptedResponse.length());
                
                String decryptedJson = DigitainCryptoUtils.decrypt(encryptedResponse);
                log.info("Decrypted JSON preview: {}", decryptedJson.substring(0, Math.min(200, decryptedJson.length())));

                // Digitain geteventslist usually returns an array of events
                List<DigitainEvent> events = objectMapper.readValue(decryptedJson, new TypeReference<List<DigitainEvent>>() {});
                log.info("Parsed {} events from Digitain API", events.size());
                
                // TODO: Save events to database
            } else {
                log.warn("Empty response for Digitain sports list");
            }
        } catch (Exception e) {
            log.error("Failed to fetch sports from Digitain API", e);
            throw new RuntimeException(e);
        }
    }
}
