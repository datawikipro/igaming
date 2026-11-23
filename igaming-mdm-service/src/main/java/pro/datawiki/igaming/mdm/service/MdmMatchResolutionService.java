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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

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
    private final MdmSportIngestionMapRepository sportIngestionMapRepository;
    private final MatchIntegrityValidator integrityValidator;
    private final MdmTournamentResolutionService tournamentResolutionService;
    private final MdmTeamResolutionService teamResolutionService;

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
            @Autowired(required = false) MdmSportIngestionMapRepository sportIngestionMapRepository,
            @Autowired(required = false) MatchIntegrityValidator integrityValidator,
            @Autowired(required = false) MdmTournamentResolutionService tournamentResolutionService,
            @Autowired(required = false) MdmTeamResolutionService teamResolutionService
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
        this.sportIngestionMapRepository = sportIngestionMapRepository;

        this.integrityValidator = integrityValidator != null ? integrityValidator
                : new MatchIntegrityValidator(anomalyReportingService, tournamentTeamRepository, teamRepository);

        this.tournamentResolutionService = tournamentResolutionService != null ? tournamentResolutionService
                : new MdmTournamentResolutionService(tournamentRepository, categoryRepository, countryRepository,
                        countryIngestionMapRepository, tournamentIngestionMapRepository, tournamentTranslationRepository,
                        tournamentTeamRepository, sportRepository, externalSystemRepository, this.integrityValidator);

        this.teamResolutionService = teamResolutionService != null ? teamResolutionService
                : new MdmTeamResolutionService(teamRepository, teamIngestionMapRepository, teamTranslationRepository,
                        teamMergeMapRepository, tournamentTeamRepository, sportRepository, externalSystemRepository,
                        categoryRepository, countryRepository, tournamentRepository, this.integrityValidator, this.tournamentResolutionService);
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
                categoryRepository, countryRepository, countryIngestionMapRepository, teamMergeMapRepository, null, null, null, null);
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
                kafkaTemplate, matchesUpsertedTopic, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    public Optional<MdmSport> findSportBySlugCached(String slug) {
        if (slug == null || sportRepository == null) return Optional.empty();
        String rawKey = slug.trim().toLowerCase().replace('_', '-').replaceAll("\\s+", "-");
        MdmSport cached = sportBySlugCache.get(rawKey);
        if (cached != null) return Optional.of(cached);
        Optional<MdmSport> found = sportRepository.findBySlug(rawKey);
        if (found.isEmpty() && rawKey.contains("-")) {
            found = sportRepository.findBySlug(rawKey.replace("-", ""));
        }
        if (found.isEmpty()) {
            pro.datawiki.igaming.dto.sport.Sport dSport = pro.datawiki.igaming.dto.sport.Sports.get(rawKey);
            if (dSport != null && dSport != pro.datawiki.igaming.dto.sport.Sports.UNKNOWN) {
                int id = pro.datawiki.igaming.dto.sport.Sports.getId(dSport);
                if (id > 0) {
                    found = findSportByIdCached(id);
                } else if (dSport.getSlug() != null && !dSport.getSlug().equalsIgnoreCase(rawKey)) {
                    found = sportRepository.findBySlug(dSport.getSlug().trim().toLowerCase());
                }
            }
        }
        found.ifPresent(s -> {
            sportBySlugCache.put(rawKey, s);
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

    public void resolveSportIfMissing(MatchResolutionRequest request) {
        if (request == null || request.getSportId() != null) return;
        if (request.getSportSlug() == null || request.getSportSlug().isBlank()) return;

        if (request.getCreatedBySource() != null && sportIngestionMapRepository != null) {
            findExternalSystemByCodeCached(request.getCreatedBySource()).ifPresent(sys -> {
                sportIngestionMapRepository.findByExternalSystemIdAndRawKey(sys.getId(), request.getSportSlug().trim())
                        .ifPresent(map -> {
                            if (map.getCanonicalSport() != null) {
                                request.setSportId(map.getCanonicalSport().getId());
                                request.setSportSlug(map.getCanonicalSport().getSlug());
                            }
                        });
            });
        }
        if (request.getSportId() == null) {
            findSportBySlugCached(request.getSportSlug())
                    .ifPresent(s -> {
                        request.setSportId(s.getId());
                        request.setSportSlug(s.getSlug());
                    });
        }
        if (request.getSportId() == null) {
            pro.datawiki.igaming.dto.sport.Sport sp = pro.datawiki.igaming.dto.sport.Sports.get(request.getSportSlug());
            if (sp != null && sp != pro.datawiki.igaming.dto.sport.Sports.UNKNOWN) {
                int id = pro.datawiki.igaming.dto.sport.Sports.getId(sp);
                if (id > 0) {
                    request.setSportId(id);
                    if (sp.getSlug() != null) request.setSportSlug(sp.getSlug());
                }
            }
        }
        if (request.getSportId() == null) {
            pro.datawiki.igaming.dto.SportType st = pro.datawiki.igaming.dto.SportType.fromAny(request.getSportSlug());
            if (st != null && st != pro.datawiki.igaming.dto.SportType.UNKNOWN) {
                findSportBySlugCached(st.getSlug()).ifPresent(s -> {
                    request.setSportId(s.getId());
                    request.setSportSlug(s.getSlug());
                });
            }
        }
    }

    public void dispatchTrueSport(MatchResolutionRequest request) {
        if (request == null) return;
        String tName = request.getTournamentName();
        String hName = request.getHomeTeamName();
        String aName = request.getAwayTeamName();
        String combined = (tName != null ? tName : "") + " " + (hName != null ? hName : "") + " " + (aName != null ? aName : "");
        String lower = combined.toLowerCase(Locale.ROOT);

        // CS2 / Counter-Strike
        if (lower.contains("clutch series") || lower.contains("counter-strike") || lower.contains("cs2") || lower.contains("cs:go")
                || (lower.contains("astralis") && lower.contains("heroic"))) {
            request.setSportId(33);
            request.setSportSlug("cs2");
            return;
        }
        // Dota 2
        if (lower.contains("dota") || lower.contains("the international")) {
            request.setSportId(34);
            request.setSportSlug("dota2");
            return;
        }
        // League of Legends
        if (lower.contains("lck") || lower.contains("lpl") || lower.contains("lec") || lower.contains("league of legends") || lower.contains("worlds")) {
            request.setSportId(35);
            request.setSportSlug("league-of-legends");
            return;
        }
        // Valorant
        if ((lower.contains("valorant") || lower.contains("vct")) && (lower.contains("champions") || lower.contains("challengers") || lower.contains("masters"))) {
            request.setSportId(36);
            request.setSportSlug("valorant");
            return;
        }
        // Cyber Football (FIFA / EA FC)
        if (lower.contains("fifa") || lower.contains("fc 24") || lower.contains("fc 25") || lower.contains("efootball") || lower.contains("esoccer") || lower.contains("киберфутбол")) {
            request.setSportId(37);
            request.setSportSlug("cyber-football");
            return;
        }
        // Cyber Hockey
        if (lower.contains("ehockey") || lower.contains("nhl 2") || lower.contains("киберхоккей")) {
            request.setSportId(41);
            request.setSportSlug("cyber-hockey");
            return;
        }
        // Cyber Basketball
        if (lower.contains("ebasketball") || lower.contains("nba 2") || lower.contains("кибербаскетбол")) {
            request.setSportId(42);
            request.setSportSlug("cyber-basketball");
            return;
        }
    }



    public Long remapTeamId(Long localId) {
        return teamResolutionService.remapTeamId(localId);
    }

    public MdmCategory resolveCategory(Integer sportId, String countryName, String categoryName, Boolean isInternational) {
        return tournamentResolutionService.resolveCategory(sportId, countryName, categoryName, isInternational);
    }

    public CanonicalTournamentDto findOrCreateTournament(TournamentResolutionRequest request) {
        return tournamentResolutionService.findOrCreateTournament(request);
    }

    public CanonicalTeamDto findOrCreateTeam(TeamResolutionRequest request) {
        return teamResolutionService.findOrCreateTeam(request);
    }

    @Transactional(readOnly = true)
    public Optional<CanonicalTournamentDto> getTournamentById(Long id) {
        return tournamentResolutionService.getTournamentById(id);
    }

    @Transactional(readOnly = true)
    public Optional<CanonicalTeamDto> getTeamById(Long id) {
        return teamResolutionService.getTeamById(id);
    }

    public static CanonicalTournamentDto toTournamentDto(MdmTournament t) {
        return MdmTournamentResolutionService.toTournamentDto(t);
    }

    public static CanonicalTeamDto toTeamDto(MdmTeam team) {
        return MdmTeamResolutionService.toTeamDto(team);
    }

    public void registerTournamentParticipant(MdmTournament tournament, MdmTeam team) {
        tournamentResolutionService.registerTournamentParticipant(tournament, team);
    }

    public MdmCountry resolveCountryEntity(String systemCode, String rawCategoryKey, String countryName, String categoryName, Boolean isInternational) {
        return tournamentResolutionService.resolveCountryEntity(systemCode, rawCategoryKey, countryName, categoryName, isInternational);
    }

    public MdmCountry resolveCountryEntity(String countryName, Boolean isInternational) {
        return tournamentResolutionService.resolveCountryEntity(countryName, isInternational);
    }

    public void healTournamentMetadataIfNeeded(MdmTournament tourney) {
        tournamentResolutionService.healTournamentMetadataIfNeeded(tourney);
    }

    public boolean isTournamentMappingAcceptable(String rawKey, String tournName, MdmTournament mappedTourn) {
        return tournamentResolutionService.isTournamentMappingAcceptable(rawKey, tournName, mappedTourn);
    }

    public boolean isTournamentMappingAcceptable(String rawKey, String tournName, MdmTournament mappedTourn, String countryCtx, MdmCountry countryEntity) {
        return tournamentResolutionService.isTournamentMappingAcceptable(rawKey, tournName, mappedTourn, countryCtx, countryEntity);
    }

    public void saveTournamentIngestionMap(String systemCode, String rawKey, MdmTournament tourney) {
        tournamentResolutionService.saveTournamentIngestionMap(systemCode, rawKey, tourney);
    }

    public void saveTournamentIngestionMap(String systemCode, String rawKey, String sourceId, MdmTournament tourney) {
        tournamentResolutionService.saveTournamentIngestionMap(systemCode, rawKey, sourceId, tourney);
    }

    public void enrichTeamSofascoreId(MdmTeam team, Long sofaId) {
        teamResolutionService.enrichTeamSofascoreId(team, sofaId);
    }

    public MdmTeam resolveCanonicalTeam(MdmTeam team) {
        return teamResolutionService.resolveCanonicalTeam(team);
    }

    public void saveTeamIngestionMap(String systemCode, String rawKey, MdmTeam team) {
        teamResolutionService.saveTeamIngestionMap(systemCode, rawKey, team);
    }

    public void saveTeamIngestionMap(String systemCode, String rawKey, String sourceId, MdmTeam team) {
        teamResolutionService.saveTeamIngestionMap(systemCode, rawKey, sourceId, team);
    }

    public void healTeamMetadataWithTournamentContext(MdmTeam team, MdmTournament tournament) {
        teamResolutionService.healTeamMetadataWithTournamentContext(team, tournament);
    }

    public void healTeamMetadataWithTournamentContext(MdmTeam team, MdmTournament tournament, String sourceOrigin) {
        teamResolutionService.healTeamMetadataWithTournamentContext(team, tournament, sourceOrigin);
    }

    public void validateLeagueIntegrity(MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String sourceOrigin, String createdBy) {
        healTeamMetadataWithTournamentContext(homeTeam, tournament, sourceOrigin);
        healTeamMetadataWithTournamentContext(awayTeam, tournament, sourceOrigin);
        integrityValidator.validateLeagueIntegrity(tournament, homeTeam, awayTeam, sourceOrigin, createdBy);
    }

    public static String detectTournamentGender(String lowerTourn) {
        return MdmTournamentResolutionService.detectTournamentGender(lowerTourn);
    }

    public static String detectTournamentAge(String lowerTourn) {
        return MdmTournamentResolutionService.detectTournamentAge(lowerTourn);
    }

    public static boolean hasCyrillic(String input) {
        return MdmTournamentResolutionService.hasCyrillic(input);
    }

    public static String toSlug(String input) {
        return MdmTournamentResolutionService.toSlug(input);
    }

    public static boolean isLexicallyCompatible(String rawKey, String canonicalName) {
        return MdmTournamentResolutionService.isLexicallyCompatible(rawKey, canonicalName);
    }

    public static boolean isAgeCompatible(String teamAge, String expectedAge) {
        return MatchIntegrityValidator.isAgeCompatible(teamAge, expectedAge, null);
    }

    public static boolean isAgeCompatible(String teamAge, String expectedAge, MdmTournament tournament) {
        return MatchIntegrityValidator.isAgeCompatible(teamAge, expectedAge, tournament);
    }

    public boolean isInternationalTournament(MdmTournament tournament) {
        return integrityValidator.isInternationalTournament(tournament);
    }

    public boolean isCountryCompatible(MdmTournament tournament, MdmTeam team) {
        return integrityValidator.isCountryCompatible(tournament, team);
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
        resolveSportIfMissing(request);
        dispatchTrueSport(request);
        String src = request.getCreatedBySource() != null ? request.getCreatedBySource() : "src";
        Integer sport = request.getSportId();
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

        resolveSportIfMissing(request);
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

        // 2. Fast-path pre-validation on raw request names BEFORE resolving entities
        validatePreResolution(request);

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
                healTeamMetadataWithTournamentContext(matched.getHomeTeam(), matched.getTournament(), request.getSourceOrigin());
                healTeamMetadataWithTournamentContext(matched.getAwayTeam(), matched.getTournament(), request.getSourceOrigin());
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
                    final Integer effectiveSportId = sportId;
                    tournamentRepository.findById(request.getTournamentId())
                            .filter(t -> Objects.equals(t.getSport().getId(), effectiveSportId))
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
        final int targetSportId = sportId;
        MdmSport sport = findSportByIdCached(targetSportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found with ID: " + targetSportId));

        MdmTournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found with ID: " + request.getTournamentId()));
        healTournamentMetadataIfNeeded(tournament);

        MdmTeam homeTeam = teamRepository.findById(homeId)
                .orElseThrow(() -> new IllegalArgumentException("Home team not found with ID: " + homeId));

        MdmTeam awayTeam = teamRepository.findById(awayId)
                .orElseThrow(() -> new IllegalArgumentException("Away team not found with ID: " + awayId));

        String createdBy = request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank()
                ? request.getCreatedBySource()
                : "system";

        // Strict cross-sport validation
        if (!Objects.equals(tournament.getSport().getId(), sportId)) {
            if (MatchIntegrityValidator.isSubSportOf(tournament.getSport().getId(), sportId)) {
                log.info("🛡️ [MDM] Adapted requested sport {} to specialized tournament sub-sport {} ({}) for '{}'",
                        sportId, tournament.getSport().getId(), tournament.getSport().getSlug(), tournament.getCanonicalName());
                sportId = tournament.getSport().getId();
                sport = tournament.getSport();
                request.setSportId(sportId);
            } else {
                String msg = String.format("Integrity violation: Tournament '%s' (sport=%d) does not match requested sport %d",
                        tournament.getCanonicalName(), tournament.getSport().getId(), sportId);
                anomalyReportingService.reportLeagueMismatch(createdBy, "SPORT_MISMATCH", tournament.getId(), tournament.getCanonicalName(), null, null, msg);
                throw new IllegalArgumentException(msg);
            }
        }
        if (!Objects.equals(homeTeam.getSport().getId(), sportId)) {
            if (!MatchIntegrityValidator.isSportCompatible(homeTeam.getSport().getId(), sportId)) {
                String msg = String.format("Integrity violation: Home Team '%s' (sport=%d) does not match requested sport %d",
                        homeTeam.getCanonicalName(), homeTeam.getSport().getId(), sportId);
                anomalyReportingService.reportLeagueMismatch(createdBy, "SPORT_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
        }
        if (!Objects.equals(awayTeam.getSport().getId(), sportId)) {
            if (!MatchIntegrityValidator.isSportCompatible(awayTeam.getSport().getId(), sportId)) {
                String msg = String.format("Integrity violation: Away Team '%s' (sport=%d) does not match requested sport %d",
                        awayTeam.getCanonicalName(), awayTeam.getSport().getId(), sportId);
                anomalyReportingService.reportLeagueMismatch(createdBy, "SPORT_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
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
        match.setSourceOrigin(sanitizeMatchOrigin(sourceOrigin, sofascoreId));
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
        resolveSportIfMissing(request);
        dispatchTrueSport(request);

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
        CanonicalTournamentDto tDto = null;
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
            tDto = findOrCreateTournament(tReq);
            request.setTournamentId(tDto.getTournamentId());
            if (resolvedCountryId == null && tDto.getCountryId() != null) {
                resolvedCountryId = tDto.getCountryId();
            }
            if (tDto.getSportId() != null && request.getSportId() != null && !Objects.equals(request.getSportId(), tDto.getSportId())) {
                if (MatchIntegrityValidator.isSubSportOf(tDto.getSportId(), request.getSportId())) {
                    log.info("🛡️ [MDM] Adapting requested sport {} to tournament's sub-sport {} ({}) for '{}'",
                            request.getSportId(), tDto.getSportId(), tDto.getSportSlug(), request.getTournamentName());
                    request.setSportId(tDto.getSportId());
                    request.setSportSlug(tDto.getSportSlug());
                }
            }
        } else {
            tournamentRepository.findById(request.getTournamentId()).ifPresent(t -> {
                if (t.getSport() != null && request.getSportId() != null && !Objects.equals(request.getSportId(), t.getSport().getId())) {
                    if (MatchIntegrityValidator.isSubSportOf(t.getSport().getId(), request.getSportId())) {
                        log.info("🛡️ [MDM] Adapting requested sport {} to existing tournament's sub-sport {} ({}) for '{}'",
                                request.getSportId(), t.getSport().getId(), t.getSport().getSlug(), t.getCanonicalName());
                        request.setSportId(t.getSport().getId());
                        request.setSportSlug(t.getSport().getSlug());
                    }
                }
            });
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

        // Fail-fast: Only SOFASCORE source origin (or matches with SofaScore ID) is authorized to create canonical entities.
        // Bookmaker sources must have all entities pre-resolved in Ingestion Blueprint.
        boolean isSofaScore = "SOFASCORE".equalsIgnoreCase(request.getSourceOrigin()) || request.getSofascoreId() != null;
        if (!isSofaScore && (request.getTournamentId() == null || request.getHomeTeamId() == null || request.getAwayTeamId() == null)) {
            String msg = String.format("Refusing unverified entity generation from %s origin: tournamentId=%s, homeTeamId=%s, awayTeamId=%s for '%s vs %s' in '%s'",
                    request.getSourceOrigin(), request.getTournamentId(), request.getHomeTeamId(), request.getAwayTeamId(),
                    request.getHomeTeamName(), request.getAwayTeamName(), request.getTournamentName());
            log.warn("🛑 [MDM-FAILFAST] {}", msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Resolves an MDM Category by country name, category name or international flag.
     * Auto-creates the category if recognized country is missing in the sport taxonomy.
     */


    public void validateMatchGenderAndAge(MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String createdBy) {
        healTeamMetadataWithTournamentContext(homeTeam, tournament);
        healTeamMetadataWithTournamentContext(awayTeam, tournament);
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

    private void validatePreResolution(MatchResolutionRequest request) {
        if (request == null) return;
        if (isSyntheticPlaceholder(request.getHomeTeamName()) || isSyntheticPlaceholder(request.getAwayTeamName())) {
            throw new IllegalArgumentException("Synthetic placeholder match rejected: " + request.getHomeTeamName() + " vs " + request.getAwayTeamName());
        }
        if (isStatsOrOutrightMatch(request)) {
            throw new IllegalArgumentException("Stats props or outright pseudo-match rejected: " + request.getHomeTeamName() + " vs " + request.getAwayTeamName() + " in " + request.getTournamentName());
        }
        if (isMalformedDoubles(request)) {
            throw new IllegalArgumentException("Malformed doubles mismatch (singles vs doubles): '" + request.getHomeTeamName() + "' vs '" + request.getAwayTeamName() + "'");
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
                .tier(t != null ? t.getTier() : null)
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
