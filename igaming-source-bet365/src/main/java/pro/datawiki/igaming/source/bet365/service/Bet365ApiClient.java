package pro.datawiki.igaming.source.bet365.service;

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
import pro.datawiki.igaming.source.bet365.config.Bet365Config;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class Bet365ApiClient {

    private final RestTemplate restTemplate;
    private final Bet365Config bet365Config;
    private final BrowserService browserService;

    // Cache to share scraped odds between getEvents (discovery) and getEventOdds (retrieval)
    private final Map<String, List<Bet365Odd>> oddsCache = new ConcurrentHashMap<>();

    public Bet365EventsResponse getEvents() {
        log.debug("Initializing browser-based extraction for Bet365 events...");
        
        List<Bet365Event> list = new ArrayList<>();
        
        try {
            String targetUrl = bet365Config.getApi().getBaseUrl();
            if (targetUrl != null && !targetUrl.contains("mock")) {
                log.info("Scraping public Bet365 site dynamically from: {}", targetUrl);
                // Wait 18 seconds for Cloudflare, WebSockets, and React layout to fully render
                String body = browserService.navigateAndGetBody(targetUrl, 18000, "bet365-lobby");
                log.debug("Retrieved Bet365 page body length: {}", body != null ? body.length() : 0);
                
                if (body != null && !body.isEmpty()) {
                    parseHtmlAndPopulateCache(body, list);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse public Bet365 site: {}. Falling back to default mock feed.", e.getMessage(), e);
        }

        // Fallback to mock feed if no events scraped
        if (list.isEmpty()) {
            log.info("Scraped 0 events. Generating fallback mock events.");
            
            // Add a mock football event
            Bet365Event event1 = new Bet365Event();
            event1.setId("b365_evt_1001");
            event1.setSport("Soccer");
            event1.setLeague("UEFA Champions League");
            event1.setHomeTeam("Real Madrid");
            event1.setAwayTeam("Bayern Munich");
            event1.setStartTime(System.currentTimeMillis() + 3600000); // starts in 1 hour
            event1.setLive(false);
            list.add(event1);

            // Add a mock tennis event
            Bet365Event event2 = new Bet365Event();
            event2.setId("b365_evt_1002");
            event2.setSport("Tennis");
            event2.setLeague("Wimbledon");
            event2.setHomeTeam("Novak Djokovic");
            event2.setAwayTeam("Carlos Alcaraz");
            event2.setStartTime(System.currentTimeMillis() - 600000); // started 10 minutes ago
            event2.setLive(true);
            list.add(event2);
        } else {
            log.info("Successfully scraped {} active events from Bet365", list.size());
        }

        Bet365EventsResponse response = new Bet365EventsResponse();
        response.setEvents(list);
        return response;
    }

    public Bet365OddsResponse getEventOdds(String eventId) {
        log.debug("Retrieving Bet365 event odds for: {}", eventId);
        
        List<Bet365Odd> odds = oddsCache.get(eventId);
        
        if (odds == null) {
            log.debug("Event {} not found in local scraped odds cache. Generating mock fallback.", eventId);
            odds = new ArrayList<>();
            if ("b365_evt_1001".equals(eventId)) {
                odds.add(new Bet365Odd("1", "1X2", "Real Madrid", 1.85));
                odds.add(new Bet365Odd("X", "1X2", "Draw", 3.60));
                odds.add(new Bet365Odd("2", "1X2", "Bayern Munich", 4.10));
            } else if ("b365_evt_1002".equals(eventId)) {
                odds.add(new Bet365Odd("1", "Match Winner", "Novak Djokovic", 1.65));
                odds.add(new Bet365Odd("2", "Match Winner", "Carlos Alcaraz", 2.20));
            }
        } else {
            log.debug("Found {} cached odds for event {}", odds.size(), eventId);
        }

        Bet365OddsResponse response = new Bet365OddsResponse();
        response.setEventId(eventId);
        response.setOdds(odds);
        return response;
    }

    private void parseHtmlAndPopulateCache(String html, List<Bet365Event> eventsList) {
        Document doc = Jsoup.parse(html);
        Elements groups = doc.select(".gl-MarketGroupContainer");
        
        for (Element group : groups) {
            String sport = "Soccer"; // Default
            String league = "Popular League";

            // Find parent group header to resolve sport/league name
            Element groupHeader = null;
            Element parent = group.parent();
            if (parent != null) {
                groupHeader = parent.selectFirst(".gl-MarketGroupButton");
            }
            if (groupHeader == null) {
                groupHeader = group.selectFirst(".gl-MarketGroupButton");
            }
            if (groupHeader != null) {
                String headerText = groupHeader.text().trim();
                league = headerText;
                if (headerText.toLowerCase().contains("tennis") || headerText.toLowerCase().contains("open") || headerText.toLowerCase().contains("franska öppna")) {
                    sport = "Tennis";
                } else if (headerText.toLowerCase().contains("esport") || headerText.toLowerCase().contains("valorant") || headerText.toLowerCase().contains("cs2") || headerText.toLowerCase().contains("league of legends")) {
                    sport = "Esports";
                } else if (headerText.toLowerCase().contains("basket") || headerText.toLowerCase().contains("nba")) {
                    sport = "Basketball";
                }
            }

            Element dateEl = group.selectFirst(".cpm-MarketFixtureDateHeader");
            String dateStr = dateEl != null ? dateEl.text().trim() : "";

            // 1. Standard row-based layout (Tennis/Soccer coupon)
            Elements rowFixtures = group.select(".cpm-MarketFixture");
            if (!rowFixtures.isEmpty()) {
                for (Element row : rowFixtures) {
                    Elements teamEls = row.select(".cpm-ParticipantFixtureDetails_Team");
                    if (teamEls.size() < 2) continue;
                    String team1 = teamEls.get(0).text().trim();
                    String team2 = teamEls.get(1).text().trim();

                    Element timeEl = row.selectFirst(".cpm-ParticipantFixtureDetails_BookCloses");
                    String timeStr = timeEl != null ? timeEl.text().trim() : "";

                    boolean isLive = dateStr.toLowerCase().contains("live") || timeStr.toLowerCase().contains("live");
                    long startTime = parseSwedishDate(dateStr, timeStr);

                    String eventId = generateEventId(sport, team1, team2, startTime);

                    Bet365Event event = new Bet365Event();
                    event.setId(eventId);
                    event.setSport(sport);
                    event.setLeague(league);
                    event.setHomeTeam(team1);
                    event.setAwayTeam(team2);
                    event.setStartTime(startTime);
                    event.setLive(isLive);
                    eventsList.add(event);

                    List<Bet365Odd> odds = new ArrayList<>();
                    Elements cols = row.select(".cpm-MarketOdds");
                    for (int idx = 0; idx < cols.size(); idx++) {
                        Element col = cols.get(idx);
                        Element headerEl = col.selectFirst(".cpm-MarketOddsHeader");
                        String headerText = headerEl != null ? headerEl.text().trim() : "";

                        Element valEl = col.selectFirst(".cpm-ParticipantOdds_Odds");
                        if (valEl != null) {
                            try {
                                double val = Double.parseDouble(valEl.text().trim());
                                String code = "Outcome_" + (idx + 1);
                                if ("1".equals(headerText)) code = "1";
                                else if ("2".equals(headerText)) code = "2";
                                else if ("X".equals(headerText) || headerText.toLowerCase().contains("oavgjort")) code = "X";

                                odds.add(new Bet365Odd(code, sport.equals("Tennis") || sport.equals("Esports") ? "Match Winner" : "1X2", idx == 0 ? team1 : (idx == 1 && cols.size() > 2 ? "Draw" : team2), val));
                            } catch (Exception ignored) {}
                        }
                    }
                    if (!odds.isEmpty()) {
                        oddsCache.put(eventId, odds);
                    }
                }
            } else {
                // 2. Coupon grid layout (Esports/Golf style)
                Elements teamBlocks = group.select(".cpm-ParticipantFixtureDetails100");
                if (!teamBlocks.isEmpty()) {
                    Elements cols = group.select(".cpm-MarketOdds");
                    
                    for (int matchIdx = 0; matchIdx < teamBlocks.size(); matchIdx++) {
                        Element teamBlock = teamBlocks.get(matchIdx);
                        Elements teamEls = teamBlock.select(".cpm-ParticipantFixtureDetails100_Team");
                        if (teamEls.size() < 2) continue;
                        String team1 = teamEls.get(0).text().trim();
                        String team2 = teamEls.get(1).text().trim();

                        Element timeEl = teamBlock.selectFirst(".cpm-ParticipantFixtureDetails100_BookCloses");
                        String timeStr = timeEl != null ? timeEl.text().trim() : "";

                        boolean isLive = dateStr.toLowerCase().contains("live") || timeStr.toLowerCase().contains("live");
                        long startTime = parseSwedishDate(dateStr, timeStr);

                        String eventId = generateEventId(sport, team1, team2, startTime);

                        Bet365Event event = new Bet365Event();
                        event.setId(eventId);
                        event.setSport(sport);
                        event.setLeague(league);
                        event.setHomeTeam(team1);
                        event.setAwayTeam(team2);
                        event.setStartTime(startTime);
                        event.setLive(isLive);
                        eventsList.add(event);

                        List<Bet365Odd> odds = new ArrayList<>();
                        for (Element col : cols) {
                            Element headerEl = col.selectFirst(".cpm-MarketOddsHeader");
                            String marketName = headerEl != null ? headerEl.text().trim() : "Match Winner";

                            Elements valEls = col.select(".cpm-ParticipantOdds_Odds");
                            if (valEls.size() == teamBlocks.size() * 2) {
                                // 2 outcomes per match
                                Element odd1El = valEls.get(matchIdx * 2);
                                Element odd2El = valEls.get(matchIdx * 2 + 1);

                                try {
                                    double val1 = Double.parseDouble(odd1El.text().trim());
                                    odds.add(new Bet365Odd("1", marketName, team1, val1));
                                } catch (Exception ignored) {}

                                try {
                                    double val2 = Double.parseDouble(odd2El.text().trim());
                                    odds.add(new Bet365Odd("2", marketName, team2, val2));
                                } catch (Exception ignored) {}
                            } else if (valEls.size() == teamBlocks.size()) {
                                // 1 outcome per match
                                Element oddEl = valEls.get(matchIdx);
                                try {
                                    double val = Double.parseDouble(oddEl.text().trim());
                                    odds.add(new Bet365Odd(marketName.equals("X") ? "X" : "1", marketName, marketName.equals("X") ? "Draw" : team1, val));
                                } catch (Exception ignored) {}
                            }
                        }
                        if (!odds.isEmpty()) {
                            oddsCache.put(eventId, odds);
                        }
                    }
                }
            }
        }
    }

    private long parseSwedishDate(String dateStr, String timeStr) {
        if (dateStr == null) dateStr = "";
        if (timeStr == null) timeStr = "";
        
        String cleanDate = dateStr.toLowerCase().trim();
        String cleanTime = timeStr.toLowerCase().trim();

        if (cleanDate.contains("live") || cleanTime.contains("live") || (cleanDate.isEmpty() && cleanTime.isEmpty())) {
            return System.currentTimeMillis();
        }

        try {
            int currentYear = java.time.LocalDate.now().getYear();
            int month = java.time.LocalDate.now().getMonthValue();
            int day = java.time.LocalDate.now().getDayOfMonth();
            int hour = 12;
            int minute = 0;

            // Extract time (HH:mm)
            java.util.regex.Matcher timeMatcher = java.util.regex.Pattern.compile("(\\d{2}):(\\d{2})").matcher(cleanTime);
            if (timeMatcher.find()) {
                hour = Integer.parseInt(timeMatcher.group(1));
                minute = Integer.parseInt(timeMatcher.group(2));
            } else {
                timeMatcher = java.util.regex.Pattern.compile("(\\d{2}):(\\d{2})").matcher(cleanDate);
                if (timeMatcher.find()) {
                    hour = Integer.parseInt(timeMatcher.group(1));
                    minute = Integer.parseInt(timeMatcher.group(2));
                }
            }

            // Extract day and month (e.g. "07 jun")
            String[] swMonths = {"jan", "feb", "mar", "apr", "maj", "jun", "jul", "aug", "sep", "okt", "nov", "dec"};
            java.util.regex.Matcher dateMatcher = java.util.regex.Pattern.compile("(\\d+)\\s+([a-zåäö]+)").matcher(cleanDate);
            if (dateMatcher.find()) {
                day = Integer.parseInt(dateMatcher.group(1));
                String monthName = dateMatcher.group(2);
                for (int i = 0; i < swMonths.length; i++) {
                    if (monthName.startsWith(swMonths[i])) {
                        month = i + 1;
                        break;
                    }
                }
            }

            java.time.LocalDateTime ldt = java.time.LocalDateTime.of(currentYear, month, day, hour, minute);
            return ldt.atZone(java.time.ZoneId.of("Europe/Stockholm")).toInstant().toEpochMilli();
        } catch (Exception e) {
            return System.currentTimeMillis() + 3600000; // fallback: starts in 1 hour
        }
    }

    private String generateEventId(String sport, String team1, String team2, long startTime) {
        try {
            String input = sport + "|" + team1 + "|" + team2 + "|" + startTime;
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return "b365_" + hexString.substring(0, 16);
        } catch (Exception e) {
            return "b365_" + Math.abs((team1 + team2 + startTime).hashCode());
        }
    }

    @Getter
    @Setter
    public static class Bet365EventsResponse {
        private List<Bet365Event> events;
    }

    @Getter
    @Setter
    public static class Bet365Event {
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
    public static class Bet365OddsResponse {
        private String eventId;
        private List<Bet365Odd> odds;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Bet365Odd {
        private String code;
        private String marketName;
        private String outcomeName;
        private double value;
    }
}
