package pro.datawiki.igaming.capture.esports.service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.UnmappedAnomalyEvent;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.dto.sport.esports.Cs2Sport;
import pro.datawiki.igaming.dto.sport.esports.Dota2Sport;
import pro.datawiki.igaming.dto.sport.esports.ValorantSport;
import pro.datawiki.igaming.mdm.client.MdmIngestionClient;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * MDM Top-Down Ingestion Blueprint integration for Esports (CS2, Dota 2, Valorant).
 * Provides:
 * 1. O(1) in-memory lookup against MDM IngestionBlueprint.
 * 2. Polymorphic integration with Cs2Sport, Dota2Sport, ValorantSport.
 * 3. Anomaly dispatch (UnmappedAnomalyEvent) to mdm.anomaly.errors topic when entities are unmapped.
 * 4. Ground-truth mdm_match_id resolution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EsportsMdmService {

    private final MdmIngestionClient mdmIngestionClient;
    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    @Value("${app.bookmaker.code:esports}")
    private String systemCode = "esports";

    @Value("${app.kafka.topics.anomaly-errors:mdm.anomaly.errors}")
    private String anomalyTopic = "mdm.anomaly.errors";

    @Value("${app.kafka.enabled:true}")
    private boolean kafkaEnabled = true;

    /**
     * Resolves canonical MDM IDs for an esports match using O(1) Blueprint caching.
     */
    public MatchResolutionResult resolveMatch(
            String game,
            String rawTournament,
            String rawTeam1,
            String rawTeam2,
            Long matchDateEpoch
    ) {
        if (game == null || rawTeam1 == null || rawTeam2 == null) {
            return MatchResolutionResult.unmapped("Invalid match parameters");
        }

        String activeCode = (systemCode != null && !systemCode.isBlank()) ? systemCode : "esports";

        // 1. Identify polymorphic Sport instance
        Sport sportInstance = resolveSportInstance(game);
        String sportSlug = sportInstance.getSlug();

        // 2. Load latest IngestionBlueprint from MDM cache
        MdmIngestionClient.IngestionBlueprint blueprint = mdmIngestionClient.getBlueprint(activeCode);
        if (blueprint == null) {
            blueprint = new MdmIngestionClient.IngestionBlueprint();
        }

        // 3. Resolve Sport in Blueprint
        MdmIngestionClient.CanonicalSport sport = blueprint.sports.get(game);
        if (sport == null) {
            sport = blueprint.sportsBySlug.get(sportSlug);
        }
        if (sport == null) {
            sport = blueprint.sportsBySlug.get("esports");
        }

        if (sport == null) {
            reportAnomaly("SPORT", game, game, null, "Unmapped esports game: " + game);
            return MatchResolutionResult.unmapped("Sport unmapped: " + game);
        }

        // 4. Resolve Home Team (Team 1) in O(1)
        MdmIngestionClient.CanonicalTeam homeTeam = lookupTeam(blueprint, rawTeam1);
        if (homeTeam == null) {
            reportAnomaly("TEAM", rawTeam1, rawTeam1, rawTournament, "Unmapped esports team: " + rawTeam1);
        }

        // 5. Resolve Away Team (Team 2) in O(1)
        MdmIngestionClient.CanonicalTeam awayTeam = lookupTeam(blueprint, rawTeam2);
        if (awayTeam == null) {
            reportAnomaly("TEAM", rawTeam2, rawTeam2, rawTournament, "Unmapped esports team: " + rawTeam2);
        }

        // 6. Resolve Tournament in O(1)
        MdmIngestionClient.CanonicalTournament tournament = lookupTournament(blueprint, rawTournament);
        if (tournament == null && rawTournament != null && !rawTournament.isBlank()) {
            reportAnomaly("TOURNAMENT", rawTournament, rawTournament, game, "Unmapped esports tournament: " + rawTournament);
        }

        // If either team is not mapped, match remains UNMAPPED
        if (homeTeam == null || awayTeam == null) {
            return MatchResolutionResult.builder()
                    .mapped(false)
                    .homeTeamId(homeTeam != null ? homeTeam.teamId() : null)
                    .awayTeamId(awayTeam != null ? awayTeam.teamId() : null)
                    .reason("One or both teams unmapped in MDM")
                    .build();
        }

        // 7. Resolve canonical Match ID via MDM Client
        Long tournamentId = tournament != null ? tournament.tournamentId() : null;
        Optional<Long> matchIdOpt = mdmIngestionClient.resolveMatchId(
                systemCode,
                sport.sportId(),
                tournamentId,
                homeTeam.teamId(),
                awayTeam.teamId(),
                matchDateEpoch
        );

        if (matchIdOpt.isPresent()) {
            log.info("🎯 [MDM-MAPPED] Successfully matched esports fixture: {} vs {} -> mdm_match_id={}",
                    rawTeam1, rawTeam2, matchIdOpt.get());
            return MatchResolutionResult.builder()
                    .mapped(true)
                    .matchId(matchIdOpt.get())
                    .homeTeamId(homeTeam.teamId())
                    .awayTeamId(awayTeam.teamId())
                    .tournamentId(tournamentId)
                    .sportId(sport.sportId())
                    .build();
        } else {
            reportAnomaly("MATCH", rawTeam1 + " vs " + rawTeam2, rawTournament, null,
                    "Failed to resolve mdm_match_id for pair " + homeTeam.teamId() + " vs " + awayTeam.teamId());
            return MatchResolutionResult.builder()
                    .mapped(false)
                    .homeTeamId(homeTeam.teamId())
                    .awayTeamId(awayTeam.teamId())
                    .tournamentId(tournamentId)
                    .reason("Match ID resolution failed")
                    .build();
        }
    }

    private MdmIngestionClient.CanonicalTeam lookupTeam(MdmIngestionClient.IngestionBlueprint bp, String rawName) {
        if (rawName == null || rawName.isBlank()) return null;
        MdmIngestionClient.CanonicalTeam team = bp.teams.get(rawName);
        if (team == null) {
            team = bp.teams.get(rawName.trim().toLowerCase());
        }
        if (team == null) {
            team = bp.teamsByName.get(rawName.trim().toLowerCase());
        }
        return team;
    }

    private MdmIngestionClient.CanonicalTournament lookupTournament(MdmIngestionClient.IngestionBlueprint bp, String rawName) {
        if (rawName == null || rawName.isBlank()) return null;
        MdmIngestionClient.CanonicalTournament t = bp.tournaments.get(rawName);
        if (t == null) {
            t = bp.tournaments.get(rawName.trim().toLowerCase());
        }
        if (t == null) {
            t = bp.tournamentsByName.get(rawName.trim().toLowerCase());
        }
        return t;
    }

    /**
     * Polymorphically maps game name string to Sport domain class from igaming-dto.
     */
    public Sport resolveSportInstance(String game) {
        if (game == null) return Sports.GENERIC_ESPORTS;
        String normalized = game.trim().toUpperCase();
        return switch (normalized) {
            case "CS2", "CS:GO", "COUNTER-STRIKE 2", "COUNTER-STRIKE" -> Sports.CS2;
            case "DOTA2", "DOTA 2", "DOTA" -> Sports.DOTA2;
            case "VALORANT", "VAL" -> Sports.VALORANT;
            default -> Sports.GENERIC_ESPORTS;
        };
    }

    /**
     * Dispatches UnmappedAnomalyEvent to Kafka topic 'mdm.anomaly.errors' and mdmIngestionClient.
     */
    public void reportAnomaly(String entityType, String rawKey, String rawName, String rawParentKey, String details) {
        String activeCode = (systemCode != null && !systemCode.isBlank()) ? systemCode : "esports";
        // 1. Report via MdmIngestionClient
        mdmIngestionClient.reportAnomaly(activeCode, entityType, rawKey, rawName, rawParentKey, details);

        // 2. Also emit typed UnmappedAnomalyEvent directly to Kafka if configured
        if (kafkaEnabled && kafkaTemplate.isPresent()) {
            try {
                UnmappedAnomalyEvent event = UnmappedAnomalyEvent.builder()
                        .externalSystemCode(activeCode)
                        .entityType(entityType)
                        .rawKey(rawKey)
                        .rawName(rawName)
                        .rawParentKey(rawParentKey)
                        .detectedAt(OffsetDateTime.now())
                        .details(details)
                        .build();

                String messageKey = activeCode + ":" + entityType + ":" + (rawKey != null ? rawKey : rawName);
                kafkaTemplate.get().send(anomalyTopic, messageKey, event);
                log.debug("📡 [Anomaly-Kafka] Sent UnmappedAnomalyEvent to topic {}: {}", anomalyTopic, messageKey);
            } catch (Exception e) {
                log.warn("Failed to dispatch UnmappedAnomalyEvent to Kafka: {}", e.getMessage());
            }
        }
    }

    public void refreshBlueprint() {
        String activeCode = (systemCode != null && !systemCode.isBlank()) ? systemCode : "esports";
        log.info("🔄 [MDM] Refreshing IngestionBlueprint cache for systemCode '{}'...", activeCode);
        mdmIngestionClient.invalidateCache(activeCode);
    }

    /**
     * Propagates captured match score into MDM Ground Truth consensus flusher.
     */
    public void syncMatchScore(Long matchId, String statusStr, Integer score1, Integer score2) {
        if (matchId == null || score1 == null || score2 == null) return;
        try {
            pro.datawiki.igaming.dto.live.MatchStatus status = "FINISHED".equalsIgnoreCase(statusStr)
                    ? pro.datawiki.igaming.dto.live.MatchStatus.FINISHED
                    : pro.datawiki.igaming.dto.live.MatchStatus.LIVE;

            pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto scoreDto = pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto.builder()
                    .matchId(matchId)
                    .status(status)
                    .score1(score1)
                    .score2(score2)
                    .regularScore1(score1)
                    .regularScore2(score2)
                    .build();

            mdmIngestionClient.submitLiveScore(matchId, scoreDto);
            log.info("🎯 [MDM-SCORE-SYNC] Propagated score {}:{} ({}) for match #{} to MDM", score1, score2, status, matchId);
        } catch (Exception e) {
            log.warn("Failed to sync score for match #{}: {}", matchId, e.getMessage());
        }
    }

    @Data
    @Builder
    public static class MatchResolutionResult {
        private boolean mapped;
        private Long matchId;
        private Long homeTeamId;
        private Long awayTeamId;
        private Long tournamentId;
        private Integer sportId;
        private String reason;

        public static MatchResolutionResult unmapped(String reason) {
            return MatchResolutionResult.builder().mapped(false).reason(reason).build();
        }
    }
}
