package pro.datawiki.igaming.source.bookmaker.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.bookmaker.config.BookmakerConfig;
import pro.datawiki.igaming.source.bookmaker.dto.BookmakerMatchDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BookmakerHtmlClient {

    private final RestTemplate restTemplate;
    private final BookmakerConfig config;

    public BookmakerHtmlClient(@Qualifier("bookmakerRestTemplate") RestTemplate restTemplate,
                               BookmakerConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        return headers;
    }

    public List<BookmakerMatchDto> fetchSportMatches(String sportSlug) {
        String url = config.getBaseUrl() + "/en/sports/" + sportSlug + "/";
        List<BookmakerMatchDto> matches = new ArrayList<>();

        try {
            log.debug("Fetching BookMaker sport lines from {}", url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("BookMaker sport '{}' returned status {}", sportSlug, response.getStatusCode());
                return matches;
            }

            Document doc = Jsoup.parse(response.getBody());

            // 1. Modern tables with data-game-row
            Elements gameTbodies = doc.select("tbody[data-game-row]");
            if (!gameTbodies.isEmpty()) {
                matches.addAll(parseModernGameTbodies(gameTbodies, sportSlug, url));
            } else {
                // 2. Legacy fallback
                Elements tables = doc.select("table.oddsTable, table");
                for (Element table : tables) {
                    matches.addAll(parseLegacyOddsTable(table, sportSlug, url));
                }
            }

            log.info("Parsed {} matches for BookMaker sport '{}'", matches.size(), sportSlug);
        } catch (Exception e) {
            log.error("Failed to fetch/parse BookMaker sport '{}': {}", sportSlug, e.getMessage());
        }

        return matches;
    }

    private List<BookmakerMatchDto> parseModernGameTbodies(Elements tbodies, String sportSlug, String pageUrl) {
        List<BookmakerMatchDto> list = new ArrayList<>();

        for (Element tbody : tbodies) {
            try {
                // Determine league from parent table caption
                String league = null;
                Element table = tbody.parent();
                if (table != null) {
                    Element caption = table.selectFirst("caption");
                    if (caption != null && !caption.text().isBlank()) {
                        league = caption.text().replace("betting lines", "").trim();
                    }
                }
                if (league == null || league.isBlank()) {
                    league = Character.toUpperCase(sportSlug.charAt(0)) + sportSlug.substring(1);
                }

                boolean isLive = "1".equals(tbody.attr("data-live"));
                String startsAttr = tbody.attr("data-starts");
                Long startTime = null;
                if (startsAttr != null && !startsAttr.isBlank()) {
                    try {
                        startTime = Instant.parse(startsAttr).toEpochMilli();
                    } catch (Exception ignored) {}
                }

                Elements rows = tbody.select("tr");
                List<Element> teamRows = new ArrayList<>();
                for (Element r : rows) {
                    if (r.hasClass("md:hidden")) continue;
                    if (r.selectFirst("th.bm-team, th[scope=row]") != null) {
                        teamRows.add(r);
                    }
                }

                if (teamRows.size() < 2) {
                    continue;
                }

                Element r1 = teamRows.get(0);
                Element r2 = teamRows.get(1);

                Element th1 = r1.selectFirst("th.bm-team, th[scope=row]");
                Element th2 = r2.selectFirst("th.bm-team, th[scope=row]");
                if (th1 == null || th2 == null) continue;

                String team1 = extractTeamName(th1);
                String team2 = extractTeamName(th2);
                if (team1.isBlank() || team2.isBlank() || "Draw".equalsIgnoreCase(team1) || "Draw".equalsIgnoreCase(team2)) {
                    continue;
                }

                String eventUrl = pageUrl;
                Element link = r1.selectFirst("a[href^=/en/sports/]");
                if (link != null) {
                    String href = link.attr("href");
                    eventUrl = href.startsWith("http") ? href : config.getBaseUrl() + href;
                }

                Elements tds1 = r1.select("td.bm-c");
                Elements tds2 = r2.select("td.bm-c");

                String spread1 = tds1.size() > 0 ? extractChipValue(tds1.get(0)) : null;
                String total = tds1.size() > 1 ? extractChipValue(tds1.get(1)) : null;
                String ml1 = tds1.size() > 2 ? extractChipValue(tds1.get(2)) : null;

                String spread2 = tds2.size() > 0 ? extractChipValue(tds2.get(0)) : null;
                String total2 = tds2.size() > 1 ? extractChipValue(tds2.get(1)) : null;
                String ml2 = tds2.size() > 2 ? extractChipValue(tds2.get(2)) : null;

                if (total == null || total.isBlank()) {
                    total = total2;
                }

                String mlX = null;
                if (teamRows.size() > 2) {
                    Element r3 = teamRows.get(2);
                    Element th3 = r3.selectFirst("th.bm-team, th[scope=row]");
                    if (th3 != null && "Draw".equalsIgnoreCase(th3.text().trim())) {
                        Elements tds3 = r3.select("td.bm-c");
                        if (tds3.size() > 2) {
                            mlX = extractChipValue(tds3.get(2));
                        }
                    }
                }

                String matchId = generateMatchId(sportSlug, league, team1, team2);

                BookmakerMatchDto match = BookmakerMatchDto.builder()
                        .id(matchId)
                        .sport(sportSlug)
                        .league(league)
                        .startTime(startTime)
                        .team1(team1)
                        .team2(team2)
                        .spread1(spread1)
                        .spread2(spread2)
                        .total(total)
                        .ml1(ml1)
                        .ml2(ml2)
                        .mlX(mlX)
                        .eventUrl(eventUrl)
                        .isLive(isLive)
                        .build();

                list.add(match);
            } catch (Exception e) {
                log.debug("Error parsing modern game tbody: {}", e.getMessage());
            }
        }
        return list;
    }

    private String extractTeamName(Element th) {
        Element span = th.selectFirst("span");
        if (span != null && !span.text().isBlank()) {
            return span.text().trim();
        }
        return th.text().trim();
    }

    private String extractChipValue(Element td) {
        Element chip = td.selectFirst("span.bm-chip");
        if (chip == null) {
            String txt = td.text().trim();
            return txt.isEmpty() ? null : txt;
        }
        Element b = chip.selectFirst("b");
        Element small = chip.selectFirst("small");
        String bText = b != null ? b.text().trim() : "";
        String smallText = small != null ? small.text().trim() : "";

        if (!bText.isEmpty() && !smallText.isEmpty()) {
            return bText + " (" + smallText + ")";
        } else if (!bText.isEmpty()) {
            return bText;
        } else if (!smallText.isEmpty()) {
            return smallText;
        }
        return null;
    }

    private List<BookmakerMatchDto> parseLegacyOddsTable(Element table, String sportSlug, String pageUrl) {
        List<BookmakerMatchDto> list = new ArrayList<>();
        Elements rows = table.select("tr");
        String currentLeague = Character.toUpperCase(sportSlug.charAt(0)) + sportSlug.substring(1);

        int i = 0;
        while (i < rows.size()) {
            Element r = rows.get(i);
            Element th = r.selectFirst("th");

            if (th != null && !th.text().contains("Time") && !th.text().contains("Team")) {
                currentLeague = th.text().trim();
                i++;
                continue;
            }

            Element hv1 = r.selectFirst("td.HV.FST");
            if (hv1 != null && (i + 1) < rows.size()) {
                Element r2 = rows.get(i + 1);
                Element hv2 = r2.selectFirst("td.HV.LST");

                if (hv2 != null) {
                    Element tm = r.selectFirst("td.TM");
                    Element sp1 = r.selectFirst("td.SP.FST");
                    Element sp2 = r2.selectFirst("td.SP.LST");
                    Element tt1 = r.selectFirst("td.TT.FST");
                    Element tt2 = r2.selectFirst("td.TT.LST");
                    Element ml1 = r.selectFirst("td.ML.FST");
                    Element ml2 = r2.selectFirst("td.ML.LST");

                    String team1 = hv1.text().trim();
                    String team2 = hv2.text().trim();
                    String timeRaw = tm != null ? tm.text().trim() : "";
                    String spread1 = sp1 != null ? sp1.text().trim() : "";
                    String spread2 = sp2 != null ? sp2.text().trim() : "";
                    String total = tt1 != null ? tt1.text().trim() : (tt2 != null ? tt2.text().trim() : "");
                    String moneyline1 = ml1 != null ? ml1.text().trim() : "";
                    String moneyline2 = ml2 != null ? ml2.text().trim() : "";

                    String matchId = generateMatchId(sportSlug, currentLeague, team1, team2);

                    BookmakerMatchDto match = BookmakerMatchDto.builder()
                            .id(matchId)
                            .sport(sportSlug)
                            .league(currentLeague)
                            .timeRaw(timeRaw)
                            .team1(team1)
                            .team2(team2)
                            .spread1(spread1)
                            .spread2(spread2)
                            .total(total)
                            .ml1(moneyline1)
                            .ml2(moneyline2)
                            .eventUrl(pageUrl)
                            .isLive(timeRaw.toLowerCase().contains("live"))
                            .build();

                    list.add(match);
                    i += 2;
                    continue;
                }
            }
            i++;
        }

        return list;
    }

    private String generateMatchId(String sport, String league, String team1, String team2) {
        try {
            String raw = (sport + "_" + league + "_" + team1 + "_" + team2).toLowerCase().replaceAll("\\s+", "_");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 6; j++) {
                sb.append(String.format("%02x", hash[j]));
            }
            return "bm_" + sb;
        } catch (Exception e) {
            return "bm_" + Math.abs((sport + team1 + team2).hashCode());
        }
    }
}
