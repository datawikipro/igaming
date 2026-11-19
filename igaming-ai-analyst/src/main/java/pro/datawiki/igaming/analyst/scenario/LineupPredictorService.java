package pro.datawiki.igaming.analyst.scenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.dto.ProjectedLineupDto;
import pro.datawiki.igaming.analyst.dto.ProjectedPlayerDto;

import java.util.*;

/**
 * Pre-Match Lineup Prediction Engine:
 * Forecasts starting XI, bench assets, and team formations up to 24 hours prior to kickoff.
 * Leverages appearance frequency, formation modal detection, injury filters, and duel analysis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineupPredictorService {

    private final ObjectMapper objectMapper;

    private static class PlayerAccumulator {
        String name;
        String position;
        Integer jerseyNumber;
        int starts = 0;
        int bench = 0;
        List<Double> ratings = new ArrayList<>();
        boolean isMissing = false;
        String missingReason = "";
    }

    /**
     * Forecasts starting lineup and bench from historical match lineup JSON strings.
     *
     * @param teamName     canonical team name
     * @param historyRows  list of historical lineup JSON objects (from sofascore_raw_events)
     * @return ProjectedLineupDto containing starters, bench, and contested duels
     */
    public ProjectedLineupDto forecastLineup(String teamName, List<Map<String, Object>> historyRows) {
        if (historyRows == null || historyRows.isEmpty()) {
            return buildDefaultLineup(teamName);
        }

        Map<String, Integer> formationCounts = new HashMap<>();
        Map<String, PlayerAccumulator> players = new HashMap<>();

        for (Map<String, Object> row : historyRows) {
            String lineupsJson = (String) row.get("lineups_json");
            String homeTeam = (String) row.get("home_team");
            boolean isHome = teamName != null && homeTeam != null &&
                    teamName.trim().equalsIgnoreCase(homeTeam.trim());

            if (lineupsJson == null || lineupsJson.isBlank() || lineupsJson.contains("\"error\"")) {
                continue;
            }

            try {
                JsonNode root = objectMapper.readTree(lineupsJson);
                JsonNode side = isHome ? root.path("home") : root.path("away");
                if (side.isMissingNode()) continue;

                String form = side.path("formation").asText("4-4-2");
                if (!form.isBlank()) {
                    formationCounts.put(form, formationCounts.getOrDefault(form, 0) + 1);
                }

                // Process missing players
                JsonNode missing = side.path("missingPlayers");
                if (missing.isArray()) {
                    for (JsonNode m : missing) {
                        String mName = m.path("player").path("name").asText();
                        if (!mName.isBlank()) {
                            PlayerAccumulator acc = players.computeIfAbsent(mName.toLowerCase(Locale.ROOT), k -> {
                                PlayerAccumulator p = new PlayerAccumulator();
                                p.name = mName;
                                return p;
                            });
                            acc.isMissing = true;
                            acc.missingReason = m.path("reason").asText("Injured/Suspended");
                        }
                    }
                }

                // Process active players
                JsonNode plArray = side.path("players");
                if (plArray.isArray()) {
                    for (JsonNode p : plArray) {
                        JsonNode pl = p.path("player");
                        String pName = pl.path("name").asText();
                        if (pName.isBlank()) continue;

                        String pos = pl.path("position").asText("M").toUpperCase(Locale.ROOT);
                        int jersey = pl.path("jerseyNumber").asInt(0);
                        boolean isSub = p.path("substitute").asBoolean(false);
                        Double rating = p.path("statistics").has("rating") ?
                                p.path("statistics").path("rating").asDouble() : null;

                        PlayerAccumulator acc = players.computeIfAbsent(pName.toLowerCase(Locale.ROOT), k -> {
                            PlayerAccumulator pa = new PlayerAccumulator();
                            pa.name = pName;
                            pa.position = pos;
                            pa.jerseyNumber = jersey;
                            return pa;
                        });

                        if (isSub) {
                            acc.bench++;
                        } else {
                            acc.starts++;
                        }
                        if (rating != null && rating > 0) {
                            acc.ratings.add(rating);
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Failed parsing historical lineup entry: {}", e.getMessage());
            }
        }

        // Determine modal formation
        String bestFormation = formationCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("4-4-2");

        // Compute scores and sort by positional groups
        Map<String, List<ProjectedPlayerDto>> byPos = new HashMap<>();
        byPos.put("G", new ArrayList<>());
        byPos.put("D", new ArrayList<>());
        byPos.put("M", new ArrayList<>());
        byPos.put("F", new ArrayList<>());

        int totalMatches = historyRows.size();

        for (PlayerAccumulator acc : players.values()) {
            if (acc.isMissing) {
                continue; // Guard against injured/suspended players
            }

            double avgRating = acc.ratings.isEmpty() ? 6.75 :
                    acc.ratings.stream().mapToDouble(Double::doubleValue).average().orElse(6.75);

            double startProb = totalMatches > 0 ? (double) acc.starts / totalMatches : 0.5;
            startProb = Math.min(0.98, Math.max(0.05, startProb));

            boolean isJoker = (acc.bench >= 2 && avgRating >= 7.0) || ("F".equals(acc.position) && acc.bench > acc.starts && avgRating >= 6.9);

            ProjectedPlayerDto dto = ProjectedPlayerDto.builder()
                    .name(acc.name)
                    .position(acc.position != null ? acc.position : "M")
                    .jerseyNumber(acc.jerseyNumber)
                    .rating(Math.round(avgRating * 100.0) / 100.0)
                    .startProbability(Math.round(startProb * 100.0) / 100.0)
                    .startsCount(acc.starts)
                    .benchCount(acc.bench)
                    .isJoker(isJoker)
                    .build();

            String group = "G".equals(dto.getPosition()) ? "G" :
                    ("D".equals(dto.getPosition()) ? "D" :
                            ("F".equals(dto.getPosition()) ? "F" : "M"));
            byPos.computeIfAbsent(group, k -> new ArrayList<>()).add(dto);
        }

        // Sort descending by start probability then rating
        byPos.values().forEach(list -> list.sort(Comparator
                .comparingDouble(ProjectedPlayerDto::getStartProbability)
                .thenComparingDouble(ProjectedPlayerDto::getRating)
                .reversed()));

        // Parse formation slots (e.g. 4-3-3, 4-2-3-1, 3-5-2)
        int dSlots = 4;
        int mSlots = 4;
        int fSlots = 2;

        String[] parts = bestFormation.split("-");
        try {
            if (parts.length == 3) {
                dSlots = Integer.parseInt(parts[0]);
                mSlots = Integer.parseInt(parts[1]);
                fSlots = Integer.parseInt(parts[2]);
            } else if (parts.length == 4) {
                dSlots = Integer.parseInt(parts[0]);
                mSlots = Integer.parseInt(parts[1]) + Integer.parseInt(parts[2]);
                fSlots = Integer.parseInt(parts[3]);
            }
        } catch (Exception ignored) {}

        List<ProjectedPlayerDto> starters = new ArrayList<>();
        Set<String> used = new HashSet<>();
        List<String> contestedDuels = new ArrayList<>();

        // 1. Goalkeeper (1)
        List<ProjectedPlayerDto> gks = byPos.get("G");
        if (!gks.isEmpty()) {
            starters.add(gks.get(0));
            used.add(gks.get(0).getName());
            if (gks.size() > 1 && Math.abs(gks.get(0).getStartProbability() - gks.get(1).getStartProbability()) <= 0.20) {
                contestedDuels.add(String.format("GK: %s (%.0f%%) vs %s (%.0f%%)",
                        gks.get(0).getName(), gks.get(0).getStartProbability() * 100,
                        gks.get(1).getName(), gks.get(1).getStartProbability() * 100));
            }
        }

        // 2. Defenders
        selectSlots(byPos.get("D"), dSlots, starters, used, contestedDuels, "DEF");

        // 3. Midfielders
        selectSlots(byPos.get("M"), mSlots, starters, used, contestedDuels, "MID");

        // 4. Forwards
        selectSlots(byPos.get("F"), fSlots, starters, used, contestedDuels, "FWD");

        // Fill remaining up to 11 if any category lacked players
        if (starters.size() < 11) {
            List<ProjectedPlayerDto> allUnused = new ArrayList<>();
            byPos.values().forEach(l -> l.stream().filter(p -> !used.contains(p.getName())).forEach(allUnused::add));
            allUnused.sort(Comparator.comparingDouble(ProjectedPlayerDto::getStartProbability).reversed());

            for (ProjectedPlayerDto p : allUnused) {
                if (starters.size() >= 11) break;
                starters.add(p);
                used.add(p.getName());
            }
        }

        // Bench candidates
        List<ProjectedPlayerDto> bench = new ArrayList<>();
        byPos.values().forEach(l -> l.stream().filter(p -> !used.contains(p.getName())).forEach(bench::add));
        bench.sort(Comparator.comparingDouble(ProjectedPlayerDto::getRating).reversed());
        List<ProjectedPlayerDto> topBench = bench.stream().limit(7).toList();

        double avgProb = starters.stream().mapToDouble(ProjectedPlayerDto::getStartProbability).average().orElse(0.80);

        return ProjectedLineupDto.builder()
                .teamName(teamName)
                .formation(bestFormation)
                .starters(starters)
                .bench(topBench)
                .confidencePct(Math.round(avgProb * 1000.0) / 10.0)
                .contestedDuels(contestedDuels)
                .build();
    }

    private void selectSlots(List<ProjectedPlayerDto> pool, int slots,
                             List<ProjectedPlayerDto> target, Set<String> used,
                             List<String> duels, String posLabel) {
        if (pool == null) return;
        int taken = 0;
        for (int i = 0; i < pool.size() && taken < slots; i++) {
            ProjectedPlayerDto p = pool.get(i);
            if (!used.contains(p.getName())) {
                target.add(p);
                used.add(p.getName());
                taken++;

                // Check contested boundary on the final slot
                if (taken == slots && (i + 1) < pool.size()) {
                    ProjectedPlayerDto next = pool.get(i + 1);
                    if (Math.abs(p.getStartProbability() - next.getStartProbability()) <= 0.20) {
                        p.setContested(true);
                        p.setContenderName(next.getName());
                        duels.add(String.format("%s: %s (%.0f%%) vs %s (%.0f%%)",
                                posLabel, p.getName(), p.getStartProbability() * 100,
                                next.getName(), next.getStartProbability() * 100));
                    }
                }
            }
        }
    }

    private ProjectedLineupDto buildDefaultLineup(String teamName) {
        return ProjectedLineupDto.builder()
                .teamName(teamName)
                .formation("4-4-2")
                .starters(Collections.emptyList())
                .bench(Collections.emptyList())
                .confidencePct(50.0)
                .contestedDuels(Collections.emptyList())
                .build();
    }
}
