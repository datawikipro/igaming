package pro.datawiki.igaming.capture.liveresult.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.ReferenceFixtureDto;
import pro.datawiki.igaming.dto.TeamProfileDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LiveResultFixtureProvider implements MatchFixtureProvider {

    private static final String BASE_URL = "https://www.liveresult.ru";

    @Override
    public String getProviderName() {
        return "LIVERESULT";
    }

    @Override
    public boolean supportsSport(String sportName) {
        if (sportName == null) return false;
        String s = sportName.toUpperCase();
        return s.contains("FOOTBALL") || s.contains("HOCKEY") || s.contains("BASKETBALL");
    }

    @Override
    public List<ReferenceFixtureDto> fetchScheduledFixtures(String sportName, LocalDate date) {
        String sportPath = mapSportToPath(sportName);
        String url = BASE_URL + "/" + sportPath + "/scheduled";

        log.debug("[LiveResult Fixtures] Fetching scheduled matches for sport '{}' from {}", sportName, url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            Elements matchElements = doc.select(".matches-list-item, .match-row, tr.match");
            if (matchElements.isEmpty()) {
                return Collections.emptyList();
            }

            List<ReferenceFixtureDto> result = new ArrayList<>();
            for (Element el : matchElements) {
                try {
                    String team1 = el.select(".team-left, .team-1, .home-team").text().trim();
                    String team2 = el.select(".team-right, .team-2, .away-team").text().trim();
                    String league = el.select(".tournament-name, .league").text().trim();
                    String timeStr = el.select(".match-time, .time").text().trim();

                    if (team1.isEmpty() || team2.isEmpty()) continue;

                    long startEpochMs = 0;
                    if (!timeStr.isEmpty() && timeStr.contains(":")) {
                        String[] parts = timeStr.split(":");
                        int hour = Integer.parseInt(parts[0].trim());
                        int minute = Integer.parseInt(parts[1].trim());
                        LocalDateTime ldt = date.atTime(hour, minute);
                        startEpochMs = ldt.atZone(ZoneId.of("Europe/Moscow")).toInstant().toEpochMilli();
                    }

                    String fixtureId = "lr_" + Math.abs((team1 + "_" + team2 + "_" + date).hashCode());

                    ReferenceFixtureDto dto = ReferenceFixtureDto.builder()
                            .provider(getProviderName())
                            .externalFixtureId(fixtureId)
                            .sport(sportName)
                            .leagueName(league.isEmpty() ? "General League" : league)
                            .team1Name(team1)
                            .team1NameLocal(team1)
                            .team2Name(team2)
                            .team2NameLocal(team2)
                            .startTimeEpochMs(startEpochMs)
                            .status("NOT_STARTED")
                            .build();

                    result.add(dto);

                } catch (Exception inner) {
                    log.debug("[LiveResult Fixtures] Skipping row: {}", inner.getMessage());
                }
            }

            log.info("[LiveResult Fixtures] Successfully ingested {} fixtures for sport '{}'", result.size(), sportName);
            return result;

        } catch (Exception e) {
            log.warn("[LiveResult Fixtures] Error scraping scheduled fixtures for sport '{}': {}", sportName, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<TeamProfileDto> fetchTeamProfile(String externalTeamId, String sportName) {
        return Optional.empty();
    }

    private String mapSportToPath(String sportName) {
        if (sportName == null) return "football";
        String s = sportName.toUpperCase();
        if (s.contains("HOCKEY")) return "hockey";
        if (s.contains("BASKETBALL")) return "basketball";
        return "football";
    }
}
