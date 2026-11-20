package pro.datawiki.igaming.mdm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.*;
import pro.datawiki.igaming.dto.signature.CyberMatchDetector;
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.dto.util.SimilarityUtils;
import pro.datawiki.igaming.mdm.domain.*;
import pro.datawiki.igaming.mdm.repository.*;
import pro.datawiki.igaming.mdm.util.CountryNormalizer;
import pro.datawiki.igaming.mdm.util.TournamentStageNormalizer;

import java.text.Normalizer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Authoritative single entry-point for resolving or creating canonical MDM entities across the ecosystem:
 * <ol>
 *   <li><b>Tournaments:</b> {@code /api/v1/mdm/tournaments/find-or-create} (scoped by country/category, international leagues).</li>
 *   <li><b>Teams & Athletes:</b> {@code /api/v1/mdm/teams/find-or-create} (phonetic normalizer, transliteration, alias resolution).</li>
 *   <li><b>Matches:</b> {@code /api/v1/mdm/matches/find-or-create} (temporal anchor matching, candidate proximity window).</li>
 * </ol>
 * <p>
 * Key architectural responsibilities:
 * <ul>
 *   <li><b>Participant Homogeneity Guard:</b> Enforces strict gender and age category matching
 *       ({@code validateMatchGenderAndAge}), routing violations to Kafka topic {@code mdm.match.mutants}.</li>
 *   <li><b>Satellite Sports Routing:</b> Automatically directs statistical events (yellow cards, fouls, offsides,
 *       goal kicks, corners, shots) to isolated satellite sport IDs (43..49), avoiding synthetic schedule collisions.</li>
 *   <li><b>Ingestion Blueprint:</b> Serves batch resolution vectors to crawlers for sub-50ms round-trips.</li>
 * </ul>
 *
 * @see MatchIntegrityValidator
 * @see pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer
 */
@Service
@Slf4j
public class MdmMatchResolutionService {

    /**
     * Generic tournament names that exist in many countries and MUST be resolved
     * with country context to prevent cross-country collisions.
     * E.g. "Premier League" exists in England, Bahrain, Nigeria, Ghana, Kuwait, etc.
     */
    private static final Set<String> AMBIGUOUS_GENERIC_LEAGUES = Set.of(
        // English
        "premier league", "serie a", "serie b", "primera division", "primera divisiones",
        "super league", "superleague", "first division", "superliga", "division 1", "division 2",
        "segunda division", "segunda liga", "tercera division", "liga 1", "liga 2", "liga 3",
        "nacional", "liga nacional", "cup", "fa cup", "national cup", "league cup",
        "super cup", "supercopa", "super cupen", "league one", "league two",
        "championship", "bundesliga", "eredivisie", "allsvenskan", "ekstraklasa", "pro league",
        // Russian
        "премьер-лига", "серия а", "серия b", "высшая лига", "первая лига",
        "вторая лига", "кубок", "суперкубок", "кубок лиги", "дивизион 1", "дивизион 2"
    );

    private final MdmMatchRepository matchRepository;
    private final MdmSportRepository sportRepository;
    private final MdmTournamentRepository tournamentRepository;
    private final MdmTeamRepository teamRepository;
    private final MdmTournamentTeamRepository tournamentTeamRepository;
    private final IngestionBlueprintService ingestionBlueprintService;
    private final AnomalyReportingService anomalyReportingService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String matchesUpsertedTopic;
    private final MdmExternalSystemRepository externalSystemRepository;
    private final MdmTeamIngestionMapRepository teamIngestionMapRepository;
    private final MdmTournamentIngestionMapRepository tournamentIngestionMapRepository;
    private final MdmTeamTranslationRepository teamTranslationRepository;
    private final MdmTournamentTranslationRepository tournamentTranslationRepository;
    private final MdmCategoryRepository categoryRepository;
    private final MdmCountryRepository countryRepository;
    private final MdmCountryIngestionMapRepository countryIngestionMapRepository;
    private final MdmTeamMergeMapRepository teamMergeMapRepository;
    private final MatchIntegrityValidator integrityValidator;
    private final Map<Long, Long> localTeamToSofaMapCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, MdmSport> sportBySlugCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<Integer, MdmSport> sportByIdCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, MdmExternalSystem> externalSystemByCodeCache = new java.util.concurrent.ConcurrentHashMap<>();

    private final com.github.benmanes.caffeine.cache.Cache<String, CanonicalMatchHeaderDto> matchResolutionCache =
            com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .expireAfterWrite(java.time.Duration.ofMinutes(10))
                    .maximumSize(100_000)
                    .build();
    private final com.github.benmanes.caffeine.cache.Cache<String, String> negativeResolutionCache =
            com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .expireAfterWrite(java.time.Duration.ofMinutes(10))
                    .maximumSize(50_000)
                    .build();
    private final com.github.benmanes.caffeine.cache.Cache<String, CanonicalTeamDto> teamResolutionCache =
            com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .expireAfterWrite(java.time.Duration.ofMinutes(15))
                    .maximumSize(100_000)
                    .build();
    private final com.github.benmanes.caffeine.cache.Cache<String, CanonicalTournamentDto> tournamentResolutionCache =
            com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .expireAfterWrite(java.time.Duration.ofMinutes(15))
                    .maximumSize(50_000)
                    .build();

    public Optional<MdmSport> findSportBySlugCached(String slug) {
        if (slug == null || sportRepository == null) return Optional.empty();
        String key = slug.trim().toLowerCase();
        MdmSport cached = sportBySlugCache.get(key);
        if (cached != null) return Optional.of(cached);
        Optional<MdmSport> found = sportRepository.findBySlug(key);
        found.ifPresent(s -> {
            sportBySlugCache.put(key, s);
            if (s.getId() != null) sportByIdCache.put(s.getId(), s);
        });
        return found;
    }

    public Optional<MdmSport> findSportByIdCached(Integer id) {
        if (id == null || sportRepository == null) return Optional.empty();
        MdmSport cached = sportByIdCache.get(id);
        if (cached != null) return Optional.of(cached);
        Optional<MdmSport> found = sportRepository.findById(id);
        found.ifPresent(s -> {
            sportByIdCache.put(id, s);
            if (s.getSlug() != null) sportBySlugCache.put(s.getSlug().trim().toLowerCase(), s);
        });
        return found;
    }

    public Optional<MdmExternalSystem> findExternalSystemByCodeCached(String code) {
        if (code == null || externalSystemRepository == null) return Optional.empty();
        String key = code.trim().toLowerCase();
        MdmExternalSystem cached = externalSystemByCodeCache.get(key);
        if (cached != null) return Optional.of(cached);
        Optional<MdmExternalSystem> found = externalSystemRepository.findByCode(key);
        found.ifPresent(sys -> externalSystemByCodeCache.put(key, sys));
        return found;
    }

    public Long remapTeamId(Long localId) {
        if (localId == null || teamMergeMapRepository == null) return localId;
        return localTeamToSofaMapCache.computeIfAbsent(localId, id ->
                teamMergeMapRepository.findByLocalId(id)
                        .map(pro.datawiki.igaming.mdm.domain.MdmTeamMergeMap::getSofaId)
                        .orElse(id)
        );
    }

