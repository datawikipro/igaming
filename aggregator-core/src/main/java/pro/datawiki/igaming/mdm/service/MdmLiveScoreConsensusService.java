/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.data.redis.core.StringRedisTemplate
 *  org.springframework.jdbc.core.JdbcTemplate
 *  org.springframework.scheduling.annotation.Scheduled
 *  org.springframework.stereotype.Service
 *  pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto
 *  pro.datawiki.igaming.dto.live.MatchStatus
 *  pro.datawiki.igaming.dto.live.PeriodScoreDto
 *  pro.datawiki.igaming.dto.sport.Sport
 *  pro.datawiki.igaming.dto.sport.Sports
 */
package pro.datawiki.igaming.mdm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.invoke.CallSite;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.dto.live.MatchStatus;
import pro.datawiki.igaming.dto.live.PeriodScoreDto;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.mdm.domain.MdmMatch;
import pro.datawiki.igaming.mdm.repository.MdmMatchRepository;
import pro.datawiki.igaming.mdm.service.AnomalyReportingService;

@Service
public class MdmLiveScoreConsensusService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(MdmLiveScoreConsensusService.class);
    private static final String REDIS_MATCH_PREFIX = "live:match:";
    private static final String REDIS_DIRTY_MATCHES_SET = "live:dirty_mdm_matches";
    private static final Duration CACHE_TTL = Duration.ofHours(48L);
    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final MdmMatchRepository matchRepository;
    private final ObjectMapper objectMapper;
    private final AnomalyReportingService anomalyReportingService;

    public CanonicalLiveScoreDto ingestLiveScore(CanonicalLiveScoreDto incoming) {
        if (incoming == null || incoming.getMatchId() == null) {
            return null;
        }
        Long matchId = incoming.getMatchId();
        String redisKey = REDIS_MATCH_PREFIX + matchId;
        CanonicalLiveScoreDto merged = incoming;
        try {
            String existingJson = (String)this.redisTemplate.opsForValue().get((Object)redisKey);
            if (existingJson != null && !existingJson.isBlank()) {
                CanonicalLiveScoreDto existing = (CanonicalLiveScoreDto)this.objectMapper.readValue(existingJson, CanonicalLiveScoreDto.class);
                int existingSec = existing.getElapsedSeconds() != null ? existing.getElapsedSeconds() : 0;
                int incomingSec = incoming.getElapsedSeconds() != null ? incoming.getElapsedSeconds() : 0;
                int effectiveSec = Math.max(existingSec, incomingSec);
                incoming.setElapsedSeconds(Integer.valueOf(effectiveSec));
                if (existing.isFinished() && !incoming.isFinished()) {
                    incoming.setStatus(existing.getStatus());
                }
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
            String dangerKey = "danger:match:" + matchId;
            if (Boolean.TRUE.equals(incoming.getIsSuspended())) {
                this.redisTemplate.opsForValue().set((Object)dangerKey, (Object)("SUSPENDED_BY_" + incoming.getSourceCode()), Duration.ofSeconds(20L));
                log.info("\u26a0\ufe0f [Danger-Signal] Market suspended for match #{} by source {}", (Object)matchId, (Object)incoming.getSourceCode());
            } else if (Boolean.FALSE.equals(incoming.getIsSuspended())) {
                this.redisTemplate.delete((Object)dangerKey);
            }
            if (incoming.getPeriods() != null && !incoming.getPeriods().isEmpty()) {
                int sum1 = 0;
                int sum2 = 0;
                boolean hasSubstantialPoints = false;
                for (PeriodScoreDto p : incoming.getPeriods()) {
                    sum1 += p.getScore1();
                    sum2 += p.getScore2();
                    if (p.getScore1() <= 5 && p.getScore2() <= 5) continue;
                    hasSubstantialPoints = true;
                }
                if (hasSubstantialPoints && (incoming.getScore1() == null || incoming.getScore1() + incoming.getScore2() <= 10)) {
                    log.info("\ud83c\udfc0 [Consensus Sanitizer] Converted quarter count {}:{} to true cumulative points {}:{} for match #{}", new Object[]{incoming.getScore1(), incoming.getScore2(), sum1, sum2, matchId});
                    incoming.setScore1(Integer.valueOf(sum1));
                    incoming.setScore2(Integer.valueOf(sum2));
                }
            } else if (incoming.getScore1() != null && incoming.getScore2() != null && incoming.getScore1() + incoming.getScore2() <= 10) {
                this.reportIfScoreAnomaly(incoming);
            }
            if (incoming.getTimestamp() == null) {
                incoming.setTimestamp(Long.valueOf(System.currentTimeMillis()));
            }
            String updatedJson = this.objectMapper.writeValueAsString((Object)incoming);
            this.redisTemplate.opsForValue().set((Object)redisKey, (Object)updatedJson, CACHE_TTL);
            this.redisTemplate.opsForSet().add((Object)REDIS_DIRTY_MATCHES_SET, (Object[])new String[]{String.valueOf(matchId)});
            merged = incoming;
        }
        catch (Exception ex) {
            log.error("Failed ingesting live score for match #{}: {}", new Object[]{matchId, ex.getMessage(), ex});
        }
        return merged;
    }

    public CanonicalLiveScoreDto getLiveScore(Long matchId) {
        if (matchId == null) {
            return null;
        }
        String redisKey = REDIS_MATCH_PREFIX + matchId;
        try {
            String json = (String)this.redisTemplate.opsForValue().get((Object)redisKey);
            if (json != null && !json.isBlank()) {
                return (CanonicalLiveScoreDto)this.objectMapper.readValue(json, CanonicalLiveScoreDto.class);
            }
            Optional matchOpt = this.matchRepository.findById(matchId);
            if (matchOpt.isPresent()) {
                MdmMatch match = (MdmMatch)matchOpt.get();
                CanonicalLiveScoreDto dto = CanonicalLiveScoreDto.builder().matchId(match.getId()).sourceCode(match.getSourceOrigin()).status(this.parseMatchStatus(match.getStatus())).score1(this.parseInteger(match.getScore1())).score2(this.parseInteger(match.getScore2())).regularScore1(this.parseInteger(match.getRegularScore1())).regularScore2(this.parseInteger(match.getRegularScore2())).overtimeScore1(this.parseInteger(match.getOvertimeScore1())).overtimeScore2(this.parseInteger(match.getOvertimeScore2())).penaltiesScore1(this.parseInteger(match.getPenaltiesScore1())).penaltiesScore2(this.parseInteger(match.getPenaltiesScore2())).displayTimer("FINISHED".equalsIgnoreCase(match.getStatus()) ? "FT" : "").timestamp(Long.valueOf(System.currentTimeMillis())).build();
                String populatedJson = this.objectMapper.writeValueAsString((Object)dto);
                this.redisTemplate.opsForValue().set((Object)redisKey, (Object)populatedJson, CACHE_TTL);
                return dto;
            }
        }
        catch (Exception ex) {
            log.warn("Error fetching live score for match #{}: {}", (Object)matchId, (Object)ex.getMessage());
        }
        return null;
    }

    public Map<Long, CanonicalLiveScoreDto> getLiveScores(Collection<Long> matchIds) {
        CanonicalLiveScoreDto dto;
        if (matchIds == null || matchIds.isEmpty()) {
            return Collections.emptyMap();
        }
        HashMap<Long, CanonicalLiveScoreDto> result = new HashMap<Long, CanonicalLiveScoreDto>();
        ArrayList<Long> idList = new ArrayList<Long>(new HashSet<Long>(matchIds));
        List<String> keys = idList.stream().map(id -> REDIS_MATCH_PREFIX + id).toList();
        HashSet<Long> missingIds = new HashSet<Long>();
        try {
            List jsonList = this.redisTemplate.opsForValue().multiGet(keys);
            if (jsonList != null) {
                for (int i = 0; i < idList.size(); ++i) {
                    String json;
                    Long matchId = (Long)idList.get(i);
                    String string = json = i < jsonList.size() ? (String)jsonList.get(i) : null;
                    if (json != null && !json.isBlank()) {
                        try {
                            dto = (CanonicalLiveScoreDto)this.objectMapper.readValue(json, CanonicalLiveScoreDto.class);
                            result.put(matchId, dto);
                        }
                        catch (Exception ignored) {
                            missingIds.add(matchId);
                        }
                        continue;
                    }
                    missingIds.add(matchId);
                }
            } else {
                missingIds.addAll(idList);
            }
        }
        catch (Exception ex) {
            log.warn("Error multi-getting live scores from Redis: {}", (Object)ex.getMessage());
            missingIds.addAll(idList);
        }
        if (!missingIds.isEmpty()) {
            try {
                List matches = this.matchRepository.findAllById(missingIds);
                HashMap<CallSite, String> toCacheInRedis = new HashMap<CallSite, String>();
                for (MdmMatch match : matches) {
                    dto = CanonicalLiveScoreDto.builder().matchId(match.getId()).sourceCode(match.getSourceOrigin()).status(this.parseMatchStatus(match.getStatus())).score1(this.parseInteger(match.getScore1())).score2(this.parseInteger(match.getScore2())).displayTimer("FINISHED".equalsIgnoreCase(match.getStatus()) ? "FT" : "").timestamp(Long.valueOf(System.currentTimeMillis())).build();
                    result.put(match.getId(), dto);
                    try {
                        toCacheInRedis.put((CallSite)((Object)(REDIS_MATCH_PREFIX + match.getId())), this.objectMapper.writeValueAsString((Object)dto));
                    }
                    catch (Exception exception) {}
                }
                if (!toCacheInRedis.isEmpty()) {
                    this.redisTemplate.opsForValue().multiSet(toCacheInRedis);
                }
            }
            catch (Exception ex) {
                log.warn("Error fetching fallback matches from DB for live scores: {}", (Object)ex.getMessage());
            }
        }
        return result;
    }

    @Scheduled(fixedDelay=60000L, initialDelay=15000L)
    public void flushDirtyMatchesBatch() {
        try {
            Set dirtyIds = this.redisTemplate.opsForSet().members((Object)REDIS_DIRTY_MATCHES_SET);
            if (dirtyIds == null || dirtyIds.isEmpty()) {
                return;
            }
            ArrayList idList = new ArrayList(dirtyIds);
            List<String> keys = idList.stream().map(id -> REDIS_MATCH_PREFIX + id).toList();
            List jsons = this.redisTemplate.opsForValue().multiGet(keys);
            if (jsons == null || jsons.isEmpty()) {
                this.redisTemplate.opsForSet().remove((Object)REDIS_DIRTY_MATCHES_SET, dirtyIds.toArray());
                return;
            }
            ArrayList<Object[]> batchParams = new ArrayList<Object[]>();
            ArrayList<String> processedIds = new ArrayList<String>();
            for (int i = 0; i < jsons.size(); ++i) {
                String json = (String)jsons.get(i);
                if (json == null || json.isBlank()) continue;
                try {
                    CanonicalLiveScoreDto dto = (CanonicalLiveScoreDto)this.objectMapper.readValue(json, CanonicalLiveScoreDto.class);
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
                    batchParams.add(new Object[]{s1, s2, reg1, reg2, ot1, ot2, pen1, pen2, status, dto.getMatchId()});
                    processedIds.add((String)idList.get(i));
                    continue;
                }
                catch (Exception ex) {
                    log.warn("Failed deserializing live score JSON during batch flush: {}", (Object)ex.getMessage());
                }
            }
            if (!batchParams.isEmpty()) {
                String sql = "UPDATE mdm_match SET score1 = ?, score2 = ?, regular_score1 = COALESCE(?, regular_score1), regular_score2 = COALESCE(?, regular_score2), overtime_score1 = COALESCE(?, overtime_score1), overtime_score2 = COALESCE(?, overtime_score2), penalties_score1 = COALESCE(?, penalties_score1), penalties_score2 = COALESCE(?, penalties_score2), status = ?, updated_at = NOW() WHERE id = ?";
                int[] results = this.jdbcTemplate.batchUpdate(sql, batchParams);
                log.info("Batch flushed {} live match updates into mdm_match (batch size: {})", (Object)results.length, (Object)batchParams.size());
            }
            if (!processedIds.isEmpty()) {
                this.redisTemplate.opsForSet().remove((Object)REDIS_DIRTY_MATCHES_SET, processedIds.toArray());
            }
        }
        catch (Exception ex) {
            log.error("Error executing scheduled batch flush to mdm_match: {}", (Object)ex.getMessage(), (Object)ex);
        }
    }

    private MatchStatus parseMatchStatus(String status) {
        if (status == null) {
            return MatchStatus.SCHEDULED;
        }
        try {
            return MatchStatus.valueOf((String)status.toUpperCase().trim());
        }
        catch (IllegalArgumentException ignored) {
            if ("IN_PLAY".equalsIgnoreCase(status)) {
                return MatchStatus.LIVE;
            }
            if ("ENDED".equalsIgnoreCase(status)) {
                return MatchStatus.FINISHED;
            }
            return MatchStatus.SCHEDULED;
        }
    }

    private Integer parseInteger(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(str.trim());
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void reportIfScoreAnomaly(CanonicalLiveScoreDto incoming) {
        if (incoming == null || incoming.getMatchId() == null || this.anomalyReportingService == null) {
            return;
        }
        if (incoming.getScore1() == null || incoming.getScore2() == null) {
            return;
        }
        try {
            String sportSlug;
            Sport sport;
            Long matchId = incoming.getMatchId();
            String sql = "SELECT m.id, s.slug as sport_slug, ht.canonical_name as home_name, at.canonical_name as away_name FROM mdm_match m JOIN mdm_sport s ON m.sport_id = s.id LEFT JOIN mdm_team ht ON m.home_team_id = ht.id LEFT JOIN mdm_team at ON m.away_team_id = at.id WHERE m.id = ?";
            List rows = this.jdbcTemplate.queryForList(sql, new Object[]{matchId});
            if (!rows.isEmpty() && (sport = Sports.get((String)(sportSlug = (String)((Map)rows.get(0)).get("sport_slug")))) != null && !sport.isValidScore(incoming.getScore1().intValue(), incoming.getScore2().intValue())) {
                String src = incoming.getSourceCode() != null ? incoming.getSourceCode() : "unknown";
                String home = (String)((Map)rows.get(0)).get("home_name");
                String away = (String)((Map)rows.get(0)).get("away_name");
                String matchTitle = (home != null ? home : "Team 1") + " vs " + (away != null ? away : "Team 2");
                String scoreStr = incoming.getScore1() + ":" + incoming.getScore2();
                String details = String.format("\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a '%s' \u043f\u043e \u0441\u043f\u043e\u0440\u0442\u0443 '%s' \u043f\u0440\u0438\u0441\u043b\u0430\u043b \u043d\u0435\u0432\u0430\u043b\u0438\u0434\u043d\u044b\u0439 \u0441\u0447\u0435\u0442 %s (\u0432\u043e\u0437\u043c\u043e\u0436\u0435\u043d \u0441\u0447\u0435\u0442 \u0447\u0435\u0442\u0432\u0435\u0440\u0442\u0435\u0439/\u043f\u0435\u0440\u0438\u043e\u0434\u043e\u0432 \u0432\u043c\u0435\u0441\u0442\u043e \u043a\u0443\u043c\u0443\u043b\u044f\u0442\u0438\u0432\u043d\u044b\u0445 \u043e\u0447\u043a\u043e\u0432 \u043c\u0430\u0442\u0447\u0430). \u041d\u0443\u0436\u043d\u043e \u043f\u043e\u043f\u0440\u0430\u0432\u0438\u0442\u044c \u043f\u0430\u0440\u0441\u0435\u0440 \u0438\u0441\u0442\u043e\u0447\u043d\u0438\u043a\u0430!", src, sport.getDisplayName("ru"), scoreStr);
                this.anomalyReportingService.reportCrawlerMetricAnomaly(src, sport.getSlug(), matchId, matchTitle, scoreStr, "SCORE_RANGE_ANOMALY", details, null);
            }
        }
        catch (Exception ex) {
            log.debug("Error checking metric anomaly for match #{}: {}", (Object)incoming.getMatchId(), (Object)ex.getMessage());
        }
    }

    @Generated
    public MdmLiveScoreConsensusService(StringRedisTemplate redisTemplate, JdbcTemplate jdbcTemplate, MdmMatchRepository matchRepository, ObjectMapper objectMapper, AnomalyReportingService anomalyReportingService) {
        this.redisTemplate = redisTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.matchRepository = matchRepository;
        this.objectMapper = objectMapper;
        this.anomalyReportingService = anomalyReportingService;
    }
}

