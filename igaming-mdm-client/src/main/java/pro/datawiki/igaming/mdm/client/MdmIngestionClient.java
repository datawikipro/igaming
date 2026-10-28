package pro.datawiki.igaming.mdm.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.dto.CanonicalMatchEventDto;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.MatchResolutionRequest;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.CanonicalTeamDto;
import pro.datawiki.igaming.dto.CanonicalTournamentDto;
import pro.datawiki.igaming.dto.TeamResolutionRequest;
import pro.datawiki.igaming.dto.TournamentResolutionRequest;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * High-performance In-Memory client for bookmaker crawlers.
 * Caches the MDM Ingestion Blueprint in JVM memory with Caffeine (O(1) lookups).
 * Enforces the Stage 1 Strict SofaScore Mandate by dispatching unmapped entities
 * to the error collector service without creating synthetic matches.
 */
@Component
@Slf4j
public class MdmIngestionClient {

    private final String externalSystemCode;
    private final String mdmServiceUrl;
    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String anomalyTopic;

    private final Cache<String, IngestionBlueprint> blueprintCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(50)
            .build();

    public MdmIngestionClient(
            @Value("${app.bookmaker.code:${app.bookmaker.name:fon-bet-ru}}") String externalSystemCode,
            @Value("${app.mdm.service.url:http://igaming-mdm-service:8095}") String mdmServiceUrl,
            @org.springframework.beans.factory.annotation.Qualifier("genericKafkaTemplate")
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.anomaly-errors:mdm.anomaly.errors}") String anomalyTopic
    ) {
        this.externalSystemCode = externalSystemCode;
        this.mdmServiceUrl = mdmServiceUrl;
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        this.restTemplate = new RestTemplate(factory);
        this.kafkaTemplate = kafkaTemplate;
        this.anomalyTopic = anomalyTopic;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("🚀 [MDM-CLIENT] Initialized MdmIngestionClient for systemCode={}, mdmServiceUrl={}, kafkaTemplatePresent={}",
                externalSystemCode, mdmServiceUrl, kafkaTemplate != null);
    }

    /**
     * Resolves and folds a raw OddsUpdateRequest into a 100% typed CanonicalMatchEventDto.
     */
    public Optional<CanonicalMatchEventDto> foldMatchEvent(OddsUpdateRequest request) {
        if (request == null) return Optional.empty();
        String systemCode = request.getBookmaker() != null && !request.getBookmaker().isBlank()
                ? request.getBookmaker().trim().toLowerCase()
                : this.externalSystemCode;
        return foldMatchEvent(systemCode, request);
    }

    /**
     * Resolves and folds a raw OddsUpdateRequest for a specific external system code.
     */
    public Optional<CanonicalMatchEventDto> foldMatchEvent(String systemCode, OddsUpdateRequest request) {
        if (request == null) return Optional.empty();
        String sportKey = request.getSportName() != null ? request.getSportName()
                : (request.getSportType() != null ? request.getSportType().name() : null);
        return foldMatchEvent(
                systemCode,
                request.getExternalEventId(),
                sportKey,
                request.getRawTournamentKey() != null ? request.getRawTournamentKey() : request.getLeagueName(),
                request.getLeagueName(),
                request.getRawTeam1Key() != null ? request.getRawTeam1Key() : request.getTeam1(),
                request.getTeam1(),
                request.getRawTeam2Key() != null ? request.getRawTeam2Key() : request.getTeam2(),
                request.getTeam2(),
                request.getStartTime(),
                request.getIsLive(),
                request.getEventUrl(),
                request.getScore1(),
                request.getScore2(),
                request.getTimer(),
                request.getScoreComment(),
                request.getOdds(),
                request.getCountry() != null ? request.getCountry() : request.getCategoryName(),
                request.getRawCategoryKey()
        );
    }

    /**
     * Resolves and folds a raw bookmaker match into a 100% typed CanonicalMatchEventDto.
     * Returns Optional.empty() and reports to error collector if any entity is unmapped.
     */
    public Optional<CanonicalMatchEventDto> foldMatchEvent(
            String externalEventId,
            String rawSportKey,
            String rawTournamentKey,
            String rawTournamentName,
            String rawHomeTeamKey,
            String rawHomeTeamName,
            String rawAwayTeamKey,
            String rawAwayTeamName,
            Long startTime,
            Boolean isLive,
            String eventUrl,
            String score1,
            String score2,
            String timer,
            String scoreComment,
            List<OddItem> odds
    ) {
        return foldMatchEvent(
                this.externalSystemCode,
                externalEventId,
                rawSportKey,
                rawTournamentKey,
                rawTournamentName,
                rawHomeTeamKey,
                rawHomeTeamName,
                rawAwayTeamKey,
                rawAwayTeamName,
                startTime,
                isLive,
                eventUrl,
                score1,
                score2,
                timer,
                scoreComment,
                odds,
                null,
                null
        );
    }

    /**
     * Resolves and folds a raw bookmaker match for a specific external system code.
     */
    public Optional<CanonicalMatchEventDto> foldMatchEvent(
            String systemCode,
            String externalEventId,
            String rawSportKey,
            String rawTournamentKey,
            String rawTournamentName,
            String rawHomeTeamKey,
            String rawHomeTeamName,
            String rawAwayTeamKey,
            String rawAwayTeamName,
            Long startTime,
            Boolean isLive,
            String eventUrl,
            String score1,
            String score2,
            String timer,
            String scoreComment,
            List<OddItem> odds
    ) {
        return foldMatchEvent(
                systemCode,
                externalEventId,
                rawSportKey,
                rawTournamentKey,
                rawTournamentName,
                rawHomeTeamKey,
                rawHomeTeamName,
                rawAwayTeamKey,
                rawAwayTeamName,
                startTime,
                isLive,
                eventUrl,
                score1,
                score2,
                timer,
                scoreComment,
                odds,
                null,
                null
        );
    }

    /**
     * Resolves and folds a raw bookmaker match for a specific external system code with country/category scoping.
     */
    public Optional<CanonicalMatchEventDto> foldMatchEvent(
            String systemCode,
            String externalEventId,
            String rawSportKey,
            String rawTournamentKey,
            String rawTournamentName,
            String rawHomeTeamKey,
            String rawHomeTeamName,
            String rawAwayTeamKey,
            String rawAwayTeamName,
            Long startTime,
            Boolean isLive,
            String eventUrl,
            String score1,
            String score2,
            String timer,
            String scoreComment,
            List<OddItem> odds,
            String rawCountryOrCategoryName,
            String rawCategoryKey
    ) {
        String activeSystemCode = (systemCode != null && !systemCode.isBlank()) ? systemCode.trim().toLowerCase() : this.externalSystemCode;
        IngestionBlueprint blueprint = getBlueprint(activeSystemCode);

        // 1. Resolve Sport
        CanonicalSport sport = null;
        if (rawSportKey != null) {
            sport = blueprint.sports.get(rawSportKey);
            if (sport == null) {
                sport = blueprint.sportsBySlug.get(rawSportKey.toLowerCase());
            }
        }
        if (sport == null) {
            reportAnomaly(activeSystemCode, "SPORT", rawSportKey, null, null, "Unmapped sport at " + activeSystemCode);
            return Optional.empty();
        }

        // 2. Resolve Country via bookmaker's own decodes in blueprint
        CanonicalCountry country = null;
        if (rawCategoryKey != null && !rawCategoryKey.isBlank()) {
            country = blueprint.countries.get(rawCategoryKey.trim());
            if (country == null) {
                country = blueprint.countries.get(rawCategoryKey.trim().toLowerCase());
            }
        }
        if (country == null && rawCountryOrCategoryName != null && !rawCountryOrCategoryName.isBlank()) {
            country = blueprint.countries.get(rawCountryOrCategoryName.trim());
            if (country == null) {
                country = blueprint.countries.get(rawCountryOrCategoryName.trim().toLowerCase());
            }
            if (country == null) {
                country = blueprint.countriesByName.get(rawCountryOrCategoryName.trim().toLowerCase());
            }
        }
        Integer countryId = country != null ? country.countryId : null;
        String countryName = country != null ? country.canonicalName : rawCountryOrCategoryName;

        // 3. Resolve Tournament (scoped by country if available)
        CanonicalTournament tournament = null;
        if (rawTournamentKey != null && !"null".equalsIgnoreCase(rawTournamentKey.trim())) {
            tournament = blueprint.tournaments.get(rawTournamentKey);
            if (tournament == null) {
                tournament = blueprint.tournaments.get(rawTournamentKey.trim().toLowerCase());
            }
        }
        if (tournament == null && countryId != null && rawTournamentName != null && !"null".equalsIgnoreCase(rawTournamentName.trim())) {
            tournament = blueprint.tournamentsByCountryAndName.get(countryId + ":" + rawTournamentName.trim().toLowerCase());
        }
        if (tournament == null && rawTournamentName != null && !"null".equalsIgnoreCase(rawTournamentName.trim())) {
            tournament = blueprint.tournamentsByName.get(rawTournamentName.trim().toLowerCase());
        }
        if (tournament != null && tournament.sportId != null && !tournament.sportId.equals(sport.sportId)) {
            tournament = null;
        }

        // 4. Resolve Home Team
        CanonicalTeam homeTeam = null;
        if (rawHomeTeamKey != null) {
            homeTeam = blueprint.teams.get(rawHomeTeamKey);
            if (homeTeam == null) {
                homeTeam = blueprint.teams.get(rawHomeTeamKey.trim().toLowerCase());
            }
        }
        if (homeTeam == null && rawHomeTeamName != null) {
            homeTeam = blueprint.teamsByName.get(rawHomeTeamName.trim().toLowerCase());
        }
        if (homeTeam != null && homeTeam.sportId != null && !homeTeam.sportId.equals(sport.sportId)) {
            homeTeam = null;
        }

        // 5. Resolve Away Team
        CanonicalTeam awayTeam = null;
        if (rawAwayTeamKey != null) {
            awayTeam = blueprint.teams.get(rawAwayTeamKey);
            if (awayTeam == null) {
                awayTeam = blueprint.teams.get(rawAwayTeamKey.trim().toLowerCase());
            }
        }
        if (awayTeam == null && rawAwayTeamName != null) {
            awayTeam = blueprint.teamsByName.get(rawAwayTeamName.trim().toLowerCase());
        }
        if (awayTeam != null && awayTeam.sportId != null && !awayTeam.sportId.equals(sport.sportId)) {
            awayTeam = null;
        }

        // 6. If ANY entity is unmapped in local blueprint, or matchId is missing:
        Long matchId = null;
        if (tournament != null && homeTeam != null && awayTeam != null) {
            matchId = resolveMatchId(activeSystemCode, sport.sportId, tournament.tournamentId, homeTeam.teamId, awayTeam.teamId, startTime).orElse(null);
        }

        if (matchId == null || tournament == null || homeTeam == null || awayTeam == null) {
            // Fast REST fallback to resolve or auto-create in MDM
            try {
                long targetSec = startTime != null ? (startTime > 100_000_000_000L ? startTime / 1000 : startTime) : (System.currentTimeMillis() / 1000);
                String resolvedTournName = rawTournamentName != null && !rawTournamentName.isBlank() ? rawTournamentName : rawTournamentKey;
                String categoryNameParsed = null;
                if (resolvedTournName != null) {
                    for (String sep : new String[]{" - ", ". ", " / "}) {
                        int sepIdx = resolvedTournName.indexOf(sep);
                        if (sepIdx > 0 && sepIdx < resolvedTournName.length() - sep.length()) {
                            categoryNameParsed = resolvedTournName.substring(0, sepIdx).trim();
                            resolvedTournName = resolvedTournName.substring(sepIdx + sep.length()).trim();
                            break;
                        }
                    }
                }

                MatchResolutionRequest resolutionRequest = MatchResolutionRequest.builder()
                        .sportId(sport.sportId)
                        .sportSlug(sport.slug)
                        .tournamentId((tournament != null && Objects.equals(tournament.sportId, sport.sportId)) ? tournament.tournamentId : null)
                        .tournamentName(resolvedTournName)
                        .countryId(countryId)
                        .countryName(countryName)
                        .rawCategoryKey(rawCategoryKey)
                        .categoryName(categoryNameParsed != null ? categoryNameParsed : rawCountryOrCategoryName)
                        .homeTeamId((homeTeam != null && Objects.equals(homeTeam.sportId, sport.sportId)) ? homeTeam.teamId : null)
                        .homeTeamName(rawHomeTeamName != null && !rawHomeTeamName.isBlank() ? rawHomeTeamName : rawHomeTeamKey)
                        .awayTeamId((awayTeam != null && Objects.equals(awayTeam.sportId, sport.sportId)) ? awayTeam.teamId : null)
                        .awayTeamName(rawAwayTeamName != null && !rawAwayTeamName.isBlank() ? rawAwayTeamName : rawAwayTeamKey)
                        .startTimeEpochSec(targetSec)
                        .sourceOrigin("BOOKMAKER")
                        .createdBySource(activeSystemCode)
                        .build();

                String url = mdmServiceUrl + "/api/v1/mdm/matches/find-or-create";
                CanonicalMatchHeaderDto resolved = restTemplate.postForObject(url, resolutionRequest, CanonicalMatchHeaderDto.class);
                if (resolved != null && resolved.getMatchId() != null) {
                    matchId = resolved.getMatchId();
                    if (tournament == null && resolved.getTournamentId() != null) {
                        tournament = new CanonicalTournament(resolved.getTournamentId(), resolved.getSportId(), countryId, countryName, null, null, resolved.getTournamentName());
                        if (rawTournamentKey != null) blueprint.tournaments.put(rawTournamentKey, tournament);
                        if (rawTournamentName != null) {
                            String normName = rawTournamentName.trim().toLowerCase();
                            blueprint.tournamentsByName.put(normName, tournament);
                            if (countryId != null) {
                                blueprint.tournamentsByCountryAndName.put(countryId + ":" + normName, tournament);
                            }
                        }
                    }
                    if (homeTeam == null && resolved.getHomeTeamId() != null) {
                        homeTeam = new CanonicalTeam(resolved.getHomeTeamId(), resolved.getSportId(), countryId, countryName, null, null, resolved.getHomeTeamName());
                        if (rawHomeTeamKey != null) blueprint.teams.put(rawHomeTeamKey, homeTeam);
                        if (rawHomeTeamName != null) blueprint.teamsByName.put(rawHomeTeamName.trim().toLowerCase(), homeTeam);
                    }
                    if (awayTeam == null && resolved.getAwayTeamId() != null) {
                        awayTeam = new CanonicalTeam(resolved.getAwayTeamId(), resolved.getSportId(), countryId, countryName, null, null, resolved.getAwayTeamName());
                        if (rawAwayTeamKey != null) blueprint.teams.put(rawAwayTeamKey, awayTeam);
                        if (rawAwayTeamName != null) blueprint.teamsByName.put(rawAwayTeamName.trim().toLowerCase(), awayTeam);
                    }
                    blueprint.matchesByPair.computeIfAbsent(resolved.buildPairKey(), k -> new CopyOnWriteArrayList<>()).add(resolved);
                    blueprint.matchesByPair.computeIfAbsent(resolved.buildReversePairKey(), k -> new CopyOnWriteArrayList<>()).add(resolved);
                }
            } catch (Exception e) {
                log.warn("MDM find-or-create match fallback failed for [{}] {} vs {}: {}", activeSystemCode, rawHomeTeamName, rawAwayTeamName, e.getMessage());
            }
        }

        if (matchId == null || tournament == null || homeTeam == null || awayTeam == null) {
            if (tournament == null) {
                reportAnomaly(activeSystemCode, "TOURNAMENT", rawTournamentKey, rawTournamentName, rawSportKey, "Unmapped tournament in " + activeSystemCode);
            }
            if (homeTeam == null) {
                reportAnomaly(activeSystemCode, "TEAM", rawHomeTeamKey, rawHomeTeamName, rawTournamentKey, "Unmapped home team in " + activeSystemCode);
            }
            if (awayTeam == null) {
                reportAnomaly(activeSystemCode, "TEAM", rawAwayTeamKey, rawAwayTeamName, rawTournamentKey, "Unmapped away team in " + activeSystemCode);
            }
            return Optional.empty();
        }

        long startSec = startTime != null
                ? (startTime > 100_000_000_000L ? startTime / 1000 : startTime)
                : (System.currentTimeMillis() / 1000);

        CanonicalMatchEventDto event = CanonicalMatchEventDto.builder()
                .matchId(matchId)
                .sportId(sport.sportId)
                .sportSlug(sport.slug)
                .tournamentId(tournament.tournamentId)
                .tournamentName(tournament.canonicalName)
                .homeTeamId(homeTeam.teamId)
                .homeTeamName(homeTeam.canonicalName)
                .awayTeamId(awayTeam.teamId)
                .awayTeamName(awayTeam.canonicalName)
                .startTime(startSec)
                .isLive(Boolean.TRUE.equals(isLive))
                .externalSystemCode(activeSystemCode)
                .externalEventId(externalEventId)
                .eventUrl(eventUrl)
                .score1(score1)
                .score2(score2)
                .timer(timer)
                .scoreComment(scoreComment)
                .odds(odds != null ? odds : List.of())
                .build();

        return Optional.of(event);
    }

    /**
     * Resolves matchId from the local IngestionBlueprint matches cache within +/- 12 hours.
     */
    private Optional<Long> resolveMatchId(String systemCode, Integer sportId, Long tournamentId, Long homeTeamId, Long awayTeamId, Long startTimeMs) {
        if (homeTeamId == null || awayTeamId == null) return Optional.empty();
        IngestionBlueprint blueprint = getBlueprint(systemCode);

        String directKey = homeTeamId + ":" + awayTeamId;
        List<CanonicalMatchHeaderDto> candidates = blueprint.matchesByPair.get(directKey);
        if (candidates == null || candidates.isEmpty()) {
            String reverseKey = awayTeamId + ":" + homeTeamId;
            candidates = blueprint.matchesByPair.get(reverseKey);
        }
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }

        long targetSec = startTimeMs != null
                ? (startTimeMs > 100_000_000_000L ? startTimeMs / 1000 : startTimeMs)
                : (System.currentTimeMillis() / 1000);

        CanonicalMatchHeaderDto best = null;
        long bestDiff = Long.MAX_VALUE;

        for (CanonicalMatchHeaderDto m : candidates) {
            if (m.getSportId() != null && !m.getSportId().equals(sportId)) continue;
            long mSec = m.getStartTimeEpochSec() != null ? m.getStartTimeEpochSec() : 0L;
            long diff = Math.abs(mSec - targetSec);
            if (diff <= 12 * 3600 && diff < bestDiff) {
                bestDiff = diff;
                best = m;
            }
        }

        return best != null ? Optional.of(best.getMatchId()) : Optional.empty();
    }

    public void reportAnomaly(String entityType, String rawKey, String rawName, String rawParentKey, String details) {
        reportAnomaly(this.externalSystemCode, entityType, rawKey, rawName, rawParentKey, details);
    }

    public void reportAnomaly(String systemCode, String entityType, String rawKey, String rawName, String rawParentKey, String details) {
        if ((rawKey == null || "null".equalsIgnoreCase(rawKey.trim()) || rawKey.isBlank()) &&
            (rawName == null || "null".equalsIgnoreCase(rawName.trim()) || rawName.isBlank())) {
            return;
        }
        String activeCode = (systemCode != null && !systemCode.isBlank()) ? systemCode : this.externalSystemCode;
        log.warn("⚠️ [Stage-1 MDM Anomaly] [{}]: {}: key='{}', name='{}' ({})", activeCode, entityType, rawKey, rawName, details);
        if (kafkaTemplate != null) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("externalSystemCode", activeCode);
                payload.put("entityType", entityType);
                payload.put("rawKey", rawKey);
                payload.put("rawName", rawName);
                payload.put("rawParentKey", rawParentKey);
                payload.put("details", details);
                payload.put("detectedAt", OffsetDateTime.now().toString());

                String key = activeCode + ":" + entityType + ":" + (rawKey != null ? rawKey : rawName);
                kafkaTemplate.send(anomalyTopic, key, payload);
            } catch (Exception e) {
                log.error("Failed to dispatch unmapped anomaly to Kafka: {}", e.getMessage());
            }
        }
    }

    public void invalidateCache() {
        blueprintCache.invalidateAll();
    }

    public void invalidateCache(String systemCode) {
        blueprintCache.invalidate(systemCode);
    }

    public IngestionBlueprint getBlueprint() {
        return getBlueprint(this.externalSystemCode);
    }

    public IngestionBlueprint getBlueprint(String systemCode) {
        String code = (systemCode != null && !systemCode.isBlank()) ? systemCode : this.externalSystemCode;
        try {
            return blueprintCache.get(code, this::fetchBlueprint);
        } catch (Exception e) {
            log.warn("MDM Blueprint fetch failed for '{}' ({}), returning un-cached temporary fallback", code, e.getMessage());
            return new IngestionBlueprint();
        }
    }

    private IngestionBlueprint fetchBlueprint(String systemCode) {
        String url = mdmServiceUrl + "/api/v1/mdm/blueprint/" + systemCode;
        log.info("Fetching MDM Ingestion Blueprint for '{}' from: {}", systemCode, url);
        try {
            BlueprintRawResponse response = restTemplate.getForObject(url, BlueprintRawResponse.class);
            if (response != null && response.sports != null && !response.sports.isEmpty()) {
                return parseBlueprint(response);
            }
            log.warn("MDM Service returned null or empty sports for '{}'", systemCode);
            throw new IllegalStateException("Empty blueprint received for " + systemCode);
        } catch (Exception e) {
            log.warn("Could not reach MDM Service at {}: {}. Will retry on next request.", url, e.getMessage());
            throw new RuntimeException("Failed to fetch blueprint for " + systemCode, e);
        }
    }

    private IngestionBlueprint parseBlueprint(BlueprintRawResponse raw) {
        IngestionBlueprint bp = new IngestionBlueprint();
        if (raw.sports != null) {
            raw.sports.forEach((k, v) -> {
                CanonicalSport sport = new CanonicalSport(v.sportId, v.slug, v.domainKey);
                bp.sports.put(k, sport);
                if (k != null) {
                    bp.sports.putIfAbsent(k.trim().toLowerCase(), sport);
                }
                if (v.slug != null) {
                    bp.sportsBySlug.put(v.slug.toLowerCase(), sport);
                }
                if (v.domainKey != null) {
                    bp.sportsBySlug.put(v.domainKey.toLowerCase(), sport);
                }
            });
        }
        if (raw.countries != null) {
            raw.countries.forEach((k, v) -> {
                CanonicalCountry country = new CanonicalCountry(v.countryId, v.isoCode, v.canonicalName, v.flagCode);
                bp.countries.put(k, country);
                if (k != null) {
                    bp.countries.putIfAbsent(k.trim().toLowerCase(), country);
                }
                if (v.canonicalName != null) {
                    bp.countriesByName.put(v.canonicalName.trim().toLowerCase(), country);
                }
                if (v.isoCode != null) {
                    bp.countriesByName.put(v.isoCode.trim().toLowerCase(), country);
                }
            });
        }
        if (raw.tournaments != null) {
            raw.tournaments.forEach((k, v) -> {
                CanonicalTournament tourney = new CanonicalTournament(v.tournamentId, v.sportId, v.countryId, v.countryName, v.slug, v.sofascoreId, v.canonicalName);
                bp.tournaments.put(k, tourney);
                if (k != null) {
                    bp.tournaments.putIfAbsent(k.trim().toLowerCase(), tourney);
                }
                if (v.canonicalName != null) {
                    String normName = v.canonicalName.trim().toLowerCase();
                    bp.tournamentsByName.put(normName, tourney);
                    if (v.countryId != null) {
                        bp.tournamentsByCountryAndName.put(v.countryId + ":" + normName, tourney);
                    }
                }
            });
        }
        if (raw.teams != null) {
            raw.teams.forEach((k, v) -> {
                CanonicalTeam team = new CanonicalTeam(v.teamId, v.sportId, v.countryId, v.countryName, v.slug, v.sofascoreId, v.canonicalName);
                bp.teams.put(k, team);
                if (k != null) {
                    bp.teams.putIfAbsent(k.trim().toLowerCase(), team);
                }
                if (v.canonicalName != null) {
                    bp.teamsByName.put(v.canonicalName.trim().toLowerCase(), team);
                }
            });
        }
        if (raw.activeMatches != null) {
            for (CanonicalMatchHeaderDto m : raw.activeMatches) {
                bp.matchesByPair.computeIfAbsent(m.buildPairKey(), k -> new CopyOnWriteArrayList<>()).add(m);
                bp.matchesByPair.computeIfAbsent(m.buildReversePairKey(), k -> new CopyOnWriteArrayList<>()).add(m);
            }
        }
        return bp;
    }

    /**
     * Resolves an existing canonical tournament or creates a new one in MDM.
     */
    public Optional<CanonicalTournamentDto> findOrCreateTournament(TournamentResolutionRequest request) {
        if (request == null) return Optional.empty();
        try {
            String url = mdmServiceUrl + "/api/v1/mdm/tournaments/find-or-create";
            CanonicalTournamentDto dto = restTemplate.postForObject(url, request, CanonicalTournamentDto.class);
            return Optional.ofNullable(dto);
        } catch (Exception e) {
            log.error("Failed to find-or-create tournament in MDM: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Resolves an existing canonical team or creates a new one in MDM.
     */
    public Optional<CanonicalTeamDto> findOrCreateTeam(TeamResolutionRequest request) {
        if (request == null) return Optional.empty();
        try {
            String url = mdmServiceUrl + "/api/v1/mdm/teams/find-or-create";
            CanonicalTeamDto dto = restTemplate.postForObject(url, request, CanonicalTeamDto.class);
            return Optional.ofNullable(dto);
        } catch (Exception e) {
            log.error("Failed to find-or-create team in MDM: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Resolves an existing canonical match or creates a new one in MDM.
     */
    public Optional<CanonicalMatchHeaderDto> findOrCreateMatch(MatchResolutionRequest request) {
        if (request == null) return Optional.empty();
        try {
            String url = mdmServiceUrl + "/api/v1/mdm/matches/find-or-create";
            CanonicalMatchHeaderDto dto = restTemplate.postForObject(url, request, CanonicalMatchHeaderDto.class);
            return Optional.ofNullable(dto);
        } catch (Exception e) {
            log.error("Failed to find-or-create match in MDM: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<CanonicalCoach> lookupCoach(String systemCode, String rawKey, String rawName, Integer sportId) {
        if (rawKey == null && rawName == null) return Optional.empty();
        IngestionBlueprint blueprint = getBlueprint(systemCode);
        CanonicalCoach coach = null;
        if (rawKey != null) {
            coach = blueprint.coaches.get(rawKey.trim().toLowerCase());
        }
        if (coach == null && rawName != null) {
            coach = blueprint.coachesByName.get(rawName.trim().toLowerCase());
        }
        return Optional.ofNullable(coach);
    }

    public Optional<CanonicalPlayer> lookupPlayer(String systemCode, String rawKey, String rawName, Integer sportId) {
        if (rawKey == null && rawName == null) return Optional.empty();
        IngestionBlueprint blueprint = getBlueprint(systemCode);
        CanonicalPlayer player = null;
        if (rawKey != null) {
            player = blueprint.players.get(rawKey.trim().toLowerCase());
        }
        if (player == null && rawName != null) {
            player = blueprint.playersByName.get(rawName.trim().toLowerCase());
        }
        return Optional.ofNullable(player);
    }

    public static class IngestionBlueprint {
        public final Map<String, CanonicalSport> sports = new HashMap<>();
        public final Map<String, CanonicalSport> sportsBySlug = new HashMap<>();
        public final Map<String, CanonicalCountry> countries = new HashMap<>();
        public final Map<String, CanonicalCountry> countriesByName = new HashMap<>();
        public final Map<String, CanonicalTournament> tournaments = new HashMap<>();
        public final Map<String, CanonicalTournament> tournamentsByName = new HashMap<>();
        public final Map<String, CanonicalTournament> tournamentsByCountryAndName = new HashMap<>();
        public final Map<String, CanonicalTeam> teams = new HashMap<>();
        public final Map<String, CanonicalTeam> teamsByName = new HashMap<>();
        public final Map<String, CanonicalPlayer> players = new HashMap<>();
        public final Map<String, CanonicalPlayer> playersByName = new HashMap<>();
        public final Map<String, CanonicalCoach> coaches = new HashMap<>();
        public final Map<String, CanonicalCoach> coachesByName = new HashMap<>();
        public final Map<String, List<CanonicalMatchHeaderDto>> matchesByPair = new ConcurrentHashMap<>();
    }

    public record CanonicalSport(Integer sportId, String slug, String domainKey) implements Serializable {}
    public record CanonicalCountry(Integer countryId, String isoCode, String canonicalName, String flagCode) implements Serializable {}
    public record CanonicalTournament(Long tournamentId, Integer sportId, Integer countryId, String countryName, String slug, Long sofascoreId, String canonicalName) implements Serializable {}
    public record CanonicalTeam(Long teamId, Integer sportId, Integer countryId, String countryName, String slug, Long sofascoreId, String canonicalName) implements Serializable {}
    public record CanonicalPlayer(Long playerId, Integer sportId, Long sofascoreId, String slug, String canonicalName) implements Serializable {}
    public record CanonicalCoach(Long coachId, Integer sportId, Long sofascoreId, String slug, String canonicalName) implements Serializable {}

    public static class BlueprintRawResponse {
        public String externalSystemCode;
        public Map<String, RawSportDto> sports;
        public Map<String, RawCountryDto> countries;
        public Map<String, RawTournamentDto> tournaments;
        public Map<String, RawTeamDto> teams;
        public List<CanonicalMatchHeaderDto> activeMatches;
    }

    public static class RawSportDto {
        public Integer sportId;
        public String slug;
        public String domainKey;
    }

    public static class RawCountryDto {
        public Integer countryId;
        public String isoCode;
        public String canonicalName;
        public String flagCode;
    }

    public static class RawTournamentDto {
        public Long tournamentId;
        public Integer sportId;
        public Integer countryId;
        public String countryName;
        public String slug;
        public Long sofascoreId;
        public String canonicalName;
    }

    public static class RawTeamDto {
        public Long teamId;
        public Integer sportId;
        public Integer countryId;
        public String countryName;
        public String slug;
        public Long sofascoreId;
        public String canonicalName;
    }
}
