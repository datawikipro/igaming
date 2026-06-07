package pro.datawiki.igaming.source.betfair.service;

import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.betfair.config.BetfairConfig;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetfairApiClient {

    private final RestTemplate restTemplate;
    private final BetfairConfig betfairConfig;
    private final BrowserService browserService;

    public BetfairEventsResponse getEvents() {
        log.info("Scraping public Betfair events...");
        BetfairEventsResponse response = new BetfairEventsResponse();
        List<BetfairEvent> list = new ArrayList<>();
        response.setEvents(list);

        try {
            String targetUrl = betfairConfig.getApi().getBaseUrl();
            if (targetUrl == null || targetUrl.isEmpty() || targetUrl.contains("mock")) {
                targetUrl = "https://www.betfair.com/sport/football";
            }
            
            log.info("Scraping Betfair site dynamically from: {}", targetUrl);
            String html = browserService.getForString(targetUrl, null);
            if (html == null || html.isEmpty()) {
                log.warn("Empty HTML returned from Betfair site");
                return response;
            }

            Document doc = Jsoup.parse(html);
            // Select all anchor links containing event/match details
            Elements links = doc.select("a[href*=/event/], a[href*=/event?], a[href*=/match-odds/]");
            log.info("Found {} potential event links on Betfair page", links.size());

            Set<String> processedIds = new HashSet<>();

            for (Element link : links) {
                String href = link.attr("href");
                String text = link.text().trim();
                
                String eventId = extractEventId(href);
                if (eventId == null || eventId.isEmpty() || processedIds.contains(eventId)) {
                    continue;
                }
                
                String team1 = null;
                String team2 = null;
                
                if (text.contains(" v ") || text.contains(" vs ") || text.contains(" - ")) {
                    String separator = text.contains(" v ") ? " v " : (text.contains(" vs ") ? " vs " : " - ");
                    String[] parts = text.split(separator, 2);
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                } else {
                    Elements nameSpans = link.select(".team-name, .home-team, .away-team, span[class*=\"team\"]");
                    if (nameSpans.size() >= 2) {
                        team1 = nameSpans.get(0).text().trim();
                        team2 = nameSpans.get(1).text().trim();
                    }
                }
                
                if (team1 == null || team1.isEmpty() || team2 == null || team2.isEmpty()) {
                    continue;
                }

                processedIds.add(eventId);

                BetfairEvent event = new BetfairEvent();
                event.setId(eventId);
                event.setSport("Soccer");
                event.setLeague("Football Match");
                event.setHomeTeam(team1);
                event.setAwayTeam(team2);
                event.setStartTime(System.currentTimeMillis() + 7200000); // starts in 2 hours
                event.setLive(href.contains("live") || text.toLowerCase().contains("live"));
                list.add(event);
                
                log.info("Parsed Betfair event: {} - {} (ID: {})", team1, team2, eventId);
            }
        } catch (Exception e) {
            log.error("Failed to parse public Betfair site: {}.", e.getMessage(), e);
        }

        if (list.isEmpty()) {
            log.warn("Scraped 0 active events from Betfair. The page structure might have changed!");
        } else {
            log.info("Successfully scraped {} active events from Betfair", list.size());
        }

        return response;
    }

    public BetfairOddsResponse getEventOdds(String eventId) {
        log.info("Scraping odds for Betfair event: {}", eventId);
        BetfairOddsResponse response = new BetfairOddsResponse();
        response.setEventId(eventId);
        List<BetfairOdd> oddsList = new ArrayList<>();
        response.setOdds(oddsList);

        try {
            String targetUrl = betfairConfig.getApi().getBaseUrl();
            if (targetUrl == null || targetUrl.isEmpty() || targetUrl.contains("mock")) {
                targetUrl = "https://www.betfair.com/sport/football";
            }
            String eventUrl = targetUrl + "/event?eventId=" + eventId;
            if (eventId.startsWith("bf-") || !eventId.matches("\\d+")) {
                log.warn("Invalid event ID format for direct navigation: {}", eventId);
                return response;
            }
            
            log.info("Loading Betfair match page: {}", eventUrl);
            String html = browserService.navigateAndGetBody(eventUrl, 8000, "betfair-event");
            if (html == null || html.isEmpty()) {
                log.warn("Empty HTML returned from Betfair event page: {}", eventId);
                return response;
            }

            Document doc = Jsoup.parse(html);
            // Select all market blocks
            Elements marketBlocks = doc.select(".market-group, .market-table, .coupon-row, [class*=\"market\"]");
            log.info("Found {} potential market blocks on Betfair event page", marketBlocks.size());

            for (Element block : marketBlocks) {
                Element titleEl = block.selectFirst(".market-title, .name, .category-label, [class*=\"title\"]");
                String marketName = titleEl != null ? titleEl.text().trim() : "Match Odds";
                if (marketName.isEmpty()) marketName = "Match Odds";

                Elements priceElements = block.select(".selection-link, .coeff-value, button, [class*=\"price\"], [class*=\"runner\"]");
                int index = 1;
                for (Element priceEl : priceElements) {
                    String text = priceEl.text().trim();
                    if (text.isEmpty() || text.equals("-") || text.equals("—")) continue;

                    String[] lines = text.split("\n");
                    String selectionName = lines.length > 1 ? lines[0].trim() : String.valueOf(index);
                    String oddStr = lines.length > 1 ? lines[1].trim() : lines[0].trim();
                    oddStr = oddStr.replaceAll("[^0-9.]", "");
                    if (oddStr.isEmpty()) continue;

                    try {
                        double oddValue = Double.parseDouble(oddStr);
                        BetfairOdd odd = new BetfairOdd(
                            String.valueOf(index),
                            marketName,
                            selectionName,
                            oddValue
                        );
                        oddsList.add(odd);
                        index++;
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (oddsList.isEmpty()) {
                log.warn("No odds parsed from Betfair event page. Attempting generic button selector.");
                Elements buttons = doc.select("button, .price, .runner");
                int index = 1;
                for (Element btn : buttons) {
                    String btnText = btn.text().trim();
                    if (btnText.isEmpty() || !btnText.matches(".*\\d+\\.\\d+.*")) continue;
                    
                    String[] lines = btnText.split("\n");
                    String selectionName = lines.length > 1 ? lines[0].trim() : "Outcome " + index;
                    String oddStr = lines.length > 1 ? lines[1].trim() : lines[0].trim();
                    oddStr = oddStr.replaceAll("[^0-9.]", "");
                    if (oddStr.isEmpty()) continue;

                    try {
                        double oddValue = Double.parseDouble(oddStr);
                        oddsList.add(new BetfairOdd(
                            String.valueOf(index),
                            "Match Odds",
                            selectionName,
                            oddValue
                        ));
                        index++;
                    } catch (NumberFormatException ignored) {}
                }
            }

            log.info("Successfully parsed {} odds for Betfair event {}", oddsList.size(), eventId);

        } catch (Exception e) {
            log.error("Failed to parse Betfair match page for event {}: {}", eventId, e.getMessage(), e);
        }

        return response;
    }

    private String extractEventId(String href) {
        if (href == null || href.isEmpty()) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("eventId=(\\d+)").matcher(href);
        if (m.find()) {
            return m.group(1);
        }
        m = java.util.regex.Pattern.compile("/event/(\\d+)").matcher(href);
        if (m.find()) {
            return m.group(1);
        }
        m = java.util.regex.Pattern.compile("/match-odds/(\\d+)").matcher(href);
        if (m.find()) {
            return m.group(1);
        }
        return "bf-" + Math.abs(href.hashCode());
    }

    @Getter
    @Setter
    public static class BetfairEventsResponse {
        private List<BetfairEvent> events;
    }

    @Getter
    @Setter
    public static class BetfairEvent {
        private String id;
        private String sport;
        private String league;
        private String homeTeam;
        private String awayTeam;
        private long startTime;
        private boolean isLive;
    }

    @Getter
    @Setter
    public static class BetfairOddsResponse {
        private String eventId;
        private List<BetfairOdd> odds;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class BetfairOdd {
        private String code;
        private String marketName;
        private String outcomeName;
        private double value;
    }
}
