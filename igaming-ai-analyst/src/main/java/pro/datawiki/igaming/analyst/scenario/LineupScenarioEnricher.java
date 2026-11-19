package pro.datawiki.igaming.analyst.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.service.MlInferenceClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles enrichment and serialization of projected starting lineups, bench assets,
 * rotation resilience, and 9-scenario stress test simulations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LineupScenarioEnricher {

    private final ObjectMapper objectMapper;
    private final MlInferenceClient mlInferenceClient;
    private final MatchAiRecommendationRepository recommendationRepository;

    /**
     * Enriches recommendation with lineup resilience, key dependencies, and projected scenario JSON.
     */
    public void enrichRecommendationWithLineupAndScenarios(MatchAiRecommendation rec, MlPredictionDto ml) {
        if (rec == null || ml == null) return;

        if (ml.getLineupResiliencePct() != null) {
            rec.setLineupResiliencePct(ml.getLineupResiliencePct());
        }

        if (ml.getKeyPlayerDependencies() != null && !ml.getKeyPlayerDependencies().isEmpty()) {
            rec.setKeyPlayerDependencies(String.join("; ", ml.getKeyPlayerDependencies()));
        }

        try {
            Map<String, Object> data = new HashMap<>();
            if (ml.getProjectedStartersHome() != null) data.put("home_starters", ml.getProjectedStartersHome());
            if (ml.getProjectedStartersAway() != null) data.put("away_starters", ml.getProjectedStartersAway());
            if (ml.getProjectedBenchHome() != null) data.put("home_bench", ml.getProjectedBenchHome());
            if (ml.getProjectedBenchAway() != null) data.put("away_bench", ml.getProjectedBenchAway());
            if (ml.getScenariosDetails() != null) data.put("scenarios", ml.getScenariosDetails());
            if (ml.getWorstCaseFloor() != null) data.put("worst_case_floor", ml.getWorstCaseFloor());

            if (!data.isEmpty()) {
                rec.setProjectedLineupJson(objectMapper.writeValueAsString(data));
            }
        } catch (Exception e) {
            log.warn("Failed to serialize projected lineup data: {}", e.getMessage());
        }
    }

    /**
     * On-demand dynamic generation and persistence of projected lineups & 9-scenario simulation
     * for recommendations where lineups have not been generated yet.
     */
    public Optional<MatchAiRecommendation> enrichAndPersistProjectedScenarios(MatchAiRecommendation rec) {
        if (rec == null) return Optional.empty();

        if (rec.getProjectedLineupJson() != null && !rec.getProjectedLineupJson().isBlank()) {
            return Optional.of(rec);
        }

        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("sport_name", rec.getSportName() != null ? rec.getSportName() : "FOOTBALL");
            meta.put("team1_name", rec.getTeam1Name());
            meta.put("team2_name", rec.getTeam2Name());
            MlPredictionDto ml = mlInferenceClient.predict(rec.getMatchId() != null ? rec.getMatchId() : 0L, meta);

            if (ml != null && Boolean.TRUE.equals(ml.getIsLineupProjected())) {
                enrichRecommendationWithLineupAndScenarios(rec, ml);
                rec.setLineupStatus("PROJECTED");
                MatchAiRecommendation saved = recommendationRepository.save(rec);
                log.info("Dynamically generated & persisted projected scenarios for rec #{} (Match #{})", rec.getId(), rec.getMatchId());
                return Optional.of(saved);
            }
        } catch (Exception e) {
            log.warn("On-the-fly projected scenario generation failed for rec #{}: {}", rec.getId(), e.getMessage());
        }

        return Optional.of(rec);
    }
}
