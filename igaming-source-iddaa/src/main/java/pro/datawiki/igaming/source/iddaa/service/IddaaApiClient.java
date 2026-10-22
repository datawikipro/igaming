package pro.datawiki.igaming.source.iddaa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.iddaa.dto.IddaaEventDto;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class IddaaApiClient {

    private final ObjectMapper objectMapper;

    @Value("${app.iddaa.bulletin.url:https://bulten.nesine.com/api/bulten/getprebultenfull}")
    private String bulletinUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final Map<Integer, String> SPORTS_MAP = Map.of(
            1, "Soccer",
            2, "Basketball",
            4, "Volleyball",
            5, "Handball",
            6, "Tennis",
            16, "Table Tennis",
            23, "Ice Hockey",
            137, "Snooker",
            153, "Darts"
    );

    public List<IddaaEventDto> fetchActiveEvents() {
        try {
            log.info("Fetching Iddaa bulletin from {}", bulletinUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(bulletinUrl))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate")
                    .timeout(Duration.ofSeconds(45))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                log.warn("Failed to fetch Iddaa bulletin, HTTP status: {}", response.statusCode());
                return Collections.emptyList();
            }

            InputStream is = response.body();
            String encoding = response.headers().firstValue("Content-Encoding").orElse("");
            if ("gzip".equalsIgnoreCase(encoding)) {
                is = new GZIPInputStream(is);
            }

            JsonNode root = objectMapper.readTree(is);
            JsonNode ea = root.path("sg").path("EA");
            if (!ea.isArray()) {
                log.warn("Iddaa bulletin missing sg.EA array");
                return Collections.emptyList();
            }

            List<IddaaEventDto> events = new ArrayList<>();
            for (JsonNode ev : ea) {
                int type = ev.path("TYPE").asInt(-1);
                String hn = ev.path("HN").asText(null);
                String an = ev.path("AN").asText(null);

                if (type <= 0 || hn == null || an == null || hn.isBlank() || an.isBlank()) {
                    continue;
                }

                String id = String.valueOf(ev.path("C").asLong(ev.path("EV").asLong(0)));
                if ("0".equals(id)) continue;

                int gt = ev.path("GT").asInt(1);
                String sport = SPORTS_MAP.getOrDefault(gt, "Soccer");
                String eno = ev.path("ENO").asText(hn + " - " + an);
                String league = ev.path("LN").asText("General");
                long esd = ev.path("ESD").asLong(System.currentTimeMillis() + 3600000L);
                boolean isLive = ev.path("LE").asInt(0) == 1 && ev.path("P").asInt(0) > 0;

                Map<String, Double> odds = extractOdds(ev.path("MA"));
                if (odds.isEmpty()) {
                    continue;
                }

                IddaaEventDto dto = IddaaEventDto.builder()
                        .id(id)
                        .name(eno)
                        .sport(sport)
                        .leagueName(league)
                        .homeTeam(hn)
                        .awayTeam(an)
                        .startTime(esd)
                        .isLive(isLive)
                        .odds(odds)
                        .build();

                events.add(dto);
            }

            log.info("Successfully parsed {} active Iddaa matches with odds from bulletin", events.size());
            return events;

        } catch (Exception e) {
            log.error("Failed to fetch/parse events from Iddaa: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Map<String, Double> extractOdds(JsonNode ma) {
        Map<String, Double> odds = new HashMap<>();
        if (!ma.isArray()) return odds;

        for (JsonNode mkt : ma) {
            int mtid = mkt.path("MTID").asInt(0);
            double sov = mkt.path("SOV").asDouble(0.0);
            JsonNode oca = mkt.path("OCA");
            if (!oca.isArray()) continue;

            // 1X2 Match Result
            if (mtid == 1 || (isThreeWayMtid(mtid) && oca.size() == 3)) {
                for (JsonNode oc : oca) {
                    int n = oc.path("N").asInt(0);
                    double o = oc.path("O").asDouble(0.0);
                    if (o <= 1.0) continue;
                    if (n == 1) odds.putIfAbsent("1", o);
                    else if (n == 2) odds.putIfAbsent("X", o);
                    else if (n == 3) odds.putIfAbsent("2", o);
                }
            }
            // 12 Moneyline
            else if (isMoneylineMarket(mtid, oca)) {
                for (JsonNode oc : oca) {
                    int n = oc.path("N").asInt(0);
                    double o = oc.path("O").asDouble(0.0);
                    if (o <= 1.0) continue;
                    if (n == 1) odds.putIfAbsent("1", o);
                    else if (n == 2) odds.putIfAbsent("2", o);
                }
            }
            // Double Chance
            else if (mtid == 3) {
                for (JsonNode oc : oca) {
                    int n = oc.path("N").asInt(0);
                    double o = oc.path("O").asDouble(0.0);
                    if (o <= 1.0) continue;
                    if (n == 1) odds.putIfAbsent("1X", o);
                    else if (n == 2) odds.putIfAbsent("12", o);
                    else if (n == 3) odds.putIfAbsent("X2", o);
                }
            }
            // Totals
            else if (isTotalMarket(mtid, sov, oca)) {
                double line = sov > 0 ? sov : (mtid == 12 ? 2.5 : (mtid == 11 ? 1.5 : (mtid == 13 ? 3.5 : 0.0)));
                if (line > 0) {
                    for (JsonNode oc : oca) {
                        int n = oc.path("N").asInt(0);
                        double o = oc.path("O").asDouble(0.0);
                        if (o <= 1.0) continue;
                        if (n == 1) odds.putIfAbsent("TM_" + line, o);
                        else if (n == 2) odds.putIfAbsent("TB_" + line, o);
                    }
                }
            }
            // BTTS
            else if (mtid == 49 || mtid == 38) {
                for (JsonNode oc : oca) {
                    int n = oc.path("N").asInt(0);
                    double o = oc.path("O").asDouble(0.0);
                    if (o <= 1.0) continue;
                    if (n == 1) odds.putIfAbsent("GG_YES", o);
                    else if (n == 2) odds.putIfAbsent("GG_NO", o);
                }
            }
        }
        return odds;
    }

    private boolean isThreeWayMtid(int mtid) {
        return mtid == 351 || mtid == 220 || mtid == 222 || mtid == 730 || mtid == 731 || mtid == 734;
    }

    private boolean isMoneylineMarket(int mtid, JsonNode oca) {
        if (oca.size() != 2) return false;
        return mtid == 142 || mtid == 179 || mtid == 182 || mtid == 376 || mtid == 363 ||
               mtid == 432 || mtid == 434 || mtid == 436 || mtid == 437 || mtid == 528 ||
               mtid == 529 || mtid == 810 || mtid == 811 || mtid == 845;
    }

    private boolean isTotalMarket(int mtid, double sov, JsonNode oca) {
        if (oca.size() != 2) return false;
        if (sov > 0) return true;
        return mtid == 11 || mtid == 12 || mtid == 13 || mtid == 149 || mtid == 150 ||
               mtid == 196 || mtid == 216 || mtid == 218 || mtid == 243 || mtid == 248 ||
               mtid == 258 || mtid == 301 || mtid == 312 || mtid == 326 || mtid == 327 ||
               mtid == 328 || mtid == 329 || mtid == 333 || mtid == 364 || mtid == 420 ||
               mtid == 446 || mtid == 544 || mtid == 545 || mtid == 664 || mtid == 672 ||
               mtid == 791 || mtid == 812 || mtid == 813 || mtid == 846 || mtid == 847 ||
               mtid == 848 || mtid == 849 || mtid == 852;
    }
}
