package pro.datawiki.igaming.analyst.guard;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.service.AnalystAnomalyReporter;
import pro.datawiki.igaming.analyst.service.RedisOddsConsensusService;
import pro.datawiki.igaming.analyst.util.SlugUtils;
import pro.datawiki.igaming.dto.Language;
import pro.datawiki.igaming.dto.sport.Sport;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Pre-Flight Gatekeeper ensuring data validity, physical reality invariants,
 * schedule collision prevention, and lineup timing discipline before running ML inference.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchPreFlightGatekeeper {

    private final MatchAiRecommendationRepository recommendationRepository;
    private final RedisOddsConsensusService redisOddsConsensusService;
    private final AnalystAnomalyReporter anomalyReporter;
    private final ObjectMapper objectMapper;

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095}")
    private String mdmUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public enum DecisionType {
        PROCEED,
        STOP_AND_PERSIST
    }

    @Getter
    @Builder
    public static class PreFlightDecision {
        private final DecisionType decisionType;
        private final String stage;
        private final MatchAiRecommendation recommendation;

        public boolean shouldProceed() {
            return decisionType == DecisionType.PROCEED;
        }
    }

    /**
     * Executes all pre-flight guards on the match dossier:
     * 1. Unmapped placeholder league guard ("League 9661")
     * 2. Physical reality schedule collision check (team overlap within minimum interval)
     * 3. Solo/individual sport unresolved player entity guard (Zero-Garbage Protocol)
     */
    public PreFlightDecision checkPreFlightGuards(
            Long matchId,
            Map<String, Object> dossierMeta,
            Sport sport,
            MatchAiRecommendation rec,
            boolean bypassConflictCheck
    ) {
        populateMetadata(rec, dossierMeta, sport);

        // ── 0. Canonical Physical Sport Invariant: Exclude proxies, fantasy duels, specials, and cyber simulations ──
        if (sport != null && (!sport.isRealSport() || sport.isStatisticalProxy() || sport.isFantasyDuel() || sport.isSpecials() || sport.isEsports())) {
            log.info("Match #{} belongs to non-real, proxy, or fantasy sport ({}, {}). Excluded from real betting pipeline.",
                    matchId, sport.getCode(), sport.getSlug());
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(0);
            rec.setEvaluationStage("EXCLUDED_NON_REAL_SPORT");
            rec.setErrorCategory("EXCLUDED_NON_REAL_SPORT");
            rec.setLineupStatus("ESTIMATED");
            rec.setCoreHypothesis("ИСКЛЮЧЕНО: Неканонический вид спорта или статистический прокси (" + sport.getDisplayName(Language.RU) + "). В реальной линии не участвует.");
            rec.setIdentifiedRisk("Событие не является реальным спортивным матчем.");
            rec.setTacticalRationale("Квантовый аналитик оценивает исключительно канонический реальный физический спорт.");

            return PreFlightDecision.builder()
                    .decisionType(DecisionType.STOP_AND_PERSIST)
                    .stage("EXCLUDED_NON_REAL_SPORT")
                    .recommendation(rec)
                    .build();
        }

        String league = dossierMeta != null ? (String) dossierMeta.get("league_name") : null;
        String t1 = dossierMeta != null ? (String) dossierMeta.get("team1_name") : null;
        String t2 = dossierMeta != null ? (String) dossierMeta.get("team2_name") : null;

        // ── 0.1 Player Duel & Fantasy Match Invariant ──
        if (t1 != null && t2 != null && (
                t1.toLowerCase().contains(" vs ") || t2.toLowerCase().contains(" vs ")
                || (league != null && (league.toLowerCase().contains("кто забьет") || league.toLowerCase().contains("дуэль") || league.toLowerCase().contains("кто больше") || league.toLowerCase().contains("балтboost")))
        )) {
            log.info("Match #{} ({} vs {}, {}) is a player duel or synthetic fantasy collision. Rejecting from real sports betting pipeline.",
                    matchId, t1, t2, league);
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(0);
            rec.setEvaluationStage("EXCLUDED_FANTASY_DUEL");
            rec.setErrorCategory("EXCLUDED_FANTASY_DUEL");
            rec.setLineupStatus("ESTIMATED");
            rec.setCoreHypothesis("ИСКЛЮЧЕНО: Дуэль игроков или фантазийный матч. Исключено в отдельный фид Specials/Fantasy.");
            rec.setIdentifiedRisk("Событие не является матчем 11 на 11 или классической спортивной встречей команд.");
            rec.setTacticalRationale("Квантовый аналитик оценивает исключительно реальные командные/индивидуальные спортивные матчи.");

            return PreFlightDecision.builder()
                    .decisionType(DecisionType.STOP_AND_PERSIST)
                    .stage("EXCLUDED_FANTASY_DUEL")
                    .recommendation(rec)
                    .build();
        }

        // ── 1. Domain Guard: Unresolved raw placeholder leagues ("League 9661") ──
        if (league != null) {
            String lLower = league.toLowerCase().trim();
            if (lLower.matches("^league\\s*\\d+$")) {
                log.info("Match #{} belongs to unmapped placeholder league ('{}'). Evaluation deferred until MDM resolution.", matchId, league);
                rec.setConvictionTier("PASS");
                rec.setRecommendedMarket("PASS");
                rec.setRecommendedOdd(BigDecimal.ONE);
                rec.setBestBookmaker("NONE");
                rec.setExpectedValuePct(BigDecimal.ZERO);
                rec.setKellyStakePct(BigDecimal.ZERO);
                rec.setConfidenceScore(0);
                
                rec.setEvaluationStage("SKIPPED_UNMAPPED_LEAGUE");
                rec.setLineupStatus("ESTIMATED");
                rec.setCoreHypothesis("НЕРАЗРЕШЕННАЯ ЛИГА-ЗАГЛУШКА (" + league + "). Ожидается маппинг турнира в MDM для получения достоверных стат-метрик.");
                rec.setIdentifiedRisk("Отсутствует профиль турнира в MDM.");
                rec.setTacticalRationale("Аналитический пайплайн требует валидного канонического профиля соревнования для оценки.");

                return PreFlightDecision.builder()
                        .decisionType(DecisionType.STOP_AND_PERSIST)
                        .stage("SKIPPED_UNMAPPED_LEAGUE")
                        .recommendation(rec)
                        .build();
            }
        }

        // ── 2. Physical Reality Invariant: Team Overlap / Schedule Collision Check ──
        Long startMs = dossierMeta != null ? (Long) dossierMeta.get("start_time") : null;

        if (!bypassConflictCheck && startMs != null && (t1 != null || t2 != null)) {
            LocalDateTime matchStartLdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startMs), ZoneOffset.UTC);
            int intervalMinutes = (sport != null) ? sport.getMinimumMatchIntervalMinutes() : 120;
            LocalDateTime windowStart = matchStartLdt.minusMinutes(intervalMinutes);
            LocalDateTime windowEnd = matchStartLdt.plusMinutes(intervalMinutes);
            List<MatchAiRecommendation> activeRecs = recommendationRepository.findByEvaluationStageNotIn(List.of("SETTLED", "CANCELLED", "SKIPPED"));

            String currentSportSlug = (sport != null) ? sport.getSlug() : (dossierMeta != null ? (String) dossierMeta.get("sport_slug") : null);

            for (MatchAiRecommendation existing : activeRecs) {
                if (existing.getMatchId() != null && !existing.getMatchId().equals(matchId) && existing.getMatchStartTime() != null) {
                    // Collision can only happen within the SAME sport discipline!
                    // A club playing football and its satellite playing corners/cards/fouls concurrently is normal!
                    if (currentSportSlug != null && existing.getSportSlug() != null && !currentSportSlug.equalsIgnoreCase(existing.getSportSlug())) {
                        continue;
                    }
                    if (existing.getMatchStartTime().isAfter(windowStart) && existing.getMatchStartTime().isBefore(windowEnd)) {
                        boolean t1Conflict = (t1 != null && (t1.equalsIgnoreCase(existing.getTeam1Name()) || t1.equalsIgnoreCase(existing.getTeam2Name())));
                        boolean t2Conflict = (t2 != null && (t2.equalsIgnoreCase(existing.getTeam1Name()) || t2.equalsIgnoreCase(existing.getTeam2Name())));

                        if (t1Conflict || t2Conflict) {
                            String conflictTeam = t1Conflict ? t1 : t2;
                            log.warn("⚠️ [SCHEDULE_COLLISION] Team '{}' in match #{} ({} vs {}, {}) conflicts with existing match #{} ({} vs {}, {})! Quarantining both and emitting anomaly.",
                                    conflictTeam, matchId, t1, t2, league, existing.getMatchId(), existing.getTeam1Name(), existing.getTeam2Name(), existing.getLeagueName());

                            rec.setConvictionTier("PASS");
                            rec.setRecommendedMarket("PASS");
                            rec.setRecommendedOdd(BigDecimal.ONE);
                            rec.setBestBookmaker("NONE");
                            rec.setExpectedValuePct(BigDecimal.ZERO);
                            rec.setKellyStakePct(BigDecimal.ZERO);
                            rec.setConfidenceScore(0);
                            
                            rec.setEvaluationStage("SCHEDULE_CONFLICT_PENDING_RESOLUTION");
                            rec.setLineupStatus("ESTIMATED");
                            rec.setCoreHypothesis(String.format("КОНФЛИКТ РАСПИСАНИЯ: Команда '%s' задействована в матче #%d и матче #%d. Находится в очереди ошибок на проверку роботом.", conflictTeam, existing.getMatchId(), matchId));
                            rec.setIdentifiedRisk("Физическая невозможность участия одной команды в двух параллельных матчах (дубликат/кросс-матч).");
                            rec.setTacticalRationale(String.format("В соответствии с инвариантом физической реальности Sport (%s), оба матча отправлены в карантин до автоматического разрешения серверным роботом.", sport != null ? sport.getCode() : "SPORT"));

                            // Freeze existing match as well if active
                            if (!"CANCELLED".equalsIgnoreCase(existing.getEvaluationStage()) && !"SETTLED".equalsIgnoreCase(existing.getEvaluationStage())) {
                                existing.setEvaluationStage("SCHEDULE_CONFLICT_PENDING_RESOLUTION");
                                                                existing.setConvictionTier("PASS");
                                existing.setRecommendedMarket("PASS");
                                existing.setRecommendedOdd(BigDecimal.ONE);
                                existing.setExpectedValuePct(BigDecimal.ZERO);
                                existing.setKellyStakePct(BigDecimal.ZERO);
                                existing.setCoreHypothesis(String.format("КОНФЛИКТ РАСПИСАНИЯ: Команда '%s' задействована в матче #%d и матче #%d. Находится в очереди ошибок на проверку роботом.", conflictTeam, existing.getMatchId(), matchId));
                                recommendationRepository.save(existing);
                            }

                            // Emit anomaly event to MDM error queue
                            reportScheduleCollisionAnomaly(matchId, existing.getMatchId(), conflictTeam, sport != null ? sport.getSlug() : "sport", t1, t2, league, existing);

                            return PreFlightDecision.builder()
                                    .decisionType(DecisionType.STOP_AND_PERSIST)
                                    .stage("SCHEDULE_CONFLICT_PENDING_RESOLUTION")
                                    .recommendation(rec)
                                    .build();
                        }
                    }
                }
            }
        }

        // ── 3. Unresolved Player Entity Guard for Individual/Solo Sports (Zero-Garbage Protocol) ──
        boolean isTeam = (sport == null || sport.isTeamSport());
        if (!isTeam) {
            List<?> t1Hist = dossierMeta != null ? (List<?>) dossierMeta.get("team1_recent_matches") : null;
            List<?> t2Hist = dossierMeta != null ? (List<?>) dossierMeta.get("team2_recent_matches") : null;
            Long s1Id = (dossierMeta != null && dossierMeta.get("team1_sofascore_id") instanceof Number n1) ? n1.longValue() : null;
            Long s2Id = (dossierMeta != null && dossierMeta.get("team2_sofascore_id") instanceof Number n2) ? n2.longValue() : null;
            boolean p1Unresolved = (t1Hist == null || t1Hist.isEmpty()) && (s1Id == null || s1Id <= 0);
            boolean p2Unresolved = (t2Hist == null || t2Hist.isEmpty()) && (s2Id == null || s2Id <= 0);

            if (p1Unresolved || p2Unresolved) {
                String unknownName = p1Unresolved ? (t1 != null ? t1 : "Player 1") : (t2 != null ? t2 : "Player 2");
                log.warn("⚠️ [UNRESOLVED_PLAYER_ENTITY] Rejecting bet on match #{}: participant '{}' has 0 verified historical matches and no DDS ground truth. Forcing PASS.",
                        matchId, unknownName);
                if (anomalyReporter != null) {
                    anomalyReporter.reportUnmappedTeam(matchId, unknownName, league, sport != null ? sport.getCode() : "SPORT", sport != null ? sport.getSlug() : "sport");
                }

                rec.setConvictionTier("PASS");
                rec.setRecommendedMarket("PASS");
                rec.setRecommendedOdd(BigDecimal.ONE);
                rec.setBestBookmaker("NONE");
                rec.setExpectedValuePct(BigDecimal.ZERO);
                rec.setKellyStakePct(BigDecimal.ZERO);
                rec.setConfidenceScore(0);
                rec.setEvaluationStage("UNRESOLVED_PLAYER_PASS");
                
                rec.setErrorCategory("UNRESOLVED_PLAYER_ENTITY");
                rec.setLessonLearned("⚠️ Ошибка сущности игрока: недостаточная история матчей или неразрешенное имя. Задействован Zero-Garbage Gatekeeper.");
                rec.setCoreHypothesis(String.format("⚠️ ПРОПУСК МАТЧА: Участник '%s' не верифицирован в базе DDS (0 подтвержденных матчей). Защита от мусорных данных (Zero-Garbage Invariant).", unknownName));
                rec.setIdentifiedRisk("Отсутствие исторической телеметрии по игроку искажает байесовские априорные вероятности модели.");
                rec.setTacticalRationale("В соответствии с институциональным регламентом, ставки на юниорские/квалификационные матчи с неизвестными участниками строго блокируются.");

                return PreFlightDecision.builder()
                        .decisionType(DecisionType.STOP_AND_PERSIST)
                        .stage("UNRESOLVED_PLAYER_PASS")
                        .recommendation(rec)
                        .build();
            }
        }

        return PreFlightDecision.builder()
                .decisionType(DecisionType.PROCEED)
                .recommendation(rec)
                .build();
    }

    /**
     * Checks if a match dossier represents a "Data Void" (zero factual baseline).
     */
    public boolean isDataVoid(Map<String, Object> dossierMeta, Sport sport) {
        if (dossierMeta == null) {
            return true;
        }

        boolean isTeam = (sport == null || sport.isTeamSport());

        Object starters1 = dossierMeta.get("team1_starters");
        Object starters2 = dossierMeta.get("team2_starters");
        boolean hasStarters = isNonEmptyJsonArray(starters1) || isNonEmptyJsonArray(starters2);

        Object recent1 = dossierMeta.get("team1_recent_matches");
        Object recent2 = dossierMeta.get("team2_recent_matches");
        boolean hasRecent = isNonEmptyJsonArray(recent1) || isNonEmptyJsonArray(recent2);

        Object rating1 = dossierMeta.get("avg_rating_team1");
        Object rating2 = dossierMeta.get("avg_rating_team2");
        boolean hasRating = rating1 != null || rating2 != null;

        Object dna1 = dossierMeta.get("team1_dna_tags");
        Object dna2 = dossierMeta.get("team2_dna_tags");
        boolean hasDna = (dna1 != null && !dna1.toString().isBlank()) || (dna2 != null && !dna2.toString().isBlank());

        Object h2h = dossierMeta.get("h2h_json");
        boolean hasH2h = h2h != null && !h2h.toString().isBlank() && !"[]".equals(h2h.toString().trim());

        Object form1 = dossierMeta.get("team1_form");
        Object form2 = dossierMeta.get("team2_form");
        boolean hasForm = (form1 != null && !form1.toString().isBlank()) || (form2 != null && !form2.toString().isBlank());

        if (isTeam) {
            return !hasStarters && !hasRecent && !hasRating && !hasDna && !hasH2h && !hasForm;
        } else {
            return !hasRecent && !hasRating && !hasH2h && !hasForm;
        }
    }

    /**
     * Populates team names, slugs, leagues, URLs, and available bookmakers into the recommendation entity.
     */
    public void populateMetadata(MatchAiRecommendation rec, Map<String, Object> dossierMeta, Sport sport) {
        if (dossierMeta != null) {
            String t1 = (String) dossierMeta.get("team1_name");
            String t2 = (String) dossierMeta.get("team2_name");
            String league = (String) dossierMeta.get("league_name");
            String sportCode = sport != null ? sport.getCode() : (String) dossierMeta.get("sport_name");
            rec.setTeam1Name(t1);
            rec.setTeam2Name(t2);
            rec.setLeagueName(league);
            rec.setSportName(sportCode);
            rec.setSportSlug(SlugUtils.slugify(sportCode, "football"));
            rec.setTournamentSlug(SlugUtils.slugify(league, "league"));

            if (rec.getMatchId() != null) {
                String matchSlug = SlugUtils.buildMatchSlug(rec.getMatchId(), t1, t2);
                rec.setMatchSlug(matchSlug);
                rec.setMatchUrl(SlugUtils.buildMatchUrl(rec.getMatchId(), t1, t2, Language.RU));
                Set<String> bks = redisOddsConsensusService.getAvailableBookmakers(rec.getMatchId());
                if (bks != null && !bks.isEmpty()) {
                    rec.setAvailableBookmakers(String.join(",", bks));
                }
                Map<String, String> urls = redisOddsConsensusService.getBookmakerUrls(rec.getMatchId(), rec.getSportSlug());
                if (urls != null && !urls.isEmpty()) {
                    try {
                        rec.setBookmakerUrlsJson(objectMapper.writeValueAsString(urls));
                    } catch (Exception ignored) {}
                }
            }

            Long startTs = (Long) dossierMeta.get("start_time");
            if (startTs != null) {
                rec.setMatchStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTs), ZoneOffset.UTC));
            }
        }
    }

    private void reportScheduleCollisionAnomaly(
            Long matchId1, Long matchId2, String conflictTeam, String sportSlug,
            String t1, String t2, String league1, MatchAiRecommendation rec2
    ) {
        if (anomalyReporter != null) {
            anomalyReporter.reportScheduleCollision(
                    matchId1, matchId2, conflictTeam, sportSlug,
                    t1, t2, league1, rec2.getTeam1Name(), rec2.getTeam2Name(), rec2.getLeagueName()
            );
        }
    }

    private boolean isNonEmptyJsonArray(Object obj) {
        if (obj == null) return false;
        String str = obj.toString().trim();
        return !str.isEmpty() && !"[]".equals(str) && !"null".equalsIgnoreCase(str);
    }
}
