package pro.datawiki.igaming.mdm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.dto.live.MatchStatus;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.mdm.domain.MdmMatch;
import pro.datawiki.igaming.mdm.repository.MdmMatchRepository;

import java.time.Duration;
import java.util.*;

/**
 * High-throughput Live Match Consensus and Cache-Aside Service.
 *
 * Responsibilities:
 * 1. Monotonic Clock Consensus: elapsedSeconds = max(stored, incoming) so time never flows backward.
 * 2. In-Memory Redis Speed: All crawler ticks land in Redis, zero direct PostgreSQL hits per tick.
 * 3. Cache-Aside for Portal / Website: Return from Redis; if miss, load from mdm_match, cache and return.
 * 4. Scheduled 1-Minute Batch Flusher: Flush dirty matches exclusively via jdbcTemplate.batchUpdate into mdm_match.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MdmLiveScoreConsensusService {

    private static final String REDIS_MATCH_PREFIX = "live:match:";
    private static final String REDIS_DIRTY_MATCHES_SET = "live:dirty_mdm_matches";
    private static final Duration CACHE_TTL = Duration.ofHours(48);

    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final MdmMatchRepository matchRepository;
    private final ObjectMapper objectMapper;
    private final AnomalyReportingService anomalyReportingService;

    /**
     * Ingests a live score update from a crawler or provider.
     * Applies the Monotonic Clock Consensus rule:
     * - elapsedSeconds = max(storedSeconds, incomingSeconds)
     * - Status progresses to FINISHED if incoming is finished
     * - Terminal status never reverts to LIVE
     * - Marks matchId in Redis dirty set for scheduled 1-minute batch flush
     */
    public CanonicalLiveScoreDto ingestLiveScore(CanonicalLiveScoreDto incoming) {
        if (incoming == null || incoming.getMatchId() == null) {
            return null;
        }

        Long matchId = incoming.getMatchId();
        String redisKey = REDIS_MATCH_PREFIX + matchId;

        CanonicalLiveScoreDto merged = incoming;
        try {
            String existingJson = redisTemplate.opsForValue().get(redisKey);
            if (existingJson != null && !existingJson.isBlank()) {
                CanonicalLiveScoreDto existing = objectMapper.readValue(existingJson, CanonicalLiveScoreDto.class);

                // 1. Monotonic elapsed time: always choose the maximum elapsed seconds
                int existingSec = existing.getElapsedSeconds() != null ? existing.getElapsedSeconds() : 0;
                int incomingSec = incoming.getElapsedSeconds() != null ? incoming.getElapsedSeconds() : 0;
                int effectiveSec = Math.max(existingSec, incomingSec);
                incoming.setElapsedSeconds(effectiveSec);

                // 2. Terminal guard: Once FINISHED or CANCELLED, do not revert to LIVE
                if (existing.isFinished() && !incoming.isFinished()) {
                    incoming.setStatus(existing.getStatus());
                }

                // 3. Score fallback: if incoming score is null, retain existing
                if (incoming.getScore1() == null && existing.getScore1() != null) {
                    incoming.setScore1(existing.getScore1());
                }
                if (incoming.getScore2() == null && existing.getScore2() != null) {
                    incoming.setScore2(existing.getScore2());
                }
                if (incoming.getRegularScore1() == null && existing.getRegularScore1() != null) {
                    incoming.setRegularScore1(existing.getRegularScore1());
                }
                if (incoming.getRegularScore2() == null && existing.getRegularScore2() != null) {
                    incoming.setRegularScore2(existing.getRegularScore2());
                }
                if (incoming.getOvertimeScore1() == null && existing.getOvertimeScore1() != null) {
                    incoming.setOvertimeScore1(existing.getOvertimeScore1());
                }
                if (incoming.getOvertimeScore2() == null && existing.getOvertimeScore2() != null) {
                    incoming.setOvertimeScore2(existing.getOvertimeScore2());
                }
                if (incoming.getPenaltiesScore1() == null && existing.getPenaltiesScore1() != null) {
                    incoming.setPenaltiesScore1(existing.getPenaltiesScore1());
                }
                if (incoming.getPenaltiesScore2() == null && existing.getPenaltiesScore2() != null) {
                    incoming.setPenaltiesScore2(existing.getPenaltiesScore2());
                }
                if (incoming.getPeriods() == null || incoming.getPeriods().isEmpty()) {
                    incoming.setPeriods(existing.getPeriods());
                }
                if (incoming.getHasBroadcast() == null && existing.getHasBroadcast() != null) {
                    incoming.setHasBroadcast(existing.getHasBroadcast());
                    incoming.setBroadcastUrl(existing.getBroadcastUrl());
                }
                if (incoming.getCorners1() == null && existing.getCorners1() != null) {
                    incoming.setCorners1(existing.getCorners1());
                    incoming.setCorners2(existing.getCorners2());
                }
                if (incoming.getYellowCards1() == null && existing.getYellowCards1() != null) {
                    incoming.setYellowCards1(existing.getYellowCards1());
                    incoming.setYellowCards2(existing.getYellowCards2());
                }
                if (incoming.getRedCards1() == null && existing.getRedCards1() != null) {
                    incoming.setRedCards1(existing.getRedCards1());
                    incoming.setRedCards2(existing.getRedCards2());
                }
            }

            // 4. Danger Key handling (ephemeral Redis flag with 20s TTL)
            String dangerKey = "danger:match:" + matchId;
            if (Boolean.TRUE.equals(incoming.getIsSuspended())) {
                redisTemplate.opsForValue().set(dangerKey, "SUSPENDED_BY_" + incoming.getSourceCode(), Duration.ofSeconds(20));
                log.info("⚠️ [Danger-Signal] Market suspended for match #{} by source {}", matchId, incoming.getSourceCode());
            } else if (Boolean.FALSE.equals(incoming.getIsSuspended())) {
                redisTemplate.delete(dangerKey);
            }

            // 4. Basketball Metric Guard & Anomaly Reporting
            if (incoming.getPeriods() != null && !incoming.getPeriods().isEmpty()) {
                int sum1 = 0;
                int sum2 = 0;
                boolean hasSubstantialPoints = false;
                for (var p : incoming.getPeriods()) {
                    sum1 += p.getScore1();
                    sum2 += p.getScore2();
                    if (p.getScore1() > 5 || p.getScore2() > 5) {
                        hasSubstantialPoints = true;
                    }
                }
                if (hasSubstantialPoints && (incoming.getScore1() == null || (incoming.getScore1() + incoming.getScore2()) <= 10)) {
                    log.info("🏀 [Consensus Sanitizer] Converted quarter count {}:{} to true cumulative points {}:{} for match #{}",
                            incoming.getScore1(), incoming.getScore2(), sum1, sum2, matchId);
                    incoming.setScore1(sum1);
                    incoming.setScore2(sum2);
                }
            } else if (incoming.getScore1() != null && incoming.getScore2() != null && (incoming.getScore1() + incoming.getScore2()) <= 10) {
                // Quarter count without periods: emit anomaly to Kafka topic crawler.metric.anomalies
                reportIfScoreAnomaly(incoming);
            }

            if (incoming.getTimestamp() == null) {
                incoming.setTimestamp(System.currentTimeMillis());
            }

            // Write to Redis
            String updatedJson = objectMapper.writeValueAsString(incoming);
            redisTemplate.opsForValue().set(redisKey, updatedJson, CACHE_TTL);

            // Mark match as dirty for 1-minute batch flusher
            redisTemplate.opsForSet().add(REDIS_DIRTY_MATCHES_SET, String.valueOf(matchId));

            merged = incoming;
        } catch (Exception ex) {
            log.error("Failed ingesting live score for match #{}: {}", matchId, ex.getMessage(), ex);
        }

        return merged;
    }

    /**
     * Cache-Aside retrieval for website and clients:
     * 1. Try reading from Redis
     * 2. If MISS: load from PostgreSQL (mdm_match), put into Redis, and return
     */
    public CanonicalLiveScoreDto getLiveScore(Long matchId) {
        if (matchId == null) return null;
        String redisKey = REDIS_MATCH_PREFIX + matchId;

        try {
            // 1. Redis lookup
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json != null && !json.isBlank()) {
                return objectMapper.readValue(json, CanonicalLiveScoreDto.class);
            }

            // 2. Cache miss -> Load from PostgreSQL mdm_match
            Optional<MdmMatch> matchOpt = matchRepository.findById(matchId);
            if (matchOpt.isPresent()) {
                MdmMatch match = matchOpt.get();
                CanonicalLiveScoreDto dto = CanonicalLiveScoreDto.builder()
                        .matchId(match.getId())
                        .sourceCode(match.getSourceOrigin())
                        .status(parseMatchStatus(match.getStatus()))
                        .score1(parseInteger(match.getScore1()))
                        .score2(parseInteger(match.getScore2()))
                        .regularScore1(parseInteger(match.getRegularScore1()))
                        .regularScore2(parseInteger(match.getRegularScore2()))
                        .overtimeScore1(parseInteger(match.getOvertimeScore1()))
                        .overtimeScore2(parseInteger(match.getOvertimeScore2()))
                        .penaltiesScore1(parseInteger(match.getPenaltiesScore1()))
                        .penaltiesScore2(parseInteger(match.getPenaltiesScore2()))
                        .displayTimer("FINISHED".equalsIgnoreCase(match.getStatus()) ? "FT" : "")
                        .timestamp(System.currentTimeMillis())
                        .build();

                // Put in Redis
                String populatedJson = objectMapper.writeValueAsString(dto);
                redisTemplate.opsForValue().set(redisKey, populatedJson, CACHE_TTL);
                return dto;
            }
        } catch (Exception ex) {
            log.warn("Error fetching live score for match #{}: {}", matchId, ex.getMessage());
        }

        return null;
    }

    /**
     * Batch Cache-Aside retrieval for website and clients:
     * 1. Multi-get from Redis
     * 2. For any cache misses, load from PostgreSQL mdm_match in a single batch query,
     *    populate Redis, and merge into result.
     */
    public Map<Long, CanonicalLiveScoreDto> getLiveScores(java.util.Collection<Long> matchIds) {
        if (matchIds == null || matchIds.isEmpty()) return Collections.emptyMap();
        Map<Long, CanonicalLiveScoreDto> result = new HashMap<>();
        List<Long> idList = new ArrayList<>(new HashSet<>(matchIds));
        List<String> keys = idList.stream().map(id -> REDIS_MATCH_PREFIX + id).toList();

        Set<Long> missingIds = new HashSet<>();
        try {
            List<String> jsonList = redisTemplate.opsForValue().multiGet(keys);
            if (jsonList != null) {
                for (int i = 0; i < idList.size(); i++) {
                    Long matchId = idList.get(i);
                    String json = (i < jsonList.size()) ? jsonList.get(i) : null;
                    if (json != null && !json.isBlank()) {
                        try {
                            CanonicalLiveScoreDto dto = objectMapper.readValue(json, CanonicalLiveScoreDto.class);
                            result.put(matchId, dto);
                        } catch (Exception ignored) {
                            missingIds.add(matchId);
                        }
                    } else {
                        missingIds.add(matchId);
                    }
                }
            } else {
                missingIds.addAll(idList);
            }
        } catch (Exception ex) {
            log.warn("Error multi-getting live scores from Redis: {}", ex.getMessage());
            missingIds.addAll(idList);
        }

        // Cache miss batch fallback from PostgreSQL mdm_match
        if (!missingIds.isEmpty()) {
            try {
                List<MdmMatch> matches = matchRepository.findAllById(missingIds);
                Map<String, String> toCacheInRedis = new HashMap<>();
                for (MdmMatch match : matches) {
                    CanonicalLiveScoreDto dto = CanonicalLiveScoreDto.builder()
                            .matchId(match.getId())
                            .sourceCode(match.getSourceOrigin())
                            .status(parseMatchStatus(match.getStatus()))
                            .score1(parseInteger(match.getScore1()))
                            .score2(parseInteger(match.getScore2()))
                            .displayTimer("FINISHED".equalsIgnoreCase(match.getStatus()) ? "FT" : "")
                            .timestamp(System.currentTimeMillis())
                            .build();

                    result.put(match.getId(), dto);
                    try {
                        toCacheInRedis.put(REDIS_MATCH_PREFIX + match.getId(), objectMapper.writeValueAsString(dto));
                    } catch (Exception ignored) {}
                }

                if (!toCacheInRedis.isEmpty()) {
                    redisTemplate.opsForValue().multiSet(toCacheInRedis);
                }
            } catch (Exception ex) {
                log.warn("Error fetching fallback matches from DB for live scores: {}", ex.getMessage());
            }
        }

        return result;
    }

    /**
     * Scheduled 1-minute Batch Flusher into PostgreSQL mdm_match.
     * Executes ONLY via jdbcTemplate.batchUpdate (strictly bulk update).
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 15000)
    public void flushDirtyMatchesBatch() {
        try {
            Set<String> dirtyIds = redisTemplate.opsForSet().members(REDIS_DIRTY_MATCHES_SET);
            if (dirtyIds == null || dirtyIds.isEmpty()) {
                return;
            }

            List<String> idList = new ArrayList<>(dirtyIds);
            List<String> keys = idList.stream().map(id -> REDIS_MATCH_PREFIX + id).toList();

            List<String> jsons = redisTemplate.opsForValue().multiGet(keys);
            if (jsons == null || jsons.isEmpty()) {
                redisTemplate.opsForSet().remove(REDIS_DIRTY_MATCHES_SET, dirtyIds.toArray());
                return;
            }

            List<Object[]> batchParams = new ArrayList<>();
            List<String> processedIds = new ArrayList<>();

            for (int i = 0; i < jsons.size(); i++) {
                String json = jsons.get(i);
                if (json == null || json.isBlank()) continue;

                try {
                    CanonicalLiveScoreDto dto = objectMapper.readValue(json, CanonicalLiveScoreDto.class);
                    if (dto.getMatchId() == null) continue;

                    String s1 = dto.getScore1() != null ? String.valueOf(dto.getScore1()) : null;
                    String s2 = dto.getScore2() != null ? String.valueOf(dto.getScore2()) : null;
                    String reg1 = dto.getRegularScore1() != null ? String.valueOf(dto.getRegularScore1()) : null;
                    String reg2 = dto.getRegularScore2() != null ? String.valueOf(dto.getRegularScore2()) : null;
                    String ot1 = dto.getOvertimeScore1() != null ? String.valueOf(dto.getOvertimeScore1()) : null;
                    String ot2 = dto.getOvertimeScore2() != null ? String.valueOf(dto.getOvertimeScore2()) : null;
                    String pen1 = dto.getPenaltiesScore1() != null ? String.valueOf(dto.getPenaltiesScore1()) : null;
                    String pen2 = dto.getPenaltiesScore2() != null ? String.valueOf(dto.getPenaltiesScore2()) : null;
                    String status = dto.getStatus() != null ? dto.getStatus().name() : "LIVE";

                    batchParams.add(new Object[] { s1, s2, reg1, reg2, ot1, ot2, pen1, pen2, status, dto.getMatchId() });
                    processedIds.add(idList.get(i));
                } catch (Exception ex) {
                    log.warn("Failed deserializing live score JSON during batch flush: {}", ex.getMessage());
                }
            }

            if (!batchParams.isEmpty()) {
                String sql = "UPDATE mdm_match SET score1 = ?, score2 = ?, regular_score1 = COALESCE(?, regular_score1), regular_score2 = COALESCE(?, regular_score2), overtime_score1 = COALESCE(?, overtime_score1), overtime_score2 = COALESCE(?, overtime_score2), penalties_score1 = COALESCE(?, penalties_score1), penalties_score2 = COALESCE(?, penalties_score2), status = ?, updated_at = NOW() WHERE id = ?";
                int[] results = jdbcTemplate.batchUpdate(sql, batchParams);
                log.info("Batch flushed {} live match updates into mdm_match (batch size: {})", results.length, batchParams.size());
            }

            if (!processedIds.isEmpty()) {
                redisTemplate.opsForSet().remove(REDIS_DIRTY_MATCHES_SET, processedIds.toArray());
            }
        } catch (Exception ex) {
            log.error("Error executing scheduled batch flush to mdm_match: {}", ex.getMessage(), ex);
        }
    }

    private MatchStatus parseMatchStatus(String status) {
        if (status == null) return MatchStatus.SCHEDULED;
        try {
            return MatchStatus.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException ignored) {
            if ("IN_PLAY".equalsIgnoreCase(status)) return MatchStatus.LIVE;
            if ("ENDED".equalsIgnoreCase(status)) return MatchStatus.FINISHED;
            return MatchStatus.SCHEDULED;
        }
    }

    private Integer parseInteger(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void reportIfScoreAnomaly(CanonicalLiveScoreDto incoming) {
        if (incoming == null || incoming.getMatchId() == null || anomalyReportingService == null) return;
        if (incoming.getScore1() == null || incoming.getScore2() == null) return;
        try {
            Long matchId = incoming.getMatchId();
            String sql = "SELECT m.id, s.slug as sport_slug, ht.canonical_name as home_name, at.canonical_name as away_name " +
                         "FROM mdm_match m JOIN mdm_sport s ON m.sport_id = s.id " +
                         "LEFT JOIN mdm_team ht ON m.home_team_id = ht.id " +
                         "LEFT JOIN mdm_team at ON m.away_team_id = at.id " +
                         "WHERE m.id = ?";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, matchId);
            if (!rows.isEmpty()) {
                String sportSlug = (String) rows.get(0).get("sport_slug");
                Sport sport = Sports.get(sportSlug);
                if (sport != null && !sport.isValidScore(incoming.getScore1(), incoming.getScore2())) {
                    String src = incoming.getSourceCode() != null ? incoming.getSourceCode() : "unknown";
                    String home = (String) rows.get(0).get("home_name");
                    String away = (String) rows.get(0).get("away_name");
                    String matchTitle = (home != null ? home : "Team 1") + " vs " + (away != null ? away : "Team 2");
                    String scoreStr = incoming.getScore1() + ":" + incoming.getScore2();
                    String details = String.format("Источник '%s' по спорту '%s' прислал невалидный счет %s (возможен счет четвертей/периодов вместо кумулятивных очков матча). Нужно поправить парсер источника!",
                            src, sport.getDisplayName("ru"), scoreStr);
                    anomalyReportingService.reportCrawlerMetricAnomaly(
                            src,
                            sport.getSlug(),
                            matchId,
                            matchTitle,
                            scoreStr,
                            "SCORE_RANGE_ANOMALY",
                            details,
                            null
                    );
                }
            }
        } catch (Exception ex) {
            log.debug("Error checking metric anomaly for match #{}: {}", incoming.getMatchId(), ex.getMessage());
        }
    }

    /**
     * Stale Live Match Sweeper.
     * Bookmaker feeds drop matches upon event conclusion without emitting explicit FINISHED status.
     * Automatically transitions any match in LIVE / IN_PLAY with no heartbeat for >= 20 minutes to FINISHED.
     * Also updates Redis cached status if present.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void reapStaleLiveMatches() {
        try {
            List<Long> staleIds = matchRepository.findStaleLiveMatchIds(200);
            if (staleIds.isEmpty()) {
                return;
            }

            int reaped = matchRepository.updateStatusByIdIn("FINISHED", staleIds);

            // Clean or mark FINISHED in Redis
            for (Long matchId : staleIds) {
                try {
                    String redisKey = REDIS_MATCH_PREFIX + matchId;
                    String json = redisTemplate.opsForValue().get(redisKey);
                    if (json != null && !json.isBlank()) {
                        CanonicalLiveScoreDto dto = objectMapper.readValue(json, CanonicalLiveScoreDto.class);
                        dto.setStatus(MatchStatus.FINISHED);
                        dto.setDisplayTimer("FT");
                        redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(dto), CACHE_TTL);
                    }
                } catch (Exception ignored) {
                }
            }

            log.info("🧹 [Live Sweeper] Reaped {} stale live matches with no heartbeat for >20m -> FINISHED", reaped);
        } catch (Exception ex) {
            log.error("Error executing stale live match sweeper: {}", ex.getMessage(), ex);
        }
    }

}
