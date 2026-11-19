package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.entity.AiStrategyVote;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.AiStrategyVoteRepository;
import pro.datawiki.igaming.dto.radar.RadarOpportunityEvent;
import pro.datawiki.igaming.dto.radar.RadarStrategyVoteDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Publishes real-time Radar opportunities to Kafka ('sports.radar.lineups')
 * when match lineups are officially confirmed and strategies cast PICK votes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RadarEventPublisher {

    public static final String DEFAULT_RADAR_TOPIC = "sports.radar.lineups";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AiStrategyVoteRepository strategyVoteRepository;

    @Value("${igaming.radar.kafka.topic:sports.radar.lineups}")
    private String radarTopic = DEFAULT_RADAR_TOPIC;

    @Value("${igaming.radar.enabled:true}")
    private boolean radarEnabled = true;

    /**
     * Inspects the newly saved match recommendation. If lineups are confirmed
     * and at least one institutional strategy voted PICK, emits a RadarOpportunityEvent.
     */
    public void publishOpportunityIfConfirmed(MatchAiRecommendation rec) {
        if (!radarEnabled || rec == null || rec.getMatchId() == null) {
            return;
        }

        boolean isLineupConfirmed = "LINEUP_CONFIRMED".equalsIgnoreCase(rec.getEvaluationStage())
                || "CONFIRMED".equalsIgnoreCase(rec.getLineupStatus());

        if (!isLineupConfirmed) {
            return;
        }

        try {
            List<AiStrategyVote> votes = rec.getId() != null
                    ? strategyVoteRepository.findByRecommendationId(rec.getId())
                    : strategyVoteRepository.findByMatchId(rec.getMatchId());

            if (votes == null || votes.isEmpty()) {
                log.debug("📡 [RADAR] No strategy votes found yet for match #{}", rec.getMatchId());
                return;
            }

            boolean hasPick = votes.stream().anyMatch(v -> "PICK".equalsIgnoreCase(v.getVoteDecision()));
            if (!hasPick) {
                log.debug("📡 [RADAR] Skipping match #{}: all strategy votes are PASS", rec.getMatchId());
                return;
            }

            Map<String, String> urlsMap = parseBookmakerUrls(rec.getBookmakerUrlsJson());
            List<String> availableBmList = parseAvailableBookmakers(rec.getAvailableBookmakers());

            List<RadarStrategyVoteDto> voteDtos = votes.stream()
                    .map(this::mapVoteToDto)
                    .collect(Collectors.toList());

            RadarOpportunityEvent event = RadarOpportunityEvent.builder()
                    .matchId(rec.getMatchId())
                    .sportSlug(rec.getSportName() != null ? rec.getSportName().toLowerCase() : "football")
                    .sportName(rec.getSportName())
                    .team1Name(rec.getTeam1Name())
                    .team2Name(rec.getTeam2Name())
                    .leagueName(rec.getLeagueName())
                    .matchStartTime(rec.getMatchStartTime())
                    .lineupStatus(rec.getLineupStatus())
                    .evaluationStage(rec.getEvaluationStage())
                    .strategyVotes(voteDtos)
                    .bookmakerUrls(urlsMap)
                    .availableBookmakers(availableBmList)
                    .publishedAt(LocalDateTime.now())
                    .build();

            String payload = objectMapper.writeValueAsString(event);
            String key = String.valueOf(rec.getMatchId());

            kafkaTemplate.send(radarTopic, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("❌ [RADAR] Failed to send opportunity for match #{}: {}", rec.getMatchId(), ex.getMessage());
                        } else {
                            log.info("📡 [RADAR-KAFKA] Emitted opportunity for match #{} ({}-{}): topic={}, offset={}",
                                    rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(),
                                    radarTopic, result.getRecordMetadata().offset());
                        }
                    });

        } catch (Exception ex) {
            log.error("❌ [RADAR] Error assembling/publishing radar opportunity for match #{}: {}",
                    rec.getMatchId(), ex.getMessage(), ex);
        }
    }

    private RadarStrategyVoteDto mapVoteToDto(AiStrategyVote v) {
        return RadarStrategyVoteDto.builder()
                .strategySlug(v.getStrategySlug())
                .strategyName(v.getStrategyName())
                .market(v.getMarket())
                .marketLabel(v.getMarketLabel())
                .recommendedOdd(v.getRecommendedOdd())
                .fairProbability(v.getFairProbability())
                .expectedValuePct(v.getExpectedValuePct())
                .voteDecision(v.getVoteDecision())
                .build();
    }

    private Map<String, String> parseBookmakerUrls(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception ex) {
            log.warn("Could not parse bookmakerUrlsJson: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private List<String> parseAvailableBookmakers(String str) {
        if (str == null || str.isBlank()) return Collections.emptyList();
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