    @Autowired
    public MdmMatchResolutionService(
            MdmMatchRepository matchRepository,
            MdmSportRepository sportRepository,
            MdmTournamentRepository tournamentRepository,
            MdmTeamRepository teamRepository,
            MdmTournamentTeamRepository tournamentTeamRepository,
            IngestionBlueprintService ingestionBlueprintService,
            AnomalyReportingService anomalyReportingService,
            @Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.matches-upserted:mdm.matches.upserted}") String matchesUpsertedTopic,
            @Autowired(required = false) MdmExternalSystemRepository externalSystemRepository,
            @Autowired(required = false) MdmTeamIngestionMapRepository teamIngestionMapRepository,
            @Autowired(required = false) MdmTournamentIngestionMapRepository tournamentIngestionMapRepository,
            @Autowired(required = false) MdmTeamTranslationRepository teamTranslationRepository,
            @Autowired(required = false) MdmTournamentTranslationRepository tournamentTranslationRepository,
            @Autowired(required = false) MdmCategoryRepository categoryRepository,
            @Autowired(required = false) MdmCountryRepository countryRepository,
            @Autowired(required = false) MdmCountryIngestionMapRepository countryIngestionMapRepository,
            @Autowired(required = false) MdmTeamMergeMapRepository teamMergeMapRepository,
            @Autowired(required = false) MatchIntegrityValidator integrityValidator
    ) {
        this.matchRepository = matchRepository;
        this.sportRepository = sportRepository;
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.ingestionBlueprintService = ingestionBlueprintService;
        this.anomalyReportingService = anomalyReportingService;
        this.kafkaTemplate = kafkaTemplate;
        this.matchesUpsertedTopic = matchesUpsertedTopic;
        this.externalSystemRepository = externalSystemRepository;
        this.teamIngestionMapRepository = teamIngestionMapRepository;
        this.tournamentIngestionMapRepository = tournamentIngestionMapRepository;
        this.teamTranslationRepository = teamTranslationRepository;
        this.tournamentTranslationRepository = tournamentTranslationRepository;
        this.categoryRepository = categoryRepository;
        this.countryRepository = countryRepository;
        this.countryIngestionMapRepository = countryIngestionMapRepository;
        this.teamMergeMapRepository = teamMergeMapRepository;
        this.integrityValidator = integrityValidator != null ? integrityValidator
                : new MatchIntegrityValidator(anomalyReportingService, tournamentTeamRepository, teamRepository);
    }

    public MdmMatchResolutionService(
            MdmMatchRepository matchRepository,
            MdmSportRepository sportRepository,
            MdmTournamentRepository tournamentRepository,
            MdmTeamRepository teamRepository,
            MdmTournamentTeamRepository tournamentTeamRepository,
            IngestionBlueprintService ingestionBlueprintService,
            AnomalyReportingService anomalyReportingService,
            KafkaTemplate<String, Object> kafkaTemplate,
            String matchesUpsertedTopic,
            MdmExternalSystemRepository externalSystemRepository,
            MdmTeamIngestionMapRepository teamIngestionMapRepository,
            MdmTournamentIngestionMapRepository tournamentIngestionMapRepository,
            MdmTeamTranslationRepository teamTranslationRepository,
            MdmTournamentTranslationRepository tournamentTranslationRepository,
            MdmCategoryRepository categoryRepository,
            MdmCountryRepository countryRepository,
            MdmCountryIngestionMapRepository countryIngestionMapRepository,
            MdmTeamMergeMapRepository teamMergeMapRepository
    ) {
        this(matchRepository, sportRepository, tournamentRepository, teamRepository,
                tournamentTeamRepository, ingestionBlueprintService, anomalyReportingService,
                kafkaTemplate, matchesUpsertedTopic, externalSystemRepository, teamIngestionMapRepository,
                tournamentIngestionMapRepository, teamTranslationRepository, tournamentTranslationRepository,
                categoryRepository, countryRepository, countryIngestionMapRepository, teamMergeMapRepository, null);
    }

    public MdmMatchResolutionService(
            MdmMatchRepository matchRepository,
            MdmSportRepository sportRepository,
            MdmTournamentRepository tournamentRepository,
            MdmTeamRepository teamRepository,
            MdmTournamentTeamRepository tournamentTeamRepository,
            IngestionBlueprintService ingestionBlueprintService,
            AnomalyReportingService anomalyReportingService,
            KafkaTemplate<String, Object> kafkaTemplate,
            String matchesUpsertedTopic
    ) {
        this(matchRepository, sportRepository, tournamentRepository, teamRepository,
                tournamentTeamRepository, ingestionBlueprintService, anomalyReportingService,
                kafkaTemplate, matchesUpsertedTopic, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Resolves an existing canonical match or creates a new one in MDM.
     * Guarantees that:
     * 1. A team cannot play against itself.
     * 2. Tournament, Home Team, and Away Team must strictly belong to the specified Sport.
     * 3. SofaScore events enrich and promote existing bookmaker matches.
     * 4. Updates are published to Kafka and invalidate blueprint caches for fast parser sync.
     */
    @Transactional
    public CanonicalMatchHeaderDto findOrCreateMatch(MatchResolutionRequest request) {
        if (request == null) throw new IllegalArgumentException("MatchResolutionRequest cannot be null");

        String cacheKey = buildResolutionCacheKey(request);
        boolean bypassCache = "SOFASCORE".equalsIgnoreCase(request.getSourceOrigin()) || request.getSofascoreId() != null;
        if (!bypassCache && cacheKey != null) {
            String negativeReason = negativeResolutionCache.getIfPresent(cacheKey);
            if (negativeReason != null) {
                throw new IllegalArgumentException(negativeReason);
            }
            CanonicalMatchHeaderDto cached = matchResolutionCache.getIfPresent(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        try {
            CanonicalMatchHeaderDto result = doFindOrCreateMatch(request);
            if (cacheKey != null && result != null) {
                matchResolutionCache.put(cacheKey, result);
            }
            return result;
        } catch (IllegalArgumentException e) {
            if (!bypassCache && cacheKey != null) {
                negativeResolutionCache.put(cacheKey, e.getMessage() != null ? e.getMessage() : "Invalid match");
            }
            throw e;
        }
    }

    private String buildResolutionCacheKey(MatchResolutionRequest request) {
        if (request == null) return null;
        String src = request.getCreatedBySource() != null ? request.getCreatedBySource() : "src";
        Integer sport = request.getSportId();
        if (sport == null && request.getSportSlug() != null) {
            sport = findSportBySlugCached(request.getSportSlug()).map(MdmSport::getId).orElse(null);
            if (sport != null) {
                request.setSportId(sport);
            }
        }
        String h = request.getHomeTeamName() != null ? request.getHomeTeamName().trim().toLowerCase() : "";
        if (h.isEmpty() && request.getHomeTeamId() != null) {
            h = "id:" + request.getHomeTeamId();
        }
        String a = request.getAwayTeamName() != null ? request.getAwayTeamName().trim().toLowerCase() : "";
        if (a.isEmpty() && request.getAwayTeamId() != null) {
            a = "id:" + request.getAwayTeamId();
        }
        Long epoch = request.getStartTimeEpochSec();
        long bucket = epoch != null ? (epoch / 300) : 0L;
        return src + ":" + sport + ":" + h + ":" + a + ":" + bucket;
    }

    private CanonicalMatchHeaderDto doFindOrCreateMatch(MatchResolutionRequest request) {
        OffsetDateTime targetTime = toOffsetDateTime(request.getStartTimeEpochSec());

        // 0. Smart Sport Dispatcher & Cross-Sport Disinfection
        dispatchTrueSport(request);

        // Normalize pre-resolved team IDs via merge map
        if (teamMergeMapRepository != null) {
            if (request.getHomeTeamId() != null) {
                request.setHomeTeamId(remapTeamId(request.getHomeTeamId()));
            }
            if (request.getAwayTeamId() != null) {
                request.setAwayTeamId(remapTeamId(request.getAwayTeamId()));
            }
        }

        // 1. O(1) Check by SofaScore ID if present
        if (request.getSofascoreId() != null) {
            Optional<MdmMatch> sofaMatch = matchRepository.findBySofascoreId(request.getSofascoreId());
            if (sofaMatch.isPresent()) {
                MdmMatch existing = sofaMatch.get();
                boolean changed = false;
                if (targetTime != null && Math.abs(existing.getStartTime().toEpochSecond() - targetTime.toEpochSecond()) > 300) {
                    existing.setStartTime(targetTime);
                    changed = true;
                }
                if (changed) {
                    existing = matchRepository.save(existing);
                    broadcastMatchUpserted(existing);
                }
                if (request.getSofascoreHomeTeamId() != null && existing.getHomeTeam() != null && existing.getHomeTeam().getSofascoreId() == null) {
                    enrichTeamSofascoreId(existing.getHomeTeam(), request.getSofascoreHomeTeamId());
                }
                if (request.getSofascoreAwayTeamId() != null && existing.getAwayTeam() != null && existing.getAwayTeam().getSofascoreId() == null) {
                    enrichTeamSofascoreId(existing.getAwayTeam(), request.getSofascoreAwayTeamId());
                }
                return toHeaderDto(existing);
            }
        }

        // 2. Early Temporal Match-Anchor Resolution (Match-First Cross-Linking)
        // Prevents duplicate entity generation by anchoring to scheduled canonical/SofaScore matches in window
        if (request.getSportId() == null && request.getSportSlug() != null) {
            findSportBySlugCached(request.getSportSlug())
                    .ifPresent(s -> request.setSportId(s.getId()));
        }
        if (request.getSportId() != null && targetTime != null
                && request.getHomeTeamName() != null && request.getAwayTeamName() != null) {
            CanonicalMatchHeaderDto anchored = tryResolveByTemporalMatchAnchor(request, targetTime);
            if (anchored != null) {
                return anchored;
            }
        }

        resolveEntities(request);
        if (teamMergeMapRepository != null) {
            if (request.getHomeTeamId() != null) {
                request.setHomeTeamId(remapTeamId(request.getHomeTeamId()));
            }
            if (request.getAwayTeamId() != null) {
                request.setAwayTeamId(remapTeamId(request.getAwayTeamId()));
            }
        }
        validateRequest(request);

        if (isSyntheticLeague(request)) {
            String src = request.getCreatedBySource() != null ? request.getCreatedBySource() : "UNKNOWN";
            log.warn("⚠️ [MDM Synthetic Anomaly] Potential synthetic/fantasy/SRL league: '{}' for match '{} vs {}' (system={})",
                    request.getTournamentName(), request.getHomeTeamName(), request.getAwayTeamName(), src);
            if (anomalyReportingService != null) {
                anomalyReportingService.reportLeagueMismatch(
                        src,
                        "SUSPICIOUS_SYNTHETIC_LEAGUE",
                        request.getTournamentId(),
                        request.getTournamentName(),
                        request.getHomeTeamId(),
                        request.getHomeTeamName(),
                        "Potential synthetic/fantasy/SRL league: " + request.getTournamentName()
                );
            }
        }

        Integer sportId = request.getSportId();
        Long homeId = request.getHomeTeamId();
        Long awayId = request.getAwayTeamId();

        // 2. Search by Team Pair within a flexible +/- 18-hour window
        OffsetDateTime windowStart = targetTime.minusHours(18);
        OffsetDateTime windowEnd = targetTime.plusHours(18);
        List<MdmMatch> candidates = matchRepository.findCandidateMatches(sportId, homeId, awayId, windowStart, windowEnd);

        if (!candidates.isEmpty()) {
            // Pick candidate closest in time
            MdmMatch matched = findClosestCandidate(candidates, targetTime);
            if (matched.getHomeTeam() != null && matched.getAwayTeam() != null) {
                validateMatchGenderAndAge(matched.getTournament(), matched.getHomeTeam(), matched.getAwayTeam(), request.getCreatedBySource());
            }
            boolean updated = false;

            // If incoming has a target time, and matched is SCHEDULED, and difference > 300 seconds:
            // Dynamically propagate updated Order of Play / match start time
            if (targetTime != null && "SCHEDULED".equals(matched.getStatus())
                    && Math.abs(matched.getStartTime().toEpochSecond() - targetTime.toEpochSecond()) > 300) {
                log.info("⏰ [MDM] Updating start time for match #{} ({}-{}) from {} to {} (source={})",
                        matched.getId(), matched.getHomeTeam().getCanonicalName(), matched.getAwayTeam().getCanonicalName(),
                        matched.getStartTime(), targetTime, request.getCreatedBySource());
                matched.setStartTime(targetTime);
                updated = true;
            }

            // If incoming is SofaScore and matched match does not have sofascoreId, enrich it
            if (request.getSofascoreId() != null && matched.getSofascoreId() == null) {
                log.info("💎 [MDM] Promoting match #{} ({}) with SofaScore ID {}",
                        matched.getId(), matched.getSourceOrigin(), request.getSofascoreId());
                matched.setSofascoreId(request.getSofascoreId());
                matched.setSourceOrigin("SOFASCORE");
                if (targetTime != null) {
                    matched.setStartTime(targetTime);
                }
                if (request.getTournamentId() != null) {
                    tournamentRepository.findById(request.getTournamentId())
                            .filter(t -> Objects.equals(t.getSport().getId(), sportId))
                            .ifPresent(matched::setTournament);
                }
                if (request.getSofascoreHomeTeamId() != null && matched.getHomeTeam() != null && matched.getHomeTeam().getSofascoreId() == null) {
                    enrichTeamSofascoreId(matched.getHomeTeam(), request.getSofascoreHomeTeamId());
                }
                if (request.getSofascoreAwayTeamId() != null && matched.getAwayTeam() != null && matched.getAwayTeam().getSofascoreId() == null) {
                    enrichTeamSofascoreId(matched.getAwayTeam(), request.getSofascoreAwayTeamId());
                }
                updated = true;
            }

            if (request.getStage() != null && matched.getStage() == null) {
                matched.setStage(request.getStage());
                matched.setRound(request.getRound());
                updated = true;
            }

            if (request.getBestOf() != null && request.getBestOf() > 0 && !Objects.equals(matched.getBestOf(), request.getBestOf())) {
                matched.setBestOf(request.getBestOf());
                updated = true;
            } else if (matched.getBestOf() == null) {
                int parsed = CanonicalMatchHeaderDto.parseBestOfFromStrings(matched.getStage(), matched.getRound(),
                        matched.getTournament() != null ? matched.getTournament().getCanonicalName() : null);
                matched.setBestOf(parsed);
                updated = true;
            }

            if (updated) {
                matched = matchRepository.save(matched);
                broadcastMatchUpserted(matched);
            }
            return toHeaderDto(matched);
        }

        // 3. Match not found -> Create new Canonical MDM Match
        MdmSport sport = findSportByIdCached(sportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found with ID: " + sportId));

        MdmTournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found with ID: " + request.getTournamentId()));

        MdmTeam homeTeam = teamRepository.findById(homeId)
                .orElseThrow(() -> new IllegalArgumentException("Home team not found with ID: " + homeId));

        MdmTeam awayTeam = teamRepository.findById(awayId)
                .orElseThrow(() -> new IllegalArgumentException("Away team not found with ID: " + awayId));

        String createdBy = request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank()
                ? request.getCreatedBySource()
                : "system";

        // Strict cross-sport validation
        if (!Objects.equals(tournament.getSport().getId(), sportId)) {
            String msg = String.format("Integrity violation: Tournament '%s' (sport=%d) does not match requested sport %d",
                    tournament.getCanonicalName(), tournament.getSport().getId(), sportId);
            anomalyReportingService.reportLeagueMismatch(createdBy, "SPORT_MISMATCH", tournament.getId(), tournament.getCanonicalName(), null, null, msg);
            throw new IllegalArgumentException(msg);
        }
        if (!Objects.equals(homeTeam.getSport().getId(), sportId)) {
            String msg = String.format("Integrity violation: Home Team '%s' (sport=%d) does not match requested sport %d",
                    homeTeam.getCanonicalName(), homeTeam.getSport().getId(), sportId);
            anomalyReportingService.reportLeagueMismatch(createdBy, "SPORT_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
            throw new IllegalArgumentException(msg);
        }
        if (!Objects.equals(awayTeam.getSport().getId(), sportId)) {
            String msg = String.format("Integrity violation: Away Team '%s' (sport=%d) does not match requested sport %d",
                    awayTeam.getCanonicalName(), awayTeam.getSport().getId(), sportId);
            anomalyReportingService.reportLeagueMismatch(createdBy, "SPORT_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
            throw new IllegalArgumentException(msg);
        }

        String origin = sanitizeMatchOrigin(request.getSourceOrigin(), request.getSofascoreId());

        // Strict League Integrity: Gender, Age, Category/Country and Roster Membership
        validateLeagueIntegrity(tournament, homeTeam, awayTeam, origin, createdBy);

        int effectiveBestOf = request.getBestOf() != null && request.getBestOf() > 0
                ? request.getBestOf()
                : CanonicalMatchHeaderDto.parseBestOfFromStrings(request.getStage(), request.getRound(), tournament.getCanonicalName());

        MdmMatch newMatch = MdmMatch.builder()
                .sport(sport)
                .tournament(tournament)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .startTime(targetTime)
                .sofascoreId(request.getSofascoreId())
                .sourceOrigin(origin)
                .createdBySource(createdBy)
                .status("SCHEDULED")
                .stage(request.getStage())
                .round(request.getRound())
                .bestOf(effectiveBestOf)
                .build();

        MdmMatch saved = matchRepository.save(newMatch);
        log.info("✨ [MDM] Created canonical match #{}: sport={} ('{}'), tourn='{}', {} vs {}, start={}, origin={}/{}",
                saved.getId(), sport.getId(), sport.getSlug(), tournament.getCanonicalName(),
                homeTeam.getCanonicalName(), awayTeam.getCanonicalName(), saved.getStartTime(),
                saved.getSourceOrigin(), saved.getCreatedBySource());

        // Auto-register participants in tournament roster
        registerTournamentParticipant(tournament, homeTeam);
        registerTournamentParticipant(tournament, awayTeam);

        broadcastMatchUpserted(saved);
        return toHeaderDto(saved);
    }

    @Transactional
    public CanonicalMatchHeaderDto cancelMatch(Long matchId, String reason) {
        MdmMatch match = matchRepository.findByIdWithDetails(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + matchId));
        log.warn("🚫 [MDM] Cancelling match #{}: {} vs {} (reason: {})",
                matchId, match.getHomeTeam().getCanonicalName(), match.getAwayTeam().getCanonicalName(), reason);
        match.setStatus("CANCELLED");
        match = matchRepository.save(match);
        broadcastMatchUpserted(match);
        matchResolutionCache.invalidateAll();
        negativeResolutionCache.invalidateAll();
        return toHeaderDto(match);
    }

    @Transactional
    public CanonicalMatchHeaderDto promoteGroundTruth(Long matchId, Long sofascoreId, String sourceOrigin, String reason) {
        MdmMatch match = matchRepository.findByIdWithDetails(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + matchId));
        log.info("💎 [MDM] Promoting match #{}: {} vs {} to ground truth (sofascoreId={}, reason: {})",
                matchId, match.getHomeTeam().getCanonicalName(), match.getAwayTeam().getCanonicalName(), sofascoreId, reason);
        match.setStatus("SCHEDULED");
        if (sourceOrigin != null && !sourceOrigin.isBlank()) {
            match.setSourceOrigin(sourceOrigin);
        } else {
            match.setSourceOrigin("SOFASCORE");
        }
        if (sofascoreId != null) {
            match.setSofascoreId(sofascoreId);
        }
        match = matchRepository.save(match);
        broadcastMatchUpserted(match);
        matchResolutionCache.invalidateAll();
        negativeResolutionCache.invalidateAll();
        return toHeaderDto(match);
    }

    private void resolveEntities(MatchResolutionRequest request) {
        if (request == null) return;

        // 1. Resolve Sport if missing
        if (request.getSportId() == null && request.getSportSlug() != null) {
            findSportBySlugCached(request.getSportSlug())
                    .ifPresent(s -> request.setSportId(s.getId()));
        }

        // 1b. Normalize tournament name and extract match stage/round
        if (request.getTournamentName() != null) {
            TournamentStageNormalizer.StageResult stageRes = TournamentStageNormalizer.extract(request.getTournamentName());
            if (request.getStage() == null && stageRes.stage() != null) {
                request.setStage(stageRes.stage());
                request.setRound(stageRes.round());
            }
            if (stageRes.cleanTournamentName() != null && !stageRes.cleanTournamentName().isBlank()) {
                request.setTournamentName(stageRes.cleanTournamentName());
            }
        }

        // 2. Resolve or Auto-create Tournament FIRST (to provide context for teams)
        Integer resolvedCountryId = request.getCountryId();
        if (request.getTournamentId() == null) {
            TournamentResolutionRequest tReq = TournamentResolutionRequest.builder()
                    .sportId(request.getSportId())
                    .sportSlug(request.getSportSlug())
                    .sofascoreTournamentId(request.getSofascoreTournamentId())
                    .tournamentName(request.getTournamentName())
                    .categoryName(request.getCategoryName())
                    .countryName(request.getCountryName())
                    .countryId(request.getCountryId())
                    .rawCategoryKey(request.getRawCategoryKey())
                    .rawKey(request.getRawTournamentKey())
                    .sourceId(request.getTournamentSourceId())
                    .isInternational(request.getIsInternational())
                    .createdBySource(request.getCreatedBySource())
                    .sourceOrigin(request.getSourceOrigin())
                    .build();
            CanonicalTournamentDto tDto = findOrCreateTournament(tReq);
            request.setTournamentId(tDto.getTournamentId());
            if (resolvedCountryId == null && tDto.getCountryId() != null) {
                resolvedCountryId = tDto.getCountryId();
            }
        }

        // 3. Resolve or Auto-create Home Team (inheriting tournament context)
        if (request.getHomeTeamId() == null) {
            TeamResolutionRequest homeReq = TeamResolutionRequest.builder()
                    .sportId(request.getSportId())
                    .sportSlug(request.getSportSlug())
                    .tournamentId(request.getTournamentId())
                    .tournamentName(request.getTournamentName())
                    .sofascoreTeamId(request.getSofascoreHomeTeamId())
                    .teamName(request.getHomeTeamName())
                    .rawKey(request.getRawHomeTeamKey())
                    .sourceId(request.getHomeTeamSourceId())
                    .countryName(request.getCountryName())
                    .countryId(resolvedCountryId)
                    .categoryName(request.getCategoryName())
                    .rawCategoryKey(request.getRawCategoryKey())
                    .createdBySource(request.getCreatedBySource())
                    .sourceOrigin(request.getSourceOrigin())
                    .build();
            CanonicalTeamDto homeDto = findOrCreateTeam(homeReq);
            request.setHomeTeamId(homeDto.getTeamId());
        }

        // 4. Resolve or Auto-create Away Team (inheriting tournament context)
        if (request.getAwayTeamId() == null) {
            TeamResolutionRequest awayReq = TeamResolutionRequest.builder()
                    .sportId(request.getSportId())
                    .sportSlug(request.getSportSlug())
                    .tournamentId(request.getTournamentId())
                    .tournamentName(request.getTournamentName())
                    .sofascoreTeamId(request.getSofascoreAwayTeamId())
                    .teamName(request.getAwayTeamName())
                    .rawKey(request.getRawAwayTeamKey())
                    .sourceId(request.getAwayTeamSourceId())
                    .countryName(request.getCountryName())
                    .countryId(resolvedCountryId)
                    .categoryName(request.getCategoryName())
                    .rawCategoryKey(request.getRawCategoryKey())
                    .createdBySource(request.getCreatedBySource())
                    .sourceOrigin(request.getSourceOrigin())
                    .build();
            CanonicalTeamDto awayDto = findOrCreateTeam(awayReq);
            request.setAwayTeamId(awayDto.getTeamId());
        }
    }

    /**
     * Resolves an MDM Category by country name, category name or international flag.
     * Auto-creates the category if recognized country is missing in the sport taxonomy.
     */
    public MdmCategory resolveCategory(Integer sportId, String countryName, String categoryName, Boolean isInternational) {
        if (categoryRepository == null || sportId == null) return null;

        String target = countryName != null && !countryName.isBlank() ? countryName.trim() : (categoryName != null ? categoryName.trim() : null);
        CountryNormalizer.CountryInfo countryInfo = CountryNormalizer.normalize(target, isInternational);

        if (countryInfo != null) {
            Optional<MdmCategory> bySlug = categoryRepository.findFirstBySportIdAndSlugIgnoreCase(sportId, countryInfo.slug());
            if (bySlug.isPresent()) {
                return bySlug.get();
            }
            MdmSport sport = findSportByIdCached(sportId).orElse(null);
            if (sport != null) {
                try {
                    MdmCategory newCat = MdmCategory.builder()
                            .sport(sport)
                            .slug(countryInfo.slug())
                            .code(countryInfo.code())
                            .sourceOrigin("NATIVE_LOCAL")
                            .build();
                    return categoryRepository.save(newCat);
                } catch (Exception e) {
                    log.warn("Failed to create category for slug {}: {}", countryInfo.slug(), e.getMessage());
                    return categoryRepository.findFirstBySportIdAndSlugIgnoreCase(sportId, countryInfo.slug()).orElse(null);
                }
            }
        }

        if (categoryName != null && !categoryName.isBlank()) {
            String slug = toSlug(categoryName);
            Optional<MdmCategory> bySlug = categoryRepository.findFirstBySportIdAndSlugIgnoreCase(sportId, slug);
            if (bySlug.isPresent()) {
                return bySlug.get();
            }
        }

        return null;
    }

    /**
     * Dedicated method to find an existing canonical tournament or create a new one.
     * Takes country/category and international scope into account to eliminate duplicate leagues.
     */
    @Transactional
    public CanonicalTournamentDto findOrCreateTournament(TournamentResolutionRequest request) {
        if (request == null) throw new IllegalArgumentException("TournamentResolutionRequest cannot be null");
        String cacheKey = buildTournamentResolutionCacheKey(request);
        if (cacheKey != null) {
            CanonicalTournamentDto cached = tournamentResolutionCache.getIfPresent(cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        CanonicalTournamentDto result = doFindOrCreateTournament(request);
        if (cacheKey != null && result != null) {
            tournamentResolutionCache.put(cacheKey, result);
        }
        return result;
    }

    private String buildTournamentResolutionCacheKey(TournamentResolutionRequest request) {
        if (request == null) return null;
        Integer sportId = request.getSportId();
        Long sofaId = request.getSofascoreTournamentId();
        String src = request.getCreatedBySource() != null ? request.getCreatedBySource() : "";
        String sId = request.getSourceId() != null ? request.getSourceId() : "";
        String rKey = request.getRawKey() != null ? request.getRawKey() : "";
        String tName = request.getTournamentName() != null ? request.getTournamentName().trim().toLowerCase() : "";
        Integer countryId = request.getCountryId();
        String countryName = request.getCountryName() != null ? request.getCountryName().trim().toLowerCase() : "";
        return sportId + ":" + (sofaId != null ? sofaId : "") + ":" + src + ":" + sId + ":" + rKey + ":" + tName + ":" + (countryId != null ? countryId : "") + ":" + countryName;
    }

    private CanonicalTournamentDto doFindOrCreateTournament(TournamentResolutionRequest request) {
        if (request == null) throw new IllegalArgumentException("TournamentResolutionRequest cannot be null");
        if (request.getSportId() == null && request.getSportSlug() != null) {
            findSportBySlugCached(request.getSportSlug())
                    .ifPresent(s -> request.setSportId(s.getId()));
        }
        Integer sportId = request.getSportId();
        if (sportId == null) throw new IllegalArgumentException("Sport ID is mandatory");

        Long sofaId = request.getSofascoreTournamentId();
        String tournName = request.getTournamentName();
        if (tournName != null) {
            tournName = tournName.trim();
            TournamentStageNormalizer.StageResult stageRes = TournamentStageNormalizer.extract(tournName);
            if (stageRes.cleanTournamentName() != null && !stageRes.cleanTournamentName().isBlank()) {
                tournName = stageRes.cleanTournamentName().trim();
                request.setTournamentName(tournName);
            }
        }
        String countryName = request.getCountryName() != null ? request.getCountryName().trim() : null;
        Boolean isIntl = request.getIsInternational();

        // 1. SofaScore ID lookup
        if (sofaId != null) {
            Optional<MdmTournament> bySofa = tournamentRepository.findBySofascoreId(sofaId);
            if (bySofa.isPresent()) {
                MdmTournament t = bySofa.get();
                enrichTournamentCountry(t, countryName);
                return toTournamentDto(t);
            }
        }

        // Determine country/category context
        MdmCountry resolvedCountry = null;
        if (request.getCountryId() != null && countryRepository != null) {
            resolvedCountry = countryRepository.findById(request.getCountryId()).orElse(null);
        }
        if (resolvedCountry == null) {
            resolvedCountry = resolveCountryEntity(
                    request.getCreatedBySource(),
                    request.getRawCategoryKey(),
                    countryName,
                    request.getCategoryName(),
                    isIntl
            );
        }
        Integer resolvedCountryId = resolvedCountry != null ? resolvedCountry.getId() : null;

        String countryCtx = null;
        if (resolvedCountry != null && resolvedCountry.getCanonicalName() != null) {
            countryCtx = resolvedCountry.getCanonicalName();
        } else if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            countryCtx = request.getCategoryName().trim();
        } else if (countryName != null && !countryName.isBlank()) {
            countryCtx = countryName;
        }

        MdmCategory category = resolveCategory(sportId, countryCtx, request.getCategoryName(), isIntl);
        if (category != null && countryCtx == null) {
            countryCtx = category.getSlug();
        }

        // 2. Check external system ingestion map first (by sourceId, rawKey, or composite key)
        if (request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank()
                && externalSystemRepository != null && tournamentIngestionMapRepository != null) {
            Optional<MdmExternalSystem> sysOpt = findExternalSystemByCodeCached(request.getCreatedBySource());
            if (sysOpt.isPresent()) {
                Integer sysId = sysOpt.get().getId();

                // 2a. Direct lookup by sourceId if provided
                if (request.getSourceId() != null && !request.getSourceId().isBlank()) {
                    Optional<MdmTournamentIngestionMap> mapBySource = tournamentIngestionMapRepository.findFirstByExternalSystemIdAndSourceId(sysId, request.getSourceId().trim());
                    if (mapBySource.isPresent() && mapBySource.get().getCanonicalTournament() != null) {
                        MdmTournament mappedTourn = mapBySource.get().getCanonicalTournament();
                        if (Objects.equals(mappedTourn.getSport().getId(), sportId)) {
                            enrichTournamentCountry(mappedTourn, countryCtx);
                            return toTournamentDto(mappedTourn);
                        }
                    }
                }

                // 2b. Direct lookup by rawKey if provided
                if (request.getRawKey() != null && !request.getRawKey().isBlank()) {
                    Optional<MdmTournamentIngestionMap> mapByRaw = tournamentIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, request.getRawKey().trim());
                    if (mapByRaw.isPresent() && mapByRaw.get().getCanonicalTournament() != null) {
                        MdmTournament mappedTourn = mapByRaw.get().getCanonicalTournament();
                        if (Objects.equals(mappedTourn.getSport().getId(), sportId)) {
                            enrichTournamentCountry(mappedTourn, countryCtx);
                            return toTournamentDto(mappedTourn);
                        }
                    }
                }

                String compositeKey = countryCtx != null && tournName != null ? (countryCtx + " - " + tournName) : null;
                Optional<MdmTournamentIngestionMap> mapOpt = compositeKey != null
                        ? tournamentIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, compositeKey)
                        : Optional.empty();
                if (mapOpt.isEmpty() && tournName != null) {
                    mapOpt = tournamentIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, tournName);
                }
                if (mapOpt.isPresent() && mapOpt.get().getCanonicalTournament() != null) {
                    MdmTournament mappedTourn = mapOpt.get().getCanonicalTournament();
                    if (Objects.equals(mappedTourn.getSport().getId(), sportId)) {
                        enrichTournamentCountry(mappedTourn, countryCtx);
                        return toTournamentDto(mappedTourn);
                    }
                }
            }
        }

        // 3. Check translation table
        if (tournName != null && tournamentTranslationRepository != null) {
            Optional<MdmTournamentTranslation> trans = tournamentTranslationRepository.findFirstByNameIgnoreCase(tournName);
            if (trans.isPresent() && trans.get().getTournament() != null
                    && Objects.equals(trans.get().getTournament().getSport().getId(), sportId)) {
                MdmTournament t = trans.get().getTournament();
                if (isAmbiguousLeagueName(tournName) && countryCtx != null && t.getCountryName() != null
                        && !countryCtx.equalsIgnoreCase(t.getCountryName())) {
                    log.debug("[MDM] Skipping translation match '{}' (country mismatch: {} vs {})", tournName, countryCtx, t.getCountryName());
                } else {
                    enrichTournamentCountry(t, countryCtx);
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }
            }
        }

        if (tournName != null) {
            boolean isAmbiguous = isAmbiguousLeagueName(tournName);

            // 4. Country-scoped lookup (strict separation by canonical country)
            if (resolvedCountryId != null) {
                Optional<MdmTournament> byCountryAndName = tournamentRepository.findFirstBySportIdAndCountryIdAndCanonicalNameIgnoreCase(
                        sportId, resolvedCountryId, tournName);
                if (byCountryAndName.isPresent()) {
                    MdmTournament t = byCountryAndName.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    enrichTournamentCountry(t, countryCtx, resolvedCountry);
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }
                String slug = toSlug(tournName);
                Optional<MdmTournament> byCountryAndSlug = tournamentRepository.findFirstBySportIdAndCountryIdAndSlug(
                        sportId, resolvedCountryId, slug);
                if (byCountryAndSlug.isPresent()) {
                    MdmTournament t = byCountryAndSlug.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    enrichTournamentCountry(t, countryCtx, resolvedCountry);
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }
            }

            // 5. Category-scoped lookup (strict separation by country category)
            if (category != null) {
                Optional<MdmTournament> byCatAndName = tournamentRepository.findFirstBySportIdAndCategoryIdAndCanonicalNameIgnoreCase(
                        sportId, category.getId(), tournName);
                if (byCatAndName.isPresent()) {
                    MdmTournament t = byCatAndName.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    enrichTournamentCountry(t, countryCtx, resolvedCountry);
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }
                String slug = toSlug(tournName);
                Optional<MdmTournament> byCatAndSlug = tournamentRepository.findBySportIdAndCategoryIdAndSlug(
                        sportId, category.getId(), slug);
                if (byCatAndSlug.isPresent()) {
                    MdmTournament t = byCatAndSlug.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    enrichTournamentCountry(t, countryCtx, resolvedCountry);
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }
            }

            // 6. Country-aware lookup by countryName
            if (countryCtx != null) {
                Optional<MdmTournament> byNameAndCountry = tournamentRepository.findFirstBySportIdAndNameAndCountry(
                        sportId, tournName, countryCtx);
                if (byNameAndCountry.isPresent()) {
                    MdmTournament t = byNameAndCountry.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    enrichTournamentCountry(t, countryCtx, resolvedCountry);
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }
            }

            // 7. Global lookup ONLY if NOT ambiguous and country is unknown
            if (!isAmbiguous && resolvedCountryId == null && countryCtx == null) {
                Optional<MdmTournament> byName = tournamentRepository.findFirstBySportIdAndCanonicalNameIgnoreCase(sportId, tournName);
                if (byName.isPresent()) {
                    MdmTournament t = byName.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    enrichTournamentCountry(t, countryCtx, resolvedCountry);
                    if (t.getCategory() == null && category != null) {
                        t.setCategory(category);
                        t = tournamentRepository.save(t);
                    }
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }

                String slug = toSlug(tournName);
                Optional<MdmTournament> bySlug = tournamentRepository.findFirstBySportIdAndSlug(sportId, slug);
                if (bySlug.isPresent()) {
                    MdmTournament t = bySlug.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    enrichTournamentCountry(t, countryCtx, resolvedCountry);
                    if (t.getCategory() == null && category != null) {
                        t.setCategory(category);
                        t = tournamentRepository.save(t);
                    }
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }
            }
        }

        // 8. Cross-language / Fuzzy Tournament lookup against existing canonical tournaments
        if (tournName != null && !tournName.isBlank()) {
            String lowerTourn = tournName.toLowerCase();
            String detectedGender = request.getGender() != null ? request.getGender().toUpperCase() : "MALE";
            if (request.getGender() == null) {
                if (lowerTourn.contains("женщин") || lowerTourn.contains("women") || lowerTourn.contains("female") || lowerTourn.contains("damas") || lowerTourn.contains("dames") || lowerTourn.contains("frauen")) {
                    detectedGender = "FEMALE";
                }
            }
            String detectedAge = request.getAgeCategory() != null ? request.getAgeCategory().toUpperCase() : "SENIOR";
            if (request.getAgeCategory() == null) {
                if (lowerTourn.contains("23") && (lowerTourn.contains("u23") || lowerTourn.contains("u-23") || lowerTourn.contains("до 23") || lowerTourn.contains("23 лет") || lowerTourn.contains("23-"))) {
                    detectedAge = "U23";
                } else if (lowerTourn.contains("21") && (lowerTourn.contains("u21") || lowerTourn.contains("u-21") || lowerTourn.contains("до 21") || lowerTourn.contains("21 лет") || lowerTourn.contains("21-"))) {
                    detectedAge = "U21";
                } else if (lowerTourn.contains("20") && (lowerTourn.contains("u20") || lowerTourn.contains("u-20") || lowerTourn.contains("до 20") || lowerTourn.contains("20 лет") || lowerTourn.contains("20-"))) {
                    detectedAge = "U20";
                } else if (lowerTourn.contains("19") && (lowerTourn.contains("u19") || lowerTourn.contains("u-19") || lowerTourn.contains("до 19") || lowerTourn.contains("19 лет") || lowerTourn.contains("19-"))) {
                    detectedAge = "U19";
                } else if (lowerTourn.contains("17") && (lowerTourn.contains("u17") || lowerTourn.contains("u-17") || lowerTourn.contains("до 17") || lowerTourn.contains("17 лет") || lowerTourn.contains("17-"))) {
                    detectedAge = "U17";
                } else if (lowerTourn.contains("16") && (lowerTourn.contains("u16") || lowerTourn.contains("u-16") || lowerTourn.contains("до 16") || lowerTourn.contains("16 лет") || lowerTourn.contains("16-"))) {
                    detectedAge = "U16";
                } else if (lowerTourn.contains("молодеж") || lowerTourn.contains("юнош") || lowerTourn.contains("youth") || lowerTourn.contains("primavera")) {
                    detectedAge = "U21";
                } else if (lowerTourn.contains("резерв") || lowerTourn.contains("reserve")) {
                    detectedAge = "RESERVE";
                }
            }

            List<MdmTournament> candidateTourns = null;
            if (resolvedCountryId != null) {
                candidateTourns = tournamentRepository.findBySportIdAndCountryId(sportId, resolvedCountryId);
            } else if (countryCtx != null) {
                final String targetCountry = countryCtx;
                candidateTourns = tournamentRepository.findBySportId(sportId).stream()
                        .filter(t -> targetCountry.equalsIgnoreCase(t.getCountryName()) || (t.getCategory() != null && targetCountry.equalsIgnoreCase(t.getCategory().getSlug())))
                        .toList();
            } else if (!isAmbiguousLeagueName(tournName)) {
                candidateTourns = tournamentRepository.findBySportId(sportId);
            }

            if (candidateTourns != null && !candidateTourns.isEmpty()) {
                int tournLen = tournName != null ? tournName.length() : 0;
                boolean pruneCandidates = candidateTourns.size() > 100;
                for (MdmTournament cand : candidateTourns) {
                    if (cand.getCanonicalName() == null) continue;
                    if (pruneCandidates && Math.abs(cand.getCanonicalName().length() - tournLen) > 6) {
                        continue;
                    }
                    boolean genderMatch = cand.getGender() == null || cand.getGender().equalsIgnoreCase(detectedGender);
                    boolean ageMatch = isAgeCompatible(cand.getAgeCategory(), detectedAge);
                    if (!genderMatch || !ageMatch) continue;

                    if (SimilarityUtils.areTournamentsSimilar(cand.getCanonicalName(), tournName)) {
                        log.info("🎯 [MDM] Fuzzy matched incoming tournament '{}' to existing canonical tournament #{} '{}'",
                                tournName, cand.getId(), cand.getCanonicalName());
                        if (sofaId != null && cand.getSofascoreId() == null) {
                            cand.setSofascoreId(sofaId);
                            cand = tournamentRepository.save(cand);
                        }
                        enrichTournamentCountry(cand, countryCtx, resolvedCountry);
                        saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), cand);
                        return toTournamentDto(cand);
                    }
                }
            }
        }

        // 6. Auto-creation of Canonical Tournament
        if (tournName == null || tournName.isBlank()) {
            throw new IllegalArgumentException("Cannot resolve or create tournament without tournamentName or sofascoreTournamentId");
        }

        MdmSport sport = findSportByIdCached(sportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found with ID: " + sportId));

        boolean isAmbiguous = isAmbiguousLeagueName(tournName);
        String canonicalName = (isAmbiguous && countryCtx != null) ? (countryCtx + ". " + tournName) : tournName;
        String slug = toSlug(canonicalName) + (sofaId != null ? "-" + sofaId : "");

        Optional<MdmTournament> existingTourn = tournamentRepository.findFirstBySportIdAndSlug(sportId, slug);
        if (existingTourn.isPresent()) {
            MdmTournament t = existingTourn.get();
            if (sofaId != null && t.getSofascoreId() == null) {
                t.setSofascoreId(sofaId);
                t = tournamentRepository.save(t);
            }
            enrichTournamentCountry(t, countryCtx);
            saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
            return toTournamentDto(t);
        }

        String origin = sanitizeEntityOrigin(request.getSourceOrigin(), sofaId);

        String lowerTourn = tournName.toLowerCase();
        String detectedGender = request.getGender() != null ? request.getGender().toUpperCase() : "MALE";
        if (request.getGender() == null) {
            if (lowerTourn.contains("женщин") || lowerTourn.contains("women") || lowerTourn.contains("female") || lowerTourn.contains("damas") || lowerTourn.contains("dames") || lowerTourn.contains("frauen")) {
                detectedGender = "FEMALE";
            }
        }

        String detectedAge = request.getAgeCategory() != null ? request.getAgeCategory().toUpperCase() : "SENIOR";
        if (request.getAgeCategory() == null) {
            if (lowerTourn.contains("23") && (lowerTourn.contains("u23") || lowerTourn.contains("u-23") || lowerTourn.contains("до 23") || lowerTourn.contains("23 лет") || lowerTourn.contains("23-"))) {
                detectedAge = "U23";
            } else if (lowerTourn.contains("21") && (lowerTourn.contains("u21") || lowerTourn.contains("u-21") || lowerTourn.contains("до 21") || lowerTourn.contains("21 лет") || lowerTourn.contains("21-"))) {
                detectedAge = "U21";
            } else if (lowerTourn.contains("20") && (lowerTourn.contains("u20") || lowerTourn.contains("u-20") || lowerTourn.contains("до 20") || lowerTourn.contains("20 лет") || lowerTourn.contains("20-"))) {
                detectedAge = "U20";
            } else if (lowerTourn.contains("19") && (lowerTourn.contains("u19") || lowerTourn.contains("u-19") || lowerTourn.contains("до 19") || lowerTourn.contains("19 лет") || lowerTourn.contains("19-"))) {
                detectedAge = "U19";
            } else if (lowerTourn.contains("17") && (lowerTourn.contains("u17") || lowerTourn.contains("u-17") || lowerTourn.contains("до 17") || lowerTourn.contains("17 лет") || lowerTourn.contains("17-"))) {
                detectedAge = "U17";
            } else if (lowerTourn.contains("16") && (lowerTourn.contains("u16") || lowerTourn.contains("u-16") || lowerTourn.contains("до 16") || lowerTourn.contains("16 лет") || lowerTourn.contains("16-"))) {
                detectedAge = "U16";
            } else if (lowerTourn.contains("молодеж") || lowerTourn.contains("юнош") || lowerTourn.contains("youth") || lowerTourn.contains("primavera")) {
                detectedAge = "U21";
            } else if (lowerTourn.contains("резерв") || lowerTourn.contains("reserve")) {
                detectedAge = "RESERVE";
            }
        }

        boolean isCup = Boolean.TRUE.equals(request.getIsCup())
                || lowerTourn.contains("cup") || lowerTourn.contains("кубок") || lowerTourn.contains("trophy") || lowerTourn.contains("copa") || lowerTourn.contains("pokal");

        if (resolvedCountry == null) {
            resolvedCountry = resolveCountryEntity(countryCtx, isIntl);
        }

        MdmTournament newTourn = MdmTournament.builder()
                .sport(sport)
                .category(category)
                .canonicalName(canonicalName)
                .slug(slug)
                .sofascoreId(sofaId)
                .gender(detectedGender)
                .ageCategory(detectedAge)
                .isCup(isCup)
                .countryName(countryCtx)
                .country(resolvedCountry)
                .sourceOrigin(origin)
                .popularityRank(500)
                .build();
        newTourn = tournamentRepository.save(newTourn);
        log.info("🌟 [MDM] Auto-created canonical tournament #{} '{}' (sport={}, country={}, sofaId={}) from {}",
                newTourn.getId(), newTourn.getCanonicalName(), sportId, countryCtx, sofaId, origin);

        saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), newTourn);
        return toTournamentDto(newTourn);
    }

    /**
     * Dedicated method to find an existing canonical team or create a new one.
     * Takes country/category, age category, gender, and tournament roster context into account.
     */
    @Transactional
    public CanonicalTeamDto findOrCreateTeam(TeamResolutionRequest request) {
        if (request == null) throw new IllegalArgumentException("TeamResolutionRequest cannot be null");
        String cacheKey = buildTeamResolutionCacheKey(request);
        if (cacheKey != null) {
            CanonicalTeamDto cached = teamResolutionCache.getIfPresent(cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        CanonicalTeamDto result = doFindOrCreateTeam(request);
        if (cacheKey != null && result != null) {
            teamResolutionCache.put(cacheKey, result);
        }
        return result;
    }

    private String buildTeamResolutionCacheKey(TeamResolutionRequest request) {
        if (request == null) return null;
        if (request.getSofascoreTeamId() != null) {
            return "sofa:" + request.getSofascoreTeamId();
        }
        String t = request.getTeamName() != null ? request.getTeamName().trim().toLowerCase() : "";
        if (t.isBlank()) return null;
        String src = request.getCreatedBySource() != null ? request.getCreatedBySource() : "src";
        Integer sport = request.getSportId();
        Integer country = request.getCountryId();
        Long tournId = request.getTournamentId();
        return src + ":" + sport + ":" + (country != null ? country : 0) + ":" + (tournId != null ? tournId : 0) + ":" + t;
    }

    private CanonicalTeamDto doFindOrCreateTeam(TeamResolutionRequest request) {
        if (request == null) throw new IllegalArgumentException("TeamResolutionRequest cannot be null");
        if (request.getSportId() == null && request.getSportSlug() != null) {
            findSportBySlugCached(request.getSportSlug())
                    .ifPresent(s -> request.setSportId(s.getId()));
        }
        Integer sportId = request.getSportId();
        if (sportId == null) throw new IllegalArgumentException("Sport ID is mandatory");

        Long sofaId = request.getSofascoreTeamId();
        String teamName = request.getTeamName();
        if (teamName != null) teamName = teamName.trim();

        // 1. SofaScore ID lookup
        if (sofaId != null) {
            Optional<MdmTeam> bySofa = teamRepository.findBySofascoreId(sofaId);
            if (bySofa.isPresent()) {
                MdmTeam team = bySofa.get();
                enrichTeamCountry(team, request.getCountryName());
                return toTeamDto(team);
            }
        }

        // 2. Load context tournament if provided
        MdmTournament contextTournament = null;
        if (request.getTournamentId() != null) {
            contextTournament = tournamentRepository.findById(request.getTournamentId()).orElse(null);
        }
        String tournamentContext = contextTournament != null ? contextTournament.getCanonicalName() : request.getTournamentName();

        // 3. Resolve Country & Category
        MdmCountry resolvedTeamCountry = null;
        if (request.getCountryId() != null && countryRepository != null) {
            resolvedTeamCountry = countryRepository.findById(request.getCountryId()).orElse(null);
        }
        if (resolvedTeamCountry == null && contextTournament != null && contextTournament.getCountry() != null) {
            resolvedTeamCountry = contextTournament.getCountry();
        }
        String countryName = request.getCountryName();
        if (countryName == null && resolvedTeamCountry != null) {
            countryName = resolvedTeamCountry.getCanonicalName();
        } else if (countryName == null && contextTournament != null && contextTournament.getCountryName() != null) {
            countryName = contextTournament.getCountryName();
        }
        if (resolvedTeamCountry == null) {
            resolvedTeamCountry = resolveCountryEntity(
                    request.getCreatedBySource(),
                    request.getRawCategoryKey(),
                    countryName,
                    request.getCategoryName(),
                    false
            );
        }
        Integer teamCountryId = resolvedTeamCountry != null ? resolvedTeamCountry.getId() : null;
        if (countryName == null && resolvedTeamCountry != null) {
            countryName = resolvedTeamCountry.getCanonicalName();
        }
        MdmCategory category = null;
        if (countryName != null || request.getCategoryName() != null) {
            category = resolveCategory(sportId, countryName, request.getCategoryName(), false);
        }
        if (category == null && contextTournament != null && contextTournament.getCategory() != null) {
            category = contextTournament.getCategory();
        }

        // Parse signature and determine expected age & gender
        var parsed = teamName != null ? TeamSignatureNormalizer.parse(teamName, tournamentContext, null) : null;
        String expectedGender = request.getGender() != null ? request.getGender().toUpperCase()
                : (parsed != null ? parsed.gender().name() : "MALE");
        String expectedAge = request.getAgeCategory() != null ? request.getAgeCategory().toUpperCase()
                : ((parsed != null && parsed.ageCategory() != TeamSignatureNormalizer.AgeCategory.MAIN)
                    ? parsed.ageCategory().name() : "SENIOR");

        if (contextTournament != null) {
            if (contextTournament.getGender() != null && !"MIXED".equalsIgnoreCase(contextTournament.getGender()) && request.getGender() == null) {
                expectedGender = contextTournament.getGender().toUpperCase();
            }
            if (contextTournament.getAgeCategory() != null && !"SENIOR".equalsIgnoreCase(contextTournament.getAgeCategory()) && request.getAgeCategory() == null) {
                expectedAge = contextTournament.getAgeCategory().toUpperCase();
            }
        }

        // 4. Ingestion map check (by sourceId, rawKey, or teamName)
        if (request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank()
                && externalSystemRepository != null && teamIngestionMapRepository != null) {
            Optional<MdmExternalSystem> sysOpt = findExternalSystemByCodeCached(request.getCreatedBySource());
            if (sysOpt.isPresent()) {
                Integer sysId = sysOpt.get().getId();

                // 4a. Check by sourceId if provided
                if (request.getSourceId() != null && !request.getSourceId().isBlank()) {
                    Optional<MdmTeamIngestionMap> mapBySource = teamIngestionMapRepository.findFirstByExternalSystemIdAndSourceId(sysId, request.getSourceId().trim());
                    if (mapBySource.isPresent() && mapBySource.get().getCanonicalTeam() != null) {
                        MdmTeam mappedTeam = mapBySource.get().getCanonicalTeam();
                        if (Objects.equals(mappedTeam.getSport().getId(), sportId)) {
                            boolean genderMatch = mappedTeam.getGender() == null || mappedTeam.getGender().equalsIgnoreCase(expectedGender);
                            boolean ageMatch = isAgeCompatible(mappedTeam.getAgeCategory(), expectedAge);
                            boolean isRawDoubles = (teamName != null && teamName.contains("/")) || (request.getRawKey() != null && request.getRawKey().contains("/"));
                            boolean isMappedDoubles = mappedTeam.getCanonicalName().contains("/");
                            boolean doublesMatch = isRawDoubles == isMappedDoubles;
                            if (genderMatch && ageMatch && doublesMatch) {
                                if (contextTournament != null) registerTournamentParticipant(contextTournament, mappedTeam);
                                enrichTeamCountry(mappedTeam, countryName);
                                return finalizeTeamResolution(mappedTeam, sofaId);
                            } else {
                                log.warn("⚠️ [MDM] Rejected source-mapped team #{} '{}' (gender={}, age={}, doubles={}) because expected is (gender={}, age={}, doubles={})",
                                        mappedTeam.getId(), mappedTeam.getCanonicalName(), mappedTeam.getGender(), mappedTeam.getAgeCategory(), isMappedDoubles, expectedGender, expectedAge, isRawDoubles);
                            }
                        }
                    }
                }

                // 4b. Check by rawKey if provided
                if (request.getRawKey() != null && !request.getRawKey().isBlank()) {
                    Optional<MdmTeamIngestionMap> mapByRaw = teamIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, request.getRawKey().trim());
                    if (mapByRaw.isPresent() && mapByRaw.get().getCanonicalTeam() != null) {
                        MdmTeam mappedTeam = mapByRaw.get().getCanonicalTeam();
                        if (Objects.equals(mappedTeam.getSport().getId(), sportId)) {
                            boolean genderMatch = mappedTeam.getGender() == null || mappedTeam.getGender().equalsIgnoreCase(expectedGender);
                            boolean ageMatch = isAgeCompatible(mappedTeam.getAgeCategory(), expectedAge);
                            boolean isRawDoubles = request.getRawKey().contains("/") || (teamName != null && teamName.contains("/"));
                            boolean isMappedDoubles = mappedTeam.getCanonicalName().contains("/");
                            boolean doublesMatch = isRawDoubles == isMappedDoubles;
                            if (genderMatch && ageMatch && doublesMatch) {
                                if (contextTournament != null) registerTournamentParticipant(contextTournament, mappedTeam);
                                enrichTeamCountry(mappedTeam, countryName);
                                return finalizeTeamResolution(mappedTeam, sofaId);
                            } else {
                                log.warn("⚠️ [MDM] Rejected rawKey-mapped team #{} '{}' (gender={}, age={}, doubles={}) because expected is (gender={}, age={}, doubles={})",
                                        mappedTeam.getId(), mappedTeam.getCanonicalName(), mappedTeam.getGender(), mappedTeam.getAgeCategory(), isMappedDoubles, expectedGender, expectedAge, isRawDoubles);
                            }
                        }
                    }
                }

                if (teamName != null) {
                    Optional<MdmTeamIngestionMap> mapOpt = teamIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, teamName);
                    if (mapOpt.isPresent() && mapOpt.get().getCanonicalTeam() != null) {
                        MdmTeam mappedTeam = mapOpt.get().getCanonicalTeam();
                        if (Objects.equals(mappedTeam.getSport().getId(), sportId)) {
                            boolean genderMatch = mappedTeam.getGender() == null || mappedTeam.getGender().equalsIgnoreCase(expectedGender);
                            boolean ageMatch = isAgeCompatible(mappedTeam.getAgeCategory(), expectedAge);
                            boolean isRawDoubles = teamName.contains("/");
                            boolean isMappedDoubles = mappedTeam.getCanonicalName().contains("/");
                            boolean doublesMatch = isRawDoubles == isMappedDoubles;
                            if (genderMatch && ageMatch && doublesMatch) {
                                if (contextTournament != null) registerTournamentParticipant(contextTournament, mappedTeam);
                                enrichTeamCountry(mappedTeam, countryName);
                                return finalizeTeamResolution(mappedTeam, sofaId);
                            } else {
                                log.warn("⚠️ [MDM] Rejected mapped team #{} '{}' (gender={}, age={}, doubles={}) because expected is (gender={}, age={}, doubles={})",
                                        mappedTeam.getId(), mappedTeam.getCanonicalName(), mappedTeam.getGender(), mappedTeam.getAgeCategory(), isMappedDoubles, expectedGender, expectedAge, isRawDoubles);
                            }
                        }
                    }
                }
            }
        }

        // 5. Check Tournament Roster first: Teams playing in this league are prime candidates
        if (contextTournament != null && teamName != null && tournamentTeamRepository != null) {
            List<MdmTeam> rosterTeams = tournamentTeamRepository.findActiveTeamsByTournamentId(contextTournament.getId());
            if (rosterTeams != null && !rosterTeams.isEmpty()) {
                String rawClean = toSlug(teamName)
                        .replaceAll("(?i)\\b(u\\d+|reserve|women|fc|sc|cd|cf|sad|19\\d\\d|20\\d\\d)\\b", "")
                        .replaceAll("[^a-z]", "");

                for (MdmTeam candidate : rosterTeams) {
                    if (!Objects.equals(candidate.getSport().getId(), sportId)) continue;
                    boolean ageOk = isAgeCompatible(candidate.getAgeCategory(), expectedAge);
                    boolean genderOk = candidate.getGender() == null || candidate.getGender().equalsIgnoreCase(expectedGender);
                    if (!ageOk || !genderOk) continue;
                    boolean isRawDoubles = teamName.contains("/");
                    boolean isCandDoubles = candidate.getCanonicalName().contains("/");
                    if (isRawDoubles != isCandDoubles) continue;
                    if (contextTournament.getCountry() != null && candidate.getCountry() != null
                            && !Boolean.TRUE.equals(contextTournament.getIsCup())
                            && !isInternationalTournament(contextTournament)
                            && !isCountryCompatible(contextTournament, candidate)) {
                        continue;
                    }

                    boolean match = SimilarityUtils.areTeamsSimilar(
                            teamName, candidate.getCanonicalName(),
                            tournamentContext, contextTournament.getCanonicalName()
                    );
                    if (!match && !rawClean.isBlank()) {
                        String candClean = toSlug(candidate.getCanonicalName())
                                .replaceAll("(?i)\\b(u\\d+|reserve|women|fc|sc|cd|cf|sad|19\\d\\d|20\\d\\d)\\b", "")
                                .replaceAll("[^a-z]", "");
                        if (!candClean.isBlank()) {
                            boolean isRawGeneric = SimilarityUtils.GENERIC_CLUB_WORDS.contains(rawClean);
                            boolean isCandGeneric = SimilarityUtils.GENERIC_CLUB_WORDS.contains(candClean);
                            if (!isRawGeneric && !isCandGeneric) {
                                match = candClean.equals(rawClean) || (rawClean.length() >= 6 && candClean.contains(rawClean)) || (candClean.length() >= 6 && rawClean.contains(candClean));
                                if (!match) {
                                    String candPhonetic = candClean.replace('s', 'z');
                                    String rawPhonetic = rawClean.replace('s', 'z');
                                    match = candPhonetic.equals(rawPhonetic) || (rawPhonetic.length() >= 6 && candPhonetic.contains(rawPhonetic)) || (candPhonetic.length() >= 6 && rawPhonetic.contains(candPhonetic));
                                }
                            } else {
                                match = candClean.equals(rawClean);
                            }
                        }
                    }

                    if (match) {
                        log.info("🎯 [MDM] Resolved team '{}' to tournament participant #{} '{}' in tournament #{} '{}'",
                                teamName, candidate.getId(), candidate.getCanonicalName(),
                                contextTournament.getId(), contextTournament.getCanonicalName());
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, candidate);
                        enrichTeamCountry(candidate, countryName);
                        return finalizeTeamResolution(candidate, sofaId);
                    }
                }
            }
        }

        // 6. Check translation table (e.g. Russian team translations)
        if (teamName != null && teamTranslationRepository != null) {
            Optional<MdmTeamTranslation> trans = teamTranslationRepository.findFirstByNameIgnoreCase(teamName);
            if (trans.isPresent() && trans.get().getTeam() != null && Objects.equals(trans.get().getTeam().getSport().getId(), sportId)) {
                MdmTeam team = trans.get().getTeam();
                boolean genderMatch = team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender);
                boolean ageMatch = isAgeCompatible(team.getAgeCategory(), expectedAge);
                if (genderMatch && ageMatch) {
                    if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                    saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                    enrichTeamCountry(team, countryName);
                    return finalizeTeamResolution(team, sofaId);
                }
            }
        }

        // 7. Country-scoped Team Lookup by countryId (avoids cross-country collisions)
        if (teamName != null) {
            if (teamCountryId != null) {
                Optional<MdmTeam> byCountryAndName = teamRepository.findFirstBySportIdAndCountryIdAndCanonicalNameIgnoreCase(
                        sportId, teamCountryId, teamName);
                if (byCountryAndName.isPresent()) {
                    MdmTeam team = byCountryAndName.get();
                    if (isAgeCompatible(team.getAgeCategory(), expectedAge) && (team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender))) {
                        if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                        enrichTeamCountry(team, countryName, resolvedTeamCountry);
                        return finalizeTeamResolution(team, sofaId);
                    }
                }
                String teamSlug = toSlug(teamName);
                Optional<MdmTeam> byCountrySlug = teamRepository.findFirstBySportIdAndCountryIdAndSlug(
                        sportId, teamCountryId, teamSlug);
                if (byCountrySlug.isPresent()) {
                    MdmTeam team = byCountrySlug.get();
                    if (isAgeCompatible(team.getAgeCategory(), expectedAge) && (team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender))) {
                        if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                        enrichTeamCountry(team, countryName, resolvedTeamCountry);
                        return finalizeTeamResolution(team, sofaId);
                    }
                }
            }

            // 8. Category-scoped Team Lookup
            if (category != null) {
                Optional<MdmTeam> byCatAndName = teamRepository.findFirstBySportIdAndCategoryIdAndCanonicalNameIgnoreCase(
                        sportId, category.getId(), teamName);
                if (byCatAndName.isPresent()) {
                    MdmTeam team = byCatAndName.get();
                    if (isAgeCompatible(team.getAgeCategory(), expectedAge) && (team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender))) {
                        if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                        enrichTeamCountry(team, countryName, resolvedTeamCountry);
                        return finalizeTeamResolution(team, sofaId);
                    }
                }
                String teamSlug = toSlug(teamName);
                Optional<MdmTeam> byCatAndSlug = teamRepository.findFirstBySportIdAndCategoryIdAndSlug(
                        sportId, category.getId(), teamSlug);
                if (byCatAndSlug.isPresent()) {
                    MdmTeam team = byCatAndSlug.get();
                    if (isAgeCompatible(team.getAgeCategory(), expectedAge) && (team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender))) {
                        if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                        enrichTeamCountry(team, countryName, resolvedTeamCountry);
                        return finalizeTeamResolution(team, sofaId);
                    }
                }
            }

            // 8. Country-scoped lookup by countryName
            if (countryName != null && !countryName.isBlank()) {
                Optional<MdmTeam> byCountry = teamRepository.findFirstBySportIdAndNameAndCountry(sportId, teamName, countryName);
                if (byCountry.isPresent()) {
                    MdmTeam team = byCountry.get();
                    if (isAgeCompatible(team.getAgeCategory(), expectedAge) && (team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender))) {
                        if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                        return finalizeTeamResolution(team, sofaId);
                    }
                }
            }

            // 9. Global Slug & Normalized Signature Lookup
            String teamSlug = toSlug(teamName);
            Optional<MdmTeam> bySlug = teamRepository.findBySportIdAndSlug(sportId, teamSlug);
            if (bySlug.isPresent()) {
                MdmTeam team = bySlug.get();
                boolean genderMatch = team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender);
                boolean ageMatch = isAgeCompatible(team.getAgeCategory(), expectedAge);
                boolean doublesMatch = team.getCanonicalName().contains("/") == teamName.contains("/");
                if (genderMatch && ageMatch && doublesMatch) {
                    if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                    saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                    enrichTeamCountry(team, countryName);
                    if (team.getCategory() == null && category != null) {
                        team.setCategory(category);
                        team = teamRepository.save(team);
                    }
                    return finalizeTeamResolution(team, sofaId);
                }
            }

            String normSig = toSlug(parsed != null ? parsed.coreClubName() : teamName);
            Optional<MdmTeam> bySig = teamRepository.findFirstBySportIdAndNormalizedSignatureAndAgeCategoryAndGender(
                    sportId, normSig, expectedAge, expectedGender);
            if (bySig.isPresent()) {
                MdmTeam team = bySig.get();
                boolean doublesMatch = team.getCanonicalName().contains("/") == teamName.contains("/");
                if (doublesMatch) {
                    if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                    saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                    enrichTeamCountry(team, countryName);
                    if (team.getCategory() == null && category != null) {
                        team.setCategory(category);
                        team = teamRepository.save(team);
                    }
                    return finalizeTeamResolution(team, sofaId);
                }
            }

            if (!normSig.equals(teamSlug)) {
                Optional<MdmTeam> byCoreSlug = teamRepository.findBySportIdAndSlug(sportId, normSig);
                if (byCoreSlug.isPresent()) {
                    MdmTeam team = byCoreSlug.get();
                    boolean genderMatch = team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender);
                    boolean ageMatch = isAgeCompatible(team.getAgeCategory(), expectedAge);
                    boolean doublesMatch = team.getCanonicalName().contains("/") == teamName.contains("/");
                    if (genderMatch && ageMatch && doublesMatch) {
                        if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                        enrichTeamCountry(team, countryName);
                        if (team.getCategory() == null && category != null) {
                            team.setCategory(category);
                            team = teamRepository.save(team);
                        }
                        return finalizeTeamResolution(team, sofaId);
                    }
                }
            }
        }

        // 9.5. Country/Category-scoped Fuzzy Team Resolution (Prevents duplicate creation for unmapped spellings)
        if (teamName != null && !CyberMatchDetector.isCyberParticipant(teamName) && teamName.length() <= 60 && !teamName.matches(".*\\(\\d{5,}\\).*")) {
            List<MdmTeam> candidates = Collections.emptyList();
            if (teamCountryId != null) {
                candidates = teamRepository.findBySportIdAndCountryId(sportId, teamCountryId);
            } else if (category != null) {
                candidates = teamRepository.findBySportIdAndCategoryId(sportId, category.getId());
            }

            if (candidates != null && !candidates.isEmpty()) {
                if (candidates.size() > 100) {
                    final int nameLen = teamName.length();
                    candidates = candidates.stream()
                            .filter(c -> Math.abs(c.getCanonicalName().length() - nameLen) <= 6)
                            .toList();
                }
                for (MdmTeam cand : candidates) {
                    boolean genderMatch = cand.getGender() == null || cand.getGender().equalsIgnoreCase(expectedGender);
                    boolean ageMatch = isAgeCompatible(cand.getAgeCategory(), expectedAge);
                    if (!genderMatch || !ageMatch) continue;
                    boolean isRawDoubles = teamName.contains("/");
                    boolean isCandDoubles = cand.getCanonicalName().contains("/");
                    if (isRawDoubles != isCandDoubles) continue;

                    if (SimilarityUtils.areTeamsSimilar(teamName, cand.getCanonicalName(), tournamentContext, contextTournament != null ? contextTournament.getCanonicalName() : null)) {
                        log.info("🎯 [MDM-COUNTRY-FUZZY] Matched incoming team '{}' to existing canonical team #{} '{}' (sport={}, countryId={})",
                                teamName, cand.getId(), cand.getCanonicalName(), sportId, teamCountryId);
                        if (contextTournament != null) registerTournamentParticipant(contextTournament, cand);
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, cand);
                        enrichTeamCountry(cand, countryName, resolvedTeamCountry);
                        return finalizeTeamResolution(cand, sofaId);
                    }
                }
            }
        }

        // 10. Auto-creation of Canonical Team
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Cannot resolve or create team without teamName or sofascoreTeamId");
        }

        MdmSport sport = findSportByIdCached(sportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found with ID: " + sportId));

        String normSig = toSlug(parsed != null ? parsed.coreClubName() : teamName);
        String slug = toSlug(teamName) + (sofaId != null ? "-" + sofaId : "");
        Optional<MdmTeam> existingSlug = teamRepository.findBySportIdAndSlug(sportId, slug);
        if (existingSlug.isPresent()) {
            MdmTeam team = existingSlug.get();
            if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
            saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
            enrichTeamCountry(team, countryName);
            return finalizeTeamResolution(team, sofaId);
        }

        String origin = sanitizeEntityOrigin(request.getSourceOrigin(), sofaId);

        String effectiveCountry = countryName != null ? countryName : (category != null ? category.getSlug() : null);
        if (resolvedTeamCountry == null && effectiveCountry != null) {
            resolvedTeamCountry = resolveCountryEntity(effectiveCountry, false);
        }

        MdmTeam newTeam = MdmTeam.builder()
                .sport(sport)
                .category(category)
                .canonicalName(teamName)
                .slug(slug)
                .sofascoreId(sofaId)
                .normalizedSignature(normSig)
                .gender(expectedGender)
                .ageCategory(expectedAge)
                .countryName(effectiveCountry)
                .country(resolvedTeamCountry)
                .sourceOrigin(origin)
                .build();
        newTeam = teamRepository.save(newTeam);
        log.info("🌟 [MDM] Auto-created canonical team #{} '{}' (sport={}, country={}, category={}, sofaId={}) from {}",
                newTeam.getId(), newTeam.getCanonicalName(), sportId, countryName, category != null ? category.getSlug() : "none", sofaId, origin);

        if (contextTournament != null) {
            registerTournamentParticipant(contextTournament, newTeam);
        }
        saveTeamIngestionMap(request.getCreatedBySource(), teamName, newTeam);
        return toTeamDto(newTeam);
    }

    @Transactional(readOnly = true)
    public Optional<CanonicalTournamentDto> getTournamentById(Long id) {
        return tournamentRepository.findById(id).map(MdmMatchResolutionService::toTournamentDto);
    }

    @Transactional(readOnly = true)
    public Optional<CanonicalTeamDto> getTeamById(Long id) {
        return teamRepository.findById(id).map(MdmMatchResolutionService::toTeamDto);
    }

    public static CanonicalTournamentDto toTournamentDto(MdmTournament t) {
        if (t == null) return null;
        return CanonicalTournamentDto.builder()
                .tournamentId(t.getId())
                .sportId(t.getSport().getId())
                .sportSlug(t.getSport().getSlug())
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .categorySlug(t.getCategory() != null ? t.getCategory().getSlug() : null)
                .countryId(t.getCountry() != null ? t.getCountry().getId() : null)
                .countryName(t.getCountryName())
                .canonicalName(t.getCanonicalName())
                .slug(t.getSlug())
                .ageCategory(t.getAgeCategory())
                .gender(t.getGender())
                .isCup(t.getIsCup())
                .isInternational(t.getCategory() != null && "international".equalsIgnoreCase(t.getCategory().getSlug()))
                .sofascoreId(t.getSofascoreId())
                .sourceOrigin(t.getSourceOrigin())
                .build();
    }

    public static CanonicalTeamDto toTeamDto(MdmTeam team) {
        if (team == null) return null;
        return CanonicalTeamDto.builder()
                .teamId(team.getId())
                .sportId(team.getSport().getId())
                .sportSlug(team.getSport().getSlug())
                .categoryId(team.getCategory() != null ? team.getCategory().getId() : null)
                .categorySlug(team.getCategory() != null ? team.getCategory().getSlug() : null)
                .countryId(team.getCountry() != null ? team.getCountry().getId() : null)
                .countryName(team.getCountryName())
                .canonicalName(team.getCanonicalName())
                .slug(team.getSlug())
                .ageCategory(team.getAgeCategory())
                .gender(team.getGender())
                .sofascoreId(team.getSofascoreId())
                .sourceOrigin(team.getSourceOrigin())
                .logoUrl(team.getLogoUrl())
                .logoSource(team.getLogoSource())
                .isOfficialLogo(team.getIsOfficialLogo())
                .build();
    }

    public void registerTournamentParticipant(MdmTournament tournament, MdmTeam team) {
        if (tournament == null || team == null || tournamentTeamRepository == null) return;
        if (tournament.getId() == null || team.getId() == null) return;
        if (tournament.getSport() == null || team.getSport() == null) return;
        if (!java.util.Objects.equals(tournament.getSport().getId(), team.getSport().getId())) {
            log.warn("🚫 [MDM-ROSTER] Cannot register team '{}' (sport={}) into tournament '{}' (sport={}) due to sport mismatch",
                    team.getCanonicalName(), team.getSport().getId(), tournament.getCanonicalName(), tournament.getSport().getId());
            return;
        }
        if (tournament.getCountry() != null && team.getCountry() != null
                && !Boolean.TRUE.equals(tournament.getIsCup())
                && !isInternationalTournament(tournament)
                && !isCountryCompatible(tournament, team)) {
            log.warn("🚫 [MDM-ROSTER] Refusing to register foreign team '{}' (#{}, country={}) into domestic tournament '{}' (#{}, country={})",
                    team.getCanonicalName(), team.getId(), team.getCountry().getCanonicalName(),
                    tournament.getCanonicalName(), tournament.getId(), tournament.getCountry().getCanonicalName());
            return;
        }
        if (!tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), team.getId())) {
            try {
                MdmTournamentTeam participant = MdmTournamentTeam.builder()
                        .tournament(tournament)
                        .team(team)
                        .sport(tournament.getSport())
                        .isActive(true)
                        .build();
                tournamentTeamRepository.save(participant);
                log.info("🛡️ [MDM-ROSTER] Registered team '{}' (#{}) as participant of tournament '{}' (#{})",
                        team.getCanonicalName(), team.getId(), tournament.getCanonicalName(), tournament.getId());
            } catch (Exception e) {
                log.debug("Participant already registered or conflict: {}", e.getMessage());
            }
        }
    }

    private static boolean isAmbiguousLeagueName(String name) {
        if (name == null) return false;
        return AMBIGUOUS_GENERIC_LEAGUES.contains(name.trim().toLowerCase());
    }

    private static String buildIngestionKey(String tournName, String countryCtx) {
        if (countryCtx != null && !countryCtx.isBlank() && isAmbiguousLeagueName(tournName)) {
            return countryCtx + " - " + tournName;
        }
        return tournName;
    }

    public MdmCountry resolveCountryEntity(String systemCode, String rawCategoryKey, String countryName, String categoryName, Boolean isInternational) {
        if (countryRepository == null) return null;
        if (Boolean.TRUE.equals(isInternational)) return null;

        // 1. Check bookmaker's own country ingestion map if systemCode is available
        if (systemCode != null && !systemCode.isBlank() && countryIngestionMapRepository != null && externalSystemRepository != null) {
            Optional<MdmExternalSystem> sysOpt = findExternalSystemByCodeCached(systemCode);
            if (sysOpt.isPresent()) {
                Integer sysId = sysOpt.get().getId();
                if (rawCategoryKey != null && !rawCategoryKey.isBlank()) {
                    Optional<MdmCountryIngestionMap> map = countryIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, rawCategoryKey.trim());
                    if (map.isPresent() && map.get().getCanonicalCountry() != null) {
                        return map.get().getCanonicalCountry();
                    }
                }
                if (countryName != null && !countryName.isBlank()) {
                    Optional<MdmCountryIngestionMap> map = countryIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, countryName.trim());
                    if (map.isPresent() && map.get().getCanonicalCountry() != null) {
                        return map.get().getCanonicalCountry();
                    }
                }
                if (categoryName != null && !categoryName.isBlank()) {
                    Optional<MdmCountryIngestionMap> map = countryIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, categoryName.trim());
                    if (map.isPresent() && map.get().getCanonicalCountry() != null) {
                        return map.get().getCanonicalCountry();
                    }
                }
            }
        }

        // 2. Normalizer & Database lookup
        String target = (countryName != null && !countryName.isBlank()) ? countryName.trim()
                : (categoryName != null && !categoryName.isBlank() ? categoryName.trim() : null);
        if (target == null) return null;

        CountryNormalizer.CountryInfo cInfo = CountryNormalizer.normalize(target, false);
        MdmCountry country = null;
        if (cInfo != null) {
            Optional<MdmCountry> byIso = countryRepository.findFirstByIsoCodeIgnoreCase(cInfo.code());
            if (byIso.isPresent()) {
                country = byIso.get();
            } else {
                Optional<MdmCountry> byName = countryRepository.findFirstByCanonicalNameIgnoreCase(cInfo.slug());
                if (byName.isPresent()) {
                    country = byName.get();
                }
            }
        }
        if (country == null) {
            country = countryRepository.findFirstByCanonicalNameIgnoreCase(target).orElse(null);
        }

        // 3. Auto-cache into mdm_country_ingestion_map
        if (country != null && systemCode != null && !systemCode.isBlank() && countryIngestionMapRepository != null && externalSystemRepository != null) {
            saveCountryIngestionMap(systemCode, rawCategoryKey != null ? rawCategoryKey : target, country);
            if (rawCategoryKey != null && !rawCategoryKey.equalsIgnoreCase(target)) {
                saveCountryIngestionMap(systemCode, target, country);
            }
        }

        return country;
    }

    public MdmCountry resolveCountryEntity(String countryName, Boolean isInternational) {
        return resolveCountryEntity(null, null, countryName, null, isInternational);
    }

    private void enrichTournamentCountry(MdmTournament t, String countryCtx) {
        enrichTournamentCountry(t, countryCtx, null);
    }

    private void enrichTournamentCountry(MdmTournament t, String countryCtx, MdmCountry countryEntity) {
        if (t == null) return;
        boolean updated = false;
        if (countryCtx != null && !countryCtx.isBlank() && t.getCountryName() == null) {
            t.setCountryName(countryCtx);
            updated = true;
        }
        if (t.getCountry() == null) {
            if (countryEntity != null) {
                t.setCountry(countryEntity);
                updated = true;
            } else if (countryCtx != null && !countryCtx.isBlank() && countryRepository != null) {
                MdmCountry c = resolveCountryEntity(countryCtx, false);
                if (c != null) {
                    t.setCountry(c);
                    updated = true;
                }
            }
        }
        if (updated) {
            tournamentRepository.save(t);
        }
    }

    private void enrichTeamCountry(MdmTeam team, String countryCtx) {
        enrichTeamCountry(team, countryCtx, null);
    }

    private void enrichTeamCountry(MdmTeam team, String countryCtx, MdmCountry countryEntity) {
        if (team == null) return;
        boolean updated = false;
        if (countryCtx != null && !countryCtx.isBlank() && team.getCountryName() == null) {
            team.setCountryName(countryCtx);
            updated = true;
        }
        if (team.getCountry() == null) {
            if (countryEntity != null) {
                team.setCountry(countryEntity);
                updated = true;
            } else if (countryCtx != null && !countryCtx.isBlank() && countryRepository != null) {
                MdmCountry c = resolveCountryEntity(countryCtx, false);
                if (c != null) {
                    team.setCountry(c);
                    updated = true;
                }
            }
        }
        if (updated) {
            teamRepository.save(team);
        }
    }

    private MdmTeam resolveCanonicalTeam(MdmTeam team) {
        if (team == null || teamMergeMapRepository == null) return team;
        Long sofaId = remapTeamId(team.getId());
        if (!Objects.equals(sofaId, team.getId())) {
            return teamRepository.findById(sofaId).orElse(team);
        }
        return team;
    }

    private CanonicalTeamDto finalizeTeamResolution(MdmTeam team, Long sofaId) {
        team = resolveCanonicalTeam(team);
        if (team != null && sofaId != null && team.getSofascoreId() == null) {
            enrichTeamSofascoreId(team, sofaId);
        }
        return toTeamDto(team);
    }

    private void enrichTeamSofascoreId(MdmTeam team, Long sofaId) {
        if (team == null || sofaId == null) return;
        if (team.getSofascoreId() != null) return;
        try {
            Optional<MdmTeam> existing = teamRepository.findBySofascoreId(sofaId);
            if (existing.isEmpty()) {
                team.setSofascoreId(sofaId);
                teamRepository.save(team);
                log.info("💎 [MDM] Linked SofaScore ID {} to canonical team #{} '{}'",
                        sofaId, team.getId(), team.getCanonicalName());
            }
        } catch (Exception e) {
            log.warn("⚠️ [MDM] Could not link SofaScore ID {} to team #{}: {}",
                    sofaId, team.getId(), e.getMessage());
        }
    }

    private void saveCountryIngestionMap(String systemCode, String rawKey, MdmCountry country) {
        if (systemCode == null || systemCode.isBlank() || rawKey == null || rawKey.isBlank() || country == null) return;
        if (externalSystemRepository == null || countryIngestionMapRepository == null) return;
        try {
            findExternalSystemByCodeCached(systemCode).ifPresent(sys -> {
                countryIngestionMapRepository.insertIgnoreConflict(sys.getId(), rawKey.trim(), country.getId(), country.getCanonicalName());
            });
        } catch (Exception e) {
            log.debug("Country ingestion map insert ignored: {}", e.getMessage());
        }
    }

    private void saveTeamIngestionMap(String systemCode, String rawKey, MdmTeam team) {
        saveTeamIngestionMap(systemCode, rawKey, null, team);
    }

    private void saveTeamIngestionMap(String systemCode, String rawKey, String sourceId, MdmTeam team) {
        team = resolveCanonicalTeam(team);
        if (systemCode == null || systemCode.isBlank() || rawKey == null || rawKey.isBlank() || team == null) return;
        if (externalSystemRepository == null || teamIngestionMapRepository == null) return;
        try {
            final MdmTeam targetTeam = team;
            // 🛡️ CRITICAL BLUEPRINT GUARD:
            // Do NOT allow a female, youth, or reserve team to claim/overwrite a naked senior male raw_key (e.g. "GKS Katowice")!
            // When a bookmaker sends "GKS Katowice" in a women's league, mapping raw_key='GKS Katowice' -> female team
            // corrupts the global flat map for all male matches.
            boolean hasFemaleMarker = rawKey.matches("(?i).*(\\bwomen\\b|\\(w\\)|\\(жен\\)|-w$).*");
            if ("FEMALE".equalsIgnoreCase(targetTeam.getGender()) && !hasFemaleMarker) {
                log.warn("🛡️ [MDM-GUARD] Refusing to overwrite rawKey '{}' for system '{}' with FEMALE team #{} '{}'. Female teams without explicit female marker in rawKey must not hijack neutral keys!",
                        rawKey, systemCode, targetTeam.getId(), targetTeam.getCanonicalName());
                return;
            }
            boolean hasYouthMarker = rawKey.matches("(?i).*(\\bu\\d+\\b|\\(до \\d+\\)|reserve|youth|-u\\d+$).*");
            if (!"SENIOR".equalsIgnoreCase(targetTeam.getAgeCategory()) && !hasYouthMarker) {
                log.warn("🛡️ [MDM-GUARD] Refusing to overwrite rawKey '{}' for system '{}' with youth/reserve team #{} '{}'. Youth teams without explicit youth marker in rawKey must not hijack neutral keys!",
                        rawKey, systemCode, targetTeam.getId(), targetTeam.getCanonicalName());
                return;
            }

            // 🛡️ CYBER / GAMER HANDLE GUARD:
            // If rawKey contains gamer tags like (shatik666), (zyX), (kairox), (billy_alish), (esports), (cyber), etc.,
            // NEVER map it to a traditional real-world football club (sportId == 1)!
            boolean hasGamerTag = rawKey.matches("(?i).*\\([A-Za-z0-9_]{3,}\\).*");
            if (hasGamerTag && targetTeam.getSport() != null && targetTeam.getSport().getId() == 1) {
                String teamNameLower = targetTeam.getCanonicalName().toLowerCase();
                if (!teamNameLower.contains("cyber") && !teamNameLower.contains("esports") && !teamNameLower.contains("esport")) {
                    log.warn("🛡️ [MDM-GUARD] Refusing to map cyber gamer rawKey '{}' to real football team #{} '{}'",
                            rawKey, systemCode, targetTeam.getId(), targetTeam.getCanonicalName());
                    return;
                }
            }

            // 🛡️ LEXICAL SANITY GUARD:
            // Refuse to map rawKey to targetTeam if there is zero lexical/token correlation!
            // (e.g. "Real San Joaquin" or "Belgrano" must NEVER map to "Be Ready"!)
            if (!isLexicallyCompatible(rawKey, targetTeam.getCanonicalName())) {
                log.warn("🛡️ [MDM-GUARD] Refusing to map disparate rawKey '{}' to team #{} '{}' (zero lexical similarity)",
                        rawKey, systemCode, targetTeam.getId(), targetTeam.getCanonicalName());
                return;
            }

            findExternalSystemByCodeCached(systemCode).ifPresent(sys -> {
                teamIngestionMapRepository.upsertMapping(sys.getId(), rawKey.trim(), sourceId, targetTeam.getId(), targetTeam.getCanonicalName());
            });
        } catch (Exception e) {
            log.debug("Team ingestion map insert ignored: {}", e.getMessage());
        }
    }

    private boolean isLexicallyCompatible(String rawKey, String canonicalName) {
        if (rawKey == null || canonicalName == null) return false;
        // Strict doubles vs singles isolation:
        boolean rawDoubles = rawKey.contains("/");
        boolean canDoubles = canonicalName.contains("/");
        if (rawDoubles != canDoubles) {
            return false;
        }
        String rawNorm = SimilarityUtils.normalizeToLatin(rawKey).toLowerCase();
        String canNorm = SimilarityUtils.normalizeToLatin(canonicalName).toLowerCase();
        if (rawNorm.equals(canNorm)) return true;
        if (rawNorm.contains(canNorm) || canNorm.contains(rawNorm)) return true;
        if (SimilarityUtils.jaroWinklerSimilarity(rawNorm, canNorm) >= 0.75) return true;
        // Check if substantial words overlap
        java.util.Set<String> rawTokens = java.util.Arrays.stream(rawNorm.split("[^a-z0-9]+"))
                .filter(t -> t.length() >= 3 && !SimilarityUtils.GENERIC_CLUB_WORDS.contains(t))
                .collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> canTokens = java.util.Arrays.stream(canNorm.split("[^a-z0-9]+"))
                .filter(t -> t.length() >= 3 && !SimilarityUtils.GENERIC_CLUB_WORDS.contains(t))
                .collect(java.util.stream.Collectors.toSet());
        return !java.util.Collections.disjoint(rawTokens, canTokens);
    }

    private void saveTournamentIngestionMap(String systemCode, String rawKey, MdmTournament tourney) {
        saveTournamentIngestionMap(systemCode, rawKey, null, tourney);
    }

    private void saveTournamentIngestionMap(String systemCode, String rawKey, String sourceId, MdmTournament tourney) {
        if (systemCode == null || systemCode.isBlank() || rawKey == null || rawKey.isBlank() || tourney == null) return;
        if (externalSystemRepository == null || tournamentIngestionMapRepository == null) return;
        try {
            findExternalSystemByCodeCached(systemCode).ifPresent(sys -> {
                tournamentIngestionMapRepository.upsertMapping(sys.getId(), rawKey.trim(), sourceId, tourney.getId(), tourney.getCanonicalName());
            });
        } catch (Exception e) {
            log.debug("Tournament ingestion map insert ignored: {}", e.getMessage());
        }
    }

    public static boolean hasCyrillic(String input) {
        if (input == null || input.isEmpty()) return false;
        for (int i = 0; i < input.length(); i++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(input.charAt(i));
            if (block == Character.UnicodeBlock.CYRILLIC) return true;
        }
        return false;
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) return "unknown";
        String latin = hasCyrillic(input) ? SimilarityUtils.transliterateCyrillic(input) : input;
        String normalized = Normalizer.normalize(latin.isBlank() ? input : latin, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    public static boolean isAgeCompatible(String teamAge, String expectedAge) {
        String a1 = (teamAge == null || teamAge.isBlank()) ? "SENIOR" : teamAge.trim().toUpperCase();
        String a2 = (expectedAge == null || expectedAge.isBlank()) ? "SENIOR" : expectedAge.trim().toUpperCase();
        return Objects.equals(a1, a2);
    }

    public boolean isInternationalTournament(MdmTournament tournament) {
        return integrityValidator.isInternationalTournament(tournament);
    }

    public boolean isCountryCompatible(MdmTournament tournament, MdmTeam team) {
        return integrityValidator.isCountryCompatible(tournament, team);
    }

    private void validateLeagueIntegrity(MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String sourceOrigin, String createdBy) {
        integrityValidator.validateLeagueIntegrity(tournament, homeTeam, awayTeam, sourceOrigin, createdBy);
    }

    public void validateMatchGenderAndAge(MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String createdBy) {
        integrityValidator.validateMatchGenderAndAge(tournament, homeTeam, awayTeam, createdBy);
    }

    public static String normalizeGender(String gender) {
        return MatchIntegrityValidator.normalizeGender(gender);
    }

    public static String normalizeAge(String ageCategory) {
        return MatchIntegrityValidator.normalizeAge(ageCategory);
    }

    public static boolean isGenderCompatible(String g1, String g2) {
        return MatchIntegrityValidator.isGenderCompatible(g1, g2);
    }

    private boolean isSyntheticLeague(MatchResolutionRequest request) {
        return integrityValidator.isSyntheticLeague(request);
    }

    private boolean isMalformedDoubles(MatchResolutionRequest request) {
        return integrityValidator.isMalformedDoubles(request);
    }

    private void dispatchTrueSport(MatchResolutionRequest request) {
        if (request == null) return;
        if (request.getSportId() == null && request.getSportSlug() != null) {
            findSportBySlugCached(request.getSportSlug())
                    .ifPresent(s -> request.setSportId(s.getId()));
        }

        String tName = request.getTournamentName() != null ? request.getTournamentName() : "";
        String tLower = tName.toLowerCase().trim();

        // 1. Cross-Sport Disinfection: Tennis leaked into other sports
        if (request.getSportId() != null && request.getSportId() != 4 && request.getSportId() != 5) {
            if (tLower.contains("atp") || tLower.contains("wta") || tLower.contains("itf")
                    || tLower.contains("ролан гаррос") || tLower.contains("уимблдон")
                    || tLower.contains("australian open") || tLower.contains("us open")
                    || (tLower.contains("челленджер") && !tLower.contains("challenger league"))
                    || tLower.contains("wtt")) {
                if (tLower.contains("wtt") || tLower.contains("настольный") || tLower.contains("table tennis") || tLower.contains("setka cup") || tLower.contains("liga pro")) {
                    request.setSportId(5);
                    request.setSportSlug("table-tennis");
                } else {
                    request.setSportId(4);
                    request.setSportSlug("tennis");
                }
                log.warn("🛡️ [MDM Disinfection] Re-routed tournament '{}' from corrupted sport to {}", tName, request.getSportSlug());
            }
        }

        // 2. Cross-Sport Disinfection: Ice Hockey leaked into other sports
        if (request.getSportId() != null && request.getSportId() != 2 && request.getSportId() != 30 && request.getSportId() != 42) {
            if (tLower.contains("nhl") || tLower.contains("нхл") || tLower.contains("кхл") || tLower.contains("вхл") || tLower.contains("мхл")) {
                if (tLower.contains("кибер") || tLower.contains("cyber") || tLower.contains("3x4") || tLower.contains("4 минуты")) {
                    request.setSportId(42);
                    request.setSportSlug("cyber-hockey");
                } else if (tLower.contains("3x3") || tLower.contains("3х3") || tLower.contains("short") || tLower.contains("3hl") || tLower.contains("rhl")) {
                    request.setSportId(30);
                    request.setSportSlug("short-hockey");
                } else {
                    request.setSportId(2);
                    request.setSportSlug("ice-hockey");
                }
                log.warn("🛡️ [MDM Disinfection] Re-routed tournament '{}' to {}", tName, request.getSportSlug());
            }
        }

        // 3. Cross-Sport Disinfection: Basketball leaked into other sports
        if (request.getSportId() != null && request.getSportId() != 3 && request.getSportId() != 29 && request.getSportId() != 41) {
            if (tLower.contains("nba") || tLower.contains("нба") || tLower.contains("евролига") || tLower.contains("euroleague")) {
                if (tLower.contains("кибер") || tLower.contains("cyber") || tLower.contains("2k") || tLower.contains("4x5") || tLower.contains("4х5")) {
                    request.setSportId(41);
                    request.setSportSlug("cyber-basketball");
                } else if (tLower.contains("3x3") || tLower.contains("3х3")) {
                    request.setSportId(29);
                    request.setSportSlug("basketball-3x3");
                } else {
                    request.setSportId(3);
                    request.setSportSlug("basketball");
                }
                log.warn("🛡️ [MDM Disinfection] Re-routed tournament '{}' to {}", tName, request.getSportSlug());
            }
        }

        // 4. Esports Slicing: from generic sport_id = 6 into dedicated disciplines
        if (request.getSportId() != null && request.getSportId() == 6) {
            if (tLower.contains("fc ") || tLower.contains("fc.") || tLower.contains("fc 2") || tLower.contains("fifa") || tLower.contains("киберфутбол") || tLower.contains("volta") || tLower.contains("gt sports")) {
                request.setSportId(37);
                request.setSportSlug("cyber-football");
            } else if (tLower.contains("nhl") || tLower.contains("киберхоккей")) {
                request.setSportId(42);
                request.setSportSlug("cyber-hockey");
            } else if (tLower.contains("nba") || tLower.contains("кибербаскетбол") || tLower.contains("2k")) {
                request.setSportId(41);
                request.setSportSlug("cyber-basketball");
            } else if (tLower.contains("cs2") || tLower.contains("counter-strike") || tLower.contains("cct ") || tLower.contains("esea") || tLower.contains("esl challenger") || tLower.contains("blast premier")) {
                request.setSportId(33);
                request.setSportSlug("cs2");
            } else if (tLower.contains("dota") || tLower.contains("dreamleague") || tLower.contains("the international")) {
                request.setSportId(34);
                request.setSportSlug("dota2");
            } else if (tLower.contains("league of legends") || tLower.contains("lck") || tLower.contains("lec") || tLower.contains("lpl") || tLower.contains("lcs") || tLower.contains("emea masters")) {
                request.setSportId(35);
                request.setSportSlug("league-of-legends");
            } else if (tLower.contains("valorant") || tLower.contains("vct")) {
                request.setSportId(36);
                request.setSportSlug("valorant");
            }
        }

        // 5. Basketball 3x3 isolation
        if (request.getSportId() != null && request.getSportId() == 3) {
            if (tLower.contains("3x3") || tLower.contains("3х3")) {
                request.setSportId(29);
                request.setSportSlug("basketball-3x3");
            } else if (tLower.contains("кибер") || tLower.contains("cyber") || tLower.contains("2k") || tLower.contains("4x5")) {
                request.setSportId(41);
                request.setSportSlug("cyber-basketball");
            }
        }

        // 6. Ice Hockey 3x3 isolation
        if (request.getSportId() != null && request.getSportId() == 2) {
            if (tLower.contains("3x3") || tLower.contains("3х3") || tLower.contains("short") || tLower.contains("3hl") || tLower.contains("rhl") || tLower.contains("шорт")) {
                request.setSportId(30);
                request.setSportSlug("short-hockey");
            } else if (tLower.contains("кибер") || tLower.contains("cyber") || tLower.contains("nhl 2") || tLower.contains("united esports") || tLower.contains("h2h")) {
                request.setSportId(42);
                request.setSportSlug("cyber-hockey");
            }
        }

        // 7. Football Disinfection: Cyber Football & Short Football leaked into football (sport_id == 1)
        if (request.getSportId() != null && request.getSportId() == 1) {
            String hName = request.getHomeTeamName() != null ? request.getHomeTeamName() : "";
            String aName = request.getAwayTeamName() != null ? request.getAwayTeamName() : "";
            boolean hasGamerHandle = hName.matches("(?i).*\\([A-Za-z0-9_]{3,}\\).*") || aName.matches("(?i).*\\([A-Za-z0-9_]{3,}\\).*");

            if (hasGamerHandle || tLower.contains("cyber") || tLower.contains("кибер") || tLower.contains("fifa")
                    || tLower.contains("fc 2") || tLower.contains("fc 24") || tLower.contains("fc 25") || tLower.contains("fc 26")
                    || tLower.contains("esportsbattle") || tLower.contains("esport battle") || tLower.contains("esports battle")
                    || tLower.contains("gt league") || tLower.contains("gt sports") || tLower.contains("live arena")
                    || tLower.contains("volta") || tLower.contains("h2h liga") || tLower.contains("united esports")) {
                request.setSportId(37);
                request.setSportSlug("cyber-football");
                log.warn("🛡️ [MDM Disinfection] Re-routed football match '{}: {} vs {}' to cyber-football (sport_id=37)", tName, hName, aName);
            } else if (tLower.contains("short football") || tLower.contains("шорт-футбол") || tLower.contains("шорт футбол")
                    || tLower.contains("budnesliga") || tLower.contains("subsoccer")
                    || tLower.matches("(?i).*\\b(?:2x2|3x3|4x4|5x5|6x6|7x7|8x8)\\b.*")) {
                request.setSportId(28);
                request.setSportSlug("short-football");
                log.warn("🛡️ [MDM Disinfection] Re-routed football tournament '{}' to short-football (sport_id=28)", tName);
            }

            // 8. Football Statistical Sub-Sports Routing (Decouple stats markets from main 11x11 football)
            String hLower = hName.toLowerCase();
            String aLower = aName.toLowerCase();

            if (hLower.contains("(жк)") || aLower.contains("(жк)") || hLower.contains("(ж.к.)") || aLower.contains("(ж.к.)")
                    || hLower.contains("желтые карт") || aLower.contains("желтые карт") || tLower.contains("желтые карт")
                    || tLower.contains("(жк)") || tLower.contains("bookings") || tLower.contains("yellow cards")) {
                request.setSportId(46);
                request.setSportSlug("football-yellow-cards");
                log.info("🛡️ [MDM Disinfection] Re-routed football match '{}: {} vs {}' to football-yellow-cards (sport_id=46)", tName, hName, aName);
            } else if (hLower.contains("(фолы)") || aLower.contains("(фолы)") || hLower.contains("фолы") || aLower.contains("фолы")
                    || tLower.contains("фолы") || tLower.contains("(фолы)") || tLower.contains("fouls")) {
                request.setSportId(47);
                request.setSportSlug("football-fouls");
                log.info("🛡️ [MDM Disinfection] Re-routed football match '{}: {} vs {}' to football-fouls (sport_id=47)", tName, hName, aName);
            } else if (hLower.contains("(офсайды)") || aLower.contains("(офсайды)") || hLower.contains("офсайды") || aLower.contains("офсайды")
                    || tLower.contains("офсайды") || tLower.contains("(офсайды)") || tLower.contains("offsides")) {
                request.setSportId(48);
                request.setSportSlug("football-offsides");
                log.info("🛡️ [MDM Disinfection] Re-routed football match '{}: {} vs {}' to football-offsides (sport_id=48)", tName, hName, aName);
            } else if (hLower.contains("(удары от ворот)") || aLower.contains("(удары от ворот)") || hLower.contains("удары от ворот") || aLower.contains("удары от ворот")
                    || tLower.contains("удары от ворот") || tLower.contains("(удары от ворот)") || tLower.contains("goal kicks")) {
                request.setSportId(49);
                request.setSportSlug("football-goal-kicks");
                log.info("🛡️ [MDM Disinfection] Re-routed football match '{}: {} vs {}' to football-goal-kicks (sport_id=49)", tName, hName, aName);
            } else if (hLower.contains("(угловые)") || aLower.contains("(угловые)") || hLower.contains("угловые") || aLower.contains("угловые")
                    || tLower.contains("угловые") || tLower.contains("(угловые)") || tLower.contains("corners")) {
                request.setSportId(43);
                request.setSportSlug("football-corners");
                log.info("🛡️ [MDM Disinfection] Re-routed football match '{}: {} vs {}' to football-corners (sport_id=43)", tName, hName, aName);
            }
        }

        // 9. Fantasy Duels, Player vs Player & Specials Disinfection (Separate sport feed)
        String hName = request.getHomeTeamName() != null ? request.getHomeTeamName() : "";
        String aName = request.getAwayTeamName() != null ? request.getAwayTeamName() : "";
        String hLower = hName.toLowerCase();
        String aLower = aName.toLowerCase();

        boolean isPlayerDuel = tLower.contains("кто забьет больше") || tLower.contains("who will score more")
                || tLower.contains("дуэль игроков") || tLower.contains("дуэли игроков")
                || tLower.contains("player duel") || tLower.contains("дуэль") || tLower.contains("дуэли")
                || tLower.contains("специальные ставки") || tLower.contains("special bets")
                || tLower.contains("fantasy") || tLower.contains("фэнтези") || tLower.contains("фантази")
                || tLower.contains("человек против") || tLower.contains("игрок против") || tLower.contains("player vs")
                || tLower.contains("балтboost") || tLower.contains("boost")
                || hLower.contains(" vs ") || aLower.contains(" vs ")
                || hLower.contains("человек против") || aLower.contains("человек против")
                || tLower.contains("kto zabet") || tLower.contains("kto zabyet");

        if (isPlayerDuel) {
            request.setSportId(23);
            request.setSportSlug("specials");
            log.warn("🛡️ [MDM Disinfection] Re-routed fantasy / player duel match '{}: {} vs {}' to specials (sport_id=23)", tName, hName, aName);
        }
    }

    private void validateRequest(MatchResolutionRequest request) {
        integrityValidator.validateRequest(request);
    }

    private boolean isStatsOrOutrightMatch(MatchResolutionRequest request) {
        return integrityValidator.isStatsOrOutrightMatch(request);
    }

    private boolean matchesStatsOrOutright(String name) {
        return integrityValidator.matchesStatsOrOutright(name);
    }

    private boolean isSyntheticPlaceholder(String name) {
        return integrityValidator.isSyntheticPlaceholder(name);
    }

    private MdmMatch findClosestCandidate(List<MdmMatch> candidates, OffsetDateTime targetTime) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        long targetSec = targetTime.toEpochSecond();
        MdmMatch best = candidates.get(0);
        long minDiff = Math.abs(best.getStartTime().toEpochSecond() - targetSec);

        for (int i = 1; i < candidates.size(); i++) {
            MdmMatch current = candidates.get(i);
            long diff = Math.abs(current.getStartTime().toEpochSecond() - targetSec);
            if (diff < minDiff) {
                minDiff = diff;
                best = current;
            }
        }
        return best;
    }

    private CanonicalMatchHeaderDto tryResolveByTemporalMatchAnchor(MatchResolutionRequest request, OffsetDateTime targetTime) {
        Integer sportId = request.getSportId();
        if (sportId == null || targetTime == null) return null;

        OffsetDateTime windowStart = targetTime.minusHours(3);
        OffsetDateTime windowEnd = targetTime.plusHours(3);

        String rawHome = request.getHomeTeamName();
        String rawAway = request.getAwayTeamName();
        String tournContext = request.getTournamentName();

        // Pre-resolve ingestion map team IDs and tournament ID ONCE outside the loop to avoid O(N) DB thrashing
        Long preResolvedHomeId = request.getHomeTeamId();
        Long preResolvedAwayId = request.getAwayTeamId();
        Long preResolvedTournId = request.getTournamentId();
        if (externalSystemRepository != null && request.getCreatedBySource() != null) {
            try {
                Optional<MdmExternalSystem> sysOpt = findExternalSystemByCodeCached(request.getCreatedBySource());
                if (sysOpt.isPresent()) {
                    Integer sysId = sysOpt.get().getId();
                    if (preResolvedHomeId == null && rawHome != null && teamIngestionMapRepository != null) {
                        var homeMap = teamIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, rawHome.trim());
                        if (homeMap.isPresent() && homeMap.get().getCanonicalTeam() != null) {
                            preResolvedHomeId = resolveCanonicalTeam(homeMap.get().getCanonicalTeam()).getId();
                        }
                    }
                    if (preResolvedAwayId == null && rawAway != null && teamIngestionMapRepository != null) {
                        var awayMap = teamIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, rawAway.trim());
                        if (awayMap.isPresent() && awayMap.get().getCanonicalTeam() != null) {
                            preResolvedAwayId = resolveCanonicalTeam(awayMap.get().getCanonicalTeam()).getId();
                        }
                    }
                    if (preResolvedTournId == null && tournContext != null && tournamentIngestionMapRepository != null) {
                        var tourMap = tournamentIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, tournContext.trim());
                        if (tourMap.isPresent() && tourMap.get().getCanonicalTournament() != null) {
                            preResolvedTournId = tourMap.get().getCanonicalTournament().getId();
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // Fast path: If both team IDs are known, do an indexed O(1) match lookup
        if (preResolvedHomeId != null && preResolvedAwayId != null) {
            List<MdmMatch> directCandidates = matchRepository.findCandidateMatches(sportId, preResolvedHomeId, preResolvedAwayId, windowStart, windowEnd);
            if (!directCandidates.isEmpty()) {
                MdmMatch cand = findClosestCandidate(directCandidates, targetTime);
                MdmTeam canonicalHome = resolveCanonicalTeam(cand.getHomeTeam());
                MdmTeam canonicalAway = resolveCanonicalTeam(cand.getAwayTeam());

                boolean isRacket = sportId == 4 || sportId == 5 || sportId == 13 || sportId == 15;
                boolean candHomeDoubles = canonicalHome.getCanonicalName().contains("/");
                boolean candAwayDoubles = canonicalAway.getCanonicalName().contains("/");
                if ((isRacket && candHomeDoubles != candAwayDoubles)
                        || !isGenderCompatible(canonicalHome.getGender(), canonicalAway.getGender())
                        || !isAgeCompatible(canonicalHome.getAgeCategory(), canonicalAway.getAgeCategory())) {
                    validateMatchGenderAndAge(cand.getTournament(), canonicalHome, canonicalAway, request.getCreatedBySource());
                }

                log.info("🎯 [MDM-ANCHOR-FAST] Matched via pre-resolved IDs (#{} vs #{}) to canonical match #{} ('{}' vs '{}')",
                        preResolvedHomeId, preResolvedAwayId, cand.getId(),
                        canonicalHome.getCanonicalName(), canonicalAway.getCanonicalName());

                if (cand.getTournament() != null) {
                    request.setTournamentId(cand.getTournament().getId());
                }
                request.setHomeTeamId(canonicalHome.getId());
                request.setAwayTeamId(canonicalAway.getId());
                if (cand.getSofascoreId() != null && request.getSofascoreId() == null) {
                    request.setSofascoreId(cand.getSofascoreId());
                }

                saveTeamIngestionMap(request.getCreatedBySource(), rawHome, canonicalHome);
                saveTeamIngestionMap(request.getCreatedBySource(), rawAway, canonicalAway);
                if (tournContext != null && cand.getTournament() != null) {
                    saveTournamentIngestionMap(request.getCreatedBySource(), tournContext, cand.getTournament());
                }

                boolean updated = false;
                if (!Objects.equals(cand.getHomeTeam().getId(), canonicalHome.getId())) {
                    cand.setHomeTeam(canonicalHome);
                    updated = true;
                }
                if (!Objects.equals(cand.getAwayTeam().getId(), canonicalAway.getId())) {
                    cand.setAwayTeam(canonicalAway);
                    updated = true;
                }
                if ("SCHEDULED".equals(cand.getStatus())
                        && Math.abs(cand.getStartTime().toEpochSecond() - targetTime.toEpochSecond()) > 300) {
                    cand.setStartTime(targetTime);
                    updated = true;
                }
                if (request.getSofascoreId() != null && cand.getSofascoreId() == null) {
                    cand.setSofascoreId(request.getSofascoreId());
                    cand.setSourceOrigin("SOFASCORE");
                    updated = true;
                }
                if (updated) {
                    cand = matchRepository.save(cand);
                    broadcastMatchUpserted(cand);
                }

                return toHeaderDto(cand);
            }
        }

        // Targeted candidate selection: Narrow down candidates by known IDs to eliminate 1000-match full table scans
        List<MdmMatch> windowMatches;
        if (preResolvedHomeId != null) {
            windowMatches = matchRepository.findMatchesByTeamInWindow(sportId, preResolvedHomeId, windowStart, windowEnd);
        } else if (preResolvedAwayId != null) {
            windowMatches = matchRepository.findMatchesByTeamInWindow(sportId, preResolvedAwayId, windowStart, windowEnd);
        } else if (preResolvedTournId != null) {
            windowMatches = matchRepository.findMatchesByTournamentInWindow(sportId, preResolvedTournId, windowStart, windowEnd);
        } else {
            // Unmapped sport-wide scan: For fast sports (table tennis, esports Bo1) limit window to +/- 45 mins
            OffsetDateTime narrowStart = (sportId == 5 || sportId == 6 || sportId == 37) ? targetTime.minusMinutes(45) : windowStart;
            OffsetDateTime narrowEnd = (sportId == 5 || sportId == 6 || sportId == 37) ? targetTime.plusMinutes(45) : windowEnd;
            windowMatches = matchRepository.findMatchesInWindow(sportId, narrowStart, narrowEnd);
        }

        if (windowMatches == null || windowMatches.isEmpty()) {
            return null;
        }

        for (MdmMatch cand : windowMatches) {
            MdmTeam canonicalHome = resolveCanonicalTeam(cand.getHomeTeam());
            MdmTeam canonicalAway = resolveCanonicalTeam(cand.getAwayTeam());

            if (!isGenderCompatible(canonicalHome.getGender(), canonicalAway.getGender())
                    || !isAgeCompatible(canonicalHome.getAgeCategory(), canonicalAway.getAgeCategory())) {
                continue; // Skip mutant candidate from anchor matching
            }

            boolean isRacket = sportId == 4 || sportId == 5 || sportId == 13 || sportId == 15;
            if (isRacket) {
                boolean candHomeDoubles = canonicalHome.getCanonicalName().contains("/");
                boolean candAwayDoubles = canonicalAway.getCanonicalName().contains("/");
                if (candHomeDoubles != candAwayDoubles) {
                    continue; // Skip mutant candidate match where one team is doubles and the other is singles
                }
                boolean rawHomeDoubles = rawHome.contains("/");
                boolean rawAwayDoubles = rawAway.contains("/");
                if (rawHomeDoubles != candHomeDoubles || rawAwayDoubles != candAwayDoubles) {
                    if (!(rawHomeDoubles == candAwayDoubles && rawAwayDoubles == candHomeDoubles)) {
                        continue; // Skip candidates with mismatched singles/doubles format
                    }
                }
            }

            String homeGenderCtx = canonicalHome.getGender() != null && "FEMALE".equalsIgnoreCase(canonicalHome.getGender()) ? "(w)" : null;
            String awayGenderCtx = canonicalAway.getGender() != null && "FEMALE".equalsIgnoreCase(canonicalAway.getGender()) ? "(w)" : null;

            String tournName = cand.getTournament() != null ? cand.getTournament().getCanonicalName() : "";
            String homeCtxCand = tournName + (homeGenderCtx != null ? " " + homeGenderCtx : "");
            String awayCtxCand = tournName + (awayGenderCtx != null ? " " + awayGenderCtx : "");

            boolean homeSim = SimilarityUtils.areTeamsSimilar(
                    rawHome, canonicalHome.getCanonicalName(),
                    tournContext, homeCtxCand);
            boolean match = homeSim && SimilarityUtils.areTeamsSimilar(
                    rawAway, canonicalAway.getCanonicalName(),
                    tournContext, awayCtxCand);

            if (!match) {
                // Fallback: match without tournament context interference, but preserving gender flag
                boolean homeSimNoContext = SimilarityUtils.areTeamsSimilar(rawHome, canonicalHome.getCanonicalName(), null, homeGenderCtx);
                if (homeSimNoContext) {
                    match = SimilarityUtils.areTeamsSimilar(rawAway, canonicalAway.getCanonicalName(), null, awayGenderCtx);
                }
            }
            if (!match) {
                boolean revHome = SimilarityUtils.areTeamsSimilar(
                        rawHome, canonicalAway.getCanonicalName(),
                        tournContext, awayCtxCand);
                if (revHome) {
                    match = SimilarityUtils.areTeamsSimilar(
                            rawAway, canonicalHome.getCanonicalName(),
                            tournContext, homeCtxCand);
                }
                if (!match) {
                    boolean revHomeNoContext = SimilarityUtils.areTeamsSimilar(rawHome, canonicalAway.getCanonicalName(), null, awayGenderCtx);
                    if (revHomeNoContext) {
                        match = SimilarityUtils.areTeamsSimilar(rawAway, canonicalHome.getCanonicalName(), null, homeGenderCtx);
                    }
                }
            }

            if (!match && preResolvedHomeId != null && preResolvedAwayId != null) {
                Long candHId = canonicalHome.getId();
                Long candAId = canonicalAway.getId();
                if (Objects.equals(preResolvedHomeId, candHId) && Objects.equals(preResolvedAwayId, candAId)) {
                    match = true;
                }
            }

            long timeDiffSec = Math.abs(cand.getStartTime().toEpochSecond() - targetTime.toEpochSecond());
            // Secondary relaxed match if one team strongly matches, the other is lexically compatible, and time difference is within 1 hour
            if (!match && timeDiffSec <= 3600) {
                boolean homeDirectSim = SimilarityUtils.areTeamsSimilar(rawHome, canonicalHome.getCanonicalName(), null, homeGenderCtx);
                if (homeDirectSim && isLexicallyCompatible(rawAway, canonicalAway.getCanonicalName())
                        && SimilarityUtils.jaroWinklerSimilarity(SimilarityUtils.normalizeToLatin(rawAway), SimilarityUtils.normalizeToLatin(canonicalAway.getCanonicalName())) >= 0.85) {
                    match = true;
                } else {
                    boolean awayDirectSim = SimilarityUtils.areTeamsSimilar(rawAway, canonicalAway.getCanonicalName(), null, awayGenderCtx);
                    if (awayDirectSim && isLexicallyCompatible(rawHome, canonicalHome.getCanonicalName())
                            && SimilarityUtils.jaroWinklerSimilarity(SimilarityUtils.normalizeToLatin(rawHome), SimilarityUtils.normalizeToLatin(canonicalHome.getCanonicalName())) >= 0.85) {
                        match = true;
                    }
                }
            }

            if (match) {
                log.info("🎯 [MDM-ANCHOR] Matched incoming match '{}' vs '{}' (start={}) to canonical match #{} ('{}' vs '{}') in tourn '{}' (sofascoreId={})",
                        rawHome, rawAway, targetTime, cand.getId(),
                        canonicalHome.getCanonicalName(), canonicalAway.getCanonicalName(),
                        tournName, cand.getSofascoreId());

                if (cand.getTournament() != null) {
                    request.setTournamentId(cand.getTournament().getId());
                }
                request.setHomeTeamId(canonicalHome.getId());
                request.setAwayTeamId(canonicalAway.getId());
                if (cand.getSofascoreId() != null && request.getSofascoreId() == null) {
                    request.setSofascoreId(cand.getSofascoreId());
                }

                saveTeamIngestionMap(request.getCreatedBySource(), rawHome, canonicalHome);
                saveTeamIngestionMap(request.getCreatedBySource(), rawAway, canonicalAway);
                if (tournContext != null && cand.getTournament() != null) {
                    saveTournamentIngestionMap(request.getCreatedBySource(), tournContext, cand.getTournament());
                }

                boolean updated = false;
                if (!Objects.equals(cand.getHomeTeam().getId(), canonicalHome.getId())) {
                    cand.setHomeTeam(canonicalHome);
                    updated = true;
                }
                if (!Objects.equals(cand.getAwayTeam().getId(), canonicalAway.getId())) {
                    cand.setAwayTeam(canonicalAway);
                    updated = true;
                }
                if (targetTime != null && "SCHEDULED".equals(cand.getStatus())
                        && Math.abs(cand.getStartTime().toEpochSecond() - targetTime.toEpochSecond()) > 300) {
                    log.info("⏰ [MDM-ANCHOR] Updating start time for match #{} ({}-{}) from {} to {} (source={})",
                            cand.getId(), canonicalHome.getCanonicalName(), canonicalAway.getCanonicalName(),
                            cand.getStartTime(), targetTime, request.getCreatedBySource());
                    cand.setStartTime(targetTime);
                    updated = true;
                }
                if (request.getSofascoreId() != null && cand.getSofascoreId() == null) {
                    cand.setSofascoreId(request.getSofascoreId());
                    cand.setSourceOrigin("SOFASCORE");
                    updated = true;
                }
                if (updated) {
                    cand = matchRepository.save(cand);
                    broadcastMatchUpserted(cand);
                }

                return toHeaderDto(cand);
            }
        }
        return null;
    }

    private void broadcastMatchUpserted(MdmMatch match) {
        // Broadcast via Kafka to running parsers and aggregator
        if (kafkaTemplate != null) {
            try {
                CanonicalMatchHeaderDto header = toHeaderDto(match);
                String messageKey = match.getSport().getId() + ":" + match.getHomeTeam().getId() + ":" + match.getAwayTeam().getId();
                kafkaTemplate.send(matchesUpsertedTopic, messageKey, header);
            } catch (Exception e) {
                log.warn("Failed to broadcast match upserted event to Kafka: {}", e.getMessage());
            }
        }
    }

    public static CanonicalMatchHeaderDto toHeaderDto(MdmMatch match) {
        if (match == null) return null;
        pro.datawiki.igaming.mdm.domain.MdmTournament t = match.getTournament();
        pro.datawiki.igaming.mdm.domain.MdmTeam ht = match.getHomeTeam();
        pro.datawiki.igaming.mdm.domain.MdmTeam at = match.getAwayTeam();
        return CanonicalMatchHeaderDto.builder()
                .matchId(match.getId())
                .sportId(match.getSport().getId())
                .sportSlug(match.getSport().getSlug())
                .tournamentId(t != null ? t.getId() : null)
                .tournamentName(t != null ? t.getCanonicalName() : null)
                .countryId(t != null && t.getCountry() != null ? t.getCountry().getId() : null)
                .countryName(t != null && t.getCountry() != null ? t.getCountry().getCanonicalName() : (t != null ? t.getCountryName() : null))
                .gender(t != null ? t.getGender() : null)
                .ageCategory(t != null ? t.getAgeCategory() : null)
                .isCup(t != null ? t.getIsCup() : null)
                .homeTeamId(ht != null ? ht.getId() : null)
                .homeTeamName(ht != null ? ht.getCanonicalName() : null)
                .homeCountryId(ht != null && ht.getCountry() != null ? ht.getCountry().getId() : null)
                .homeCountryName(ht != null && ht.getCountry() != null ? ht.getCountry().getCanonicalName() : (ht != null ? ht.getCountryName() : null))
                .awayTeamId(at != null ? at.getId() : null)
                .awayTeamName(at != null ? at.getCanonicalName() : null)
                .awayCountryId(at != null && at.getCountry() != null ? at.getCountry().getId() : null)
                .awayCountryName(at != null && at.getCountry() != null ? at.getCountry().getCanonicalName() : (at != null ? at.getCountryName() : null))
                .startTimeEpochSec(match.getStartTime().toEpochSecond())
                .sofascoreId(match.getSofascoreId())
                .mlEligible(t != null && Boolean.TRUE.equals(t.getMlEligible()))
                .coverageTier(t != null ? t.getCoverageTier() : null)
                .sourceOrigin(match.getSourceOrigin())
                .status(match.getStatus())
                .score1(match.getScore1())
                .score2(match.getScore2())
                .regularScore1(match.getRegularScore1())
                .regularScore2(match.getRegularScore2())
                .overtimeScore1(match.getOvertimeScore1())
                .overtimeScore2(match.getOvertimeScore2())
                .penaltiesScore1(match.getPenaltiesScore1())
                .penaltiesScore2(match.getPenaltiesScore2())
                .stage(match.getStage())
                .round(match.getRound())
                .bestOf(match.getBestOf())
                .build();
    }

    private static String sanitizeEntityOrigin(String rawOrigin, Long sofaId) {
        if (sofaId != null) {
            return "SOFASCORE";
        }
        if (rawOrigin != null) {
            String o = rawOrigin.trim().toUpperCase();
            if ("SOFASCORE".equals(o) || "OPERATOR".equals(o) || "NATIVE_LOCAL".equals(o)) {
                return o;
            }
        }
        return "NATIVE_LOCAL";
    }

    private static String sanitizeMatchOrigin(String rawOrigin, Long sofaId) {
        if (sofaId != null) {
            return "SOFASCORE";
        }
        if (rawOrigin != null) {
            String o = rawOrigin.trim().toUpperCase();
            if ("SOFASCORE".equals(o) || "OPERATOR".equals(o) || "BOOKMAKER".equals(o)) {
                return o;
            }
        }
        return "BOOKMAKER";
    }

    public static OffsetDateTime toOffsetDateTime(Long epochTime) {
        if (epochTime == null || epochTime <= 0) return OffsetDateTime.now(ZoneOffset.UTC);
        Instant instant = (epochTime > 100_000_000_000L)
                ? Instant.ofEpochMilli(epochTime)
                : Instant.ofEpochSecond(epochTime);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
