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
            Elements tables = doc.select("table.oddsTable");
            if (tables.isEmpty()) {
                log.debug("No oddsTable found for BookMaker sport '{}'", sportSlug);
                return matches;
            }

            for (Element table : tables) {
                matches.addAll(parseOddsTable(table, sportSlug, url));
            }

            log.info("Parsed {} matches for BookMaker sport '{}'", matches.size(), sportSlug);
        } catch (Exception e) {
            log.error("Failed to fetch/parse BookMaker sport '{}': {}", sportSlug, e.getMessage());
        }

        return matches;
    }

    private List<BookmakerMatchDto> parseOddsTable(Element table, String sportSlug, String pageUrl) {
        List<BookmakerMatchDto> list = new ArrayList<>();
        Elements rows = table.select("tr");
        String currentLeague = Character.toUpperCase(sportSlug.charAt(0)) + sportSlug.substring(1);

        int i = 0;
        while (i < rows.size()) {
            Element r = rows.get(i);
            Element th = r.selectFirst("th");

            // League title row
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
