package pro.datawiki.igaming.mdm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.*;
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer;
import pro.datawiki.igaming.dto.util.SimilarityUtils;
import pro.datawiki.igaming.mdm.domain.*;
import pro.datawiki.igaming.mdm.repository.*;
import pro.datawiki.igaming.mdm.util.CountryNormalizer;

import java.text.Normalizer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Authoritative single entry-point for resolving or creating canonical MDM entities:
 * 1. Tournaments: POST /api/v1/mdm/tournaments/find-or-create (scoped by country/category, international)
 * 2. Teams: POST /api/v1/mdm/teams/find-or-create (scoped by country/category, roster context)
 * 3. Matches: POST /api/v1/mdm/matches/find-or-create (accepts pre-resolved IDs or orchestrates resolution)
 *
 * Enforces composite sport and league integrity constraints at application and database level.
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
        "premier league", "serie a", "primera division", "primera divisiones",
        "super league", "first division", "superliga", "division 1", "division 2",
        "segunda division", "segunda liga", "tercera division", "liga 1", "liga 2",
        "nacional", "liga nacional", "cup", "fa cup", "national cup",
        "super cup", "supercopa", "league cup", "league one", "league two",
        "championship", "bundesliga", "eredivisie", "allsvenskan", "ekstraklasa"
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
            @Autowired(required = false) MdmCountryRepository countryRepository
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
                kafkaTemplate, matchesUpsertedTopic, null, null, null, null, null, null, null);
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
        resolveEntities(request);
        validateRequest(request);

        OffsetDateTime targetTime = toOffsetDateTime(request.getStartTimeEpochSec());
        Integer sportId = request.getSportId();
        Long homeId = request.getHomeTeamId();
        Long awayId = request.getAwayTeamId();

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
                return toHeaderDto(existing);
            }
        }

        // 2. Search by Team Pair within a flexible +/- 18-hour window
        OffsetDateTime windowStart = targetTime.minusHours(18);
        OffsetDateTime windowEnd = targetTime.plusHours(18);
        List<MdmMatch> candidates = matchRepository.findCandidateMatches(sportId, homeId, awayId, windowStart, windowEnd);

        if (!candidates.isEmpty()) {
            // Pick candidate closest in time
            MdmMatch matched = findClosestCandidate(candidates, targetTime);
            boolean updated = false;

            // If incoming is SofaScore and matched match does not have sofascoreId, enrich it
            if (request.getSofascoreId() != null && matched.getSofascoreId() == null) {
                log.info("💎 [MDM] Promoting match #{} ({}) with SofaScore ID {}",
                        matched.getId(), matched.getSourceOrigin(), request.getSofascoreId());
                matched.setSofascoreId(request.getSofascoreId());
                matched.setSourceOrigin("SOFASCORE");
                if (request.getTournamentId() != null) {
                    tournamentRepository.findById(request.getTournamentId())
                            .filter(t -> Objects.equals(t.getSport().getId(), sportId))
                            .ifPresent(matched::setTournament);
                }
                updated = true;
            }

            if (updated) {
                matched = matchRepository.save(matched);
                broadcastMatchUpserted(matched);
            }
            return toHeaderDto(matched);
        }

        // 3. Match not found -> Create new Canonical MDM Match
        MdmSport sport = sportRepository.findById(sportId)
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

        String origin = request.getSourceOrigin() != null && !request.getSourceOrigin().isBlank()
                ? request.getSourceOrigin().toUpperCase()
                : (request.getSofascoreId() != null ? "SOFASCORE" : "BOOKMAKER");

        // Strict League Integrity: Gender, Age, Category/Country and Roster Membership
        validateLeagueIntegrity(tournament, homeTeam, awayTeam, origin, createdBy);

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

    private void resolveEntities(MatchResolutionRequest request) {
        if (request == null) return;

        // 1. Resolve Sport if missing
        if (request.getSportId() == null && request.getSportSlug() != null) {
            sportRepository.findBySlug(request.getSportSlug().trim().toLowerCase())
                    .ifPresent(s -> request.setSportId(s.getId()));
        }

        // 2. Resolve or Auto-create Tournament FIRST (to provide context for teams)
        if (request.getTournamentId() == null) {
            TournamentResolutionRequest tReq = TournamentResolutionRequest.builder()
                    .sportId(request.getSportId())
                    .sportSlug(request.getSportSlug())
                    .sofascoreTournamentId(request.getSofascoreTournamentId())
                    .tournamentName(request.getTournamentName())
                    .categoryName(request.getCategoryName())
                    .countryName(request.getCountryName())
                    .isInternational(request.getIsInternational())
                    .createdBySource(request.getCreatedBySource())
                    .sourceOrigin(request.getSourceOrigin())
                    .build();
            CanonicalTournamentDto tDto = findOrCreateTournament(tReq);
            request.setTournamentId(tDto.getTournamentId());
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
                    .countryName(request.getCountryName())
                    .categoryName(request.getCategoryName())
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
                    .countryName(request.getCountryName())
                    .categoryName(request.getCategoryName())
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
            MdmSport sport = sportRepository.findById(sportId).orElse(null);
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
        if (request.getSportId() == null && request.getSportSlug() != null) {
            sportRepository.findBySlug(request.getSportSlug().trim().toLowerCase())
                    .ifPresent(s -> request.setSportId(s.getId()));
        }
        Integer sportId = request.getSportId();
        if (sportId == null) throw new IllegalArgumentException("Sport ID is mandatory");

        Long sofaId = request.getSofascoreTournamentId();
        String tournName = request.getTournamentName();
        if (tournName != null) tournName = tournName.trim();
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
        String countryCtx = null;
        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            countryCtx = request.getCategoryName().trim();
        } else if (countryName != null && !countryName.isBlank()) {
            countryCtx = countryName;
        }

        MdmCategory category = resolveCategory(sportId, countryName, request.getCategoryName(), isIntl);
        if (category != null && countryCtx == null) {
            countryCtx = category.getSlug();
        }

        // 2. Check external system ingestion map first (composite key preferred for ambiguous names)
        if (request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank()
                && tournName != null && externalSystemRepository != null && tournamentIngestionMapRepository != null) {
            Optional<MdmExternalSystem> sysOpt = externalSystemRepository.findByCode(request.getCreatedBySource());
            if (sysOpt.isPresent()) {
                Integer sysId = sysOpt.get().getId();
                String compositeKey = countryCtx != null ? (countryCtx + " - " + tournName) : null;
                Optional<MdmTournamentIngestionMap> mapOpt = compositeKey != null
                        ? tournamentIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, compositeKey)
                        : Optional.empty();
                if (mapOpt.isEmpty()) {
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

            // 4. Category-scoped lookup (strict separation by country category)
            if (category != null) {
                Optional<MdmTournament> byCatAndName = tournamentRepository.findFirstBySportIdAndCategoryIdAndCanonicalNameIgnoreCase(
                        sportId, category.getId(), tournName);
                if (byCatAndName.isPresent()) {
                    MdmTournament t = byCatAndName.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    enrichTournamentCountry(t, countryCtx);
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
                    enrichTournamentCountry(t, countryCtx);
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }
            }

            // 5. Country-aware lookup by countryName
            if (isAmbiguous && countryCtx != null) {
                Optional<MdmTournament> byNameAndCountry = tournamentRepository.findFirstBySportIdAndNameAndCountry(
                        sportId, tournName, countryCtx);
                if (byNameAndCountry.isPresent()) {
                    MdmTournament t = byNameAndCountry.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    enrichTournamentCountry(t, countryCtx);
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }
                log.debug("[MDM] Ambiguous league '{}' with country '{}' - no existing match, will auto-create", tournName, countryCtx);
            } else if (!isAmbiguous) {
                // Non-ambiguous name: safe to match globally by name/slug
                Optional<MdmTournament> byName = tournamentRepository.findFirstBySportIdAndCanonicalNameIgnoreCase(sportId, tournName);
                if (byName.isPresent()) {
                    MdmTournament t = byName.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    enrichTournamentCountry(t, countryCtx);
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
                    enrichTournamentCountry(t, countryCtx);
                    if (t.getCategory() == null && category != null) {
                        t.setCategory(category);
                        t = tournamentRepository.save(t);
                    }
                    saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), t);
                    return toTournamentDto(t);
                }
            }
        }

        // 6. Auto-creation of Canonical Tournament
        if (tournName == null || tournName.isBlank()) {
            throw new IllegalArgumentException("Cannot resolve or create tournament without tournamentName or sofascoreTournamentId");
        }

        MdmSport sport = sportRepository.findById(sportId)
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

        String origin = request.getSourceOrigin() != null && !request.getSourceOrigin().isBlank()
                ? request.getSourceOrigin().toUpperCase()
                : (sofaId != null ? "SOFASCORE" : "NATIVE_LOCAL");

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

        MdmCountry resolvedCountry = resolveCountryEntity(countryCtx, isIntl);

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
        if (request.getSportId() == null && request.getSportSlug() != null) {
            sportRepository.findBySlug(request.getSportSlug().trim().toLowerCase())
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
        String countryName = request.getCountryName();
        if (countryName == null && contextTournament != null && contextTournament.getCountryName() != null) {
            countryName = contextTournament.getCountryName();
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

        // 4. Ingestion map check
        if (request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank()
                && teamName != null && externalSystemRepository != null && teamIngestionMapRepository != null) {
            Optional<MdmExternalSystem> sysOpt = externalSystemRepository.findByCode(request.getCreatedBySource());
            if (sysOpt.isPresent()) {
                Optional<MdmTeamIngestionMap> mapOpt = teamIngestionMapRepository.findByExternalSystemIdAndRawKey(sysOpt.get().getId(), teamName);
                if (mapOpt.isPresent() && mapOpt.get().getCanonicalTeam() != null) {
                    MdmTeam mappedTeam = mapOpt.get().getCanonicalTeam();
                    if (Objects.equals(mappedTeam.getSport().getId(), sportId)) {
                        boolean genderMatch = mappedTeam.getGender() == null || mappedTeam.getGender().equalsIgnoreCase(expectedGender);
                        boolean ageMatch = isAgeCompatible(mappedTeam.getAgeCategory(), expectedAge);
                        if (genderMatch && ageMatch) {
                            if (contextTournament != null) registerTournamentParticipant(contextTournament, mappedTeam);
                            enrichTeamCountry(mappedTeam, countryName);
                            return toTeamDto(mappedTeam);
                        } else {
                            log.warn("⚠️ [MDM] Rejected mapped team #{} '{}' (gender={}, age={}) because expected is (gender={}, age={})",
                                    mappedTeam.getId(), mappedTeam.getCanonicalName(), mappedTeam.getGender(), mappedTeam.getAgeCategory(), expectedGender, expectedAge);
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

                    boolean match = SimilarityUtils.areTeamsSimilar(
                            teamName, candidate.getCanonicalName(),
                            tournamentContext, contextTournament.getCanonicalName()
                    );
                    if (!match && !rawClean.isBlank()) {
                        String candClean = toSlug(candidate.getCanonicalName())
                                .replaceAll("(?i)\\b(u\\d+|reserve|women|fc|sc|cd|cf|sad|19\\d\\d|20\\d\\d)\\b", "")
                                .replaceAll("[^a-z]", "");
                        if (!candClean.isBlank()) {
                            match = candClean.equals(rawClean) || (rawClean.length() >= 4 && candClean.contains(rawClean)) || (candClean.length() >= 4 && rawClean.contains(candClean));
                            if (!match) {
                                String candPhonetic = candClean.replace('s', 'z');
                                String rawPhonetic = rawClean.replace('s', 'z');
                                match = candPhonetic.equals(rawPhonetic) || (rawPhonetic.length() >= 4 && candPhonetic.contains(rawPhonetic)) || (candPhonetic.length() >= 4 && rawPhonetic.contains(candPhonetic));
                            }
                        }
                    }

                    if (match) {
                        log.info("🎯 [MDM] Resolved team '{}' to tournament participant #{} '{}' in tournament #{} '{}'",
                                teamName, candidate.getId(), candidate.getCanonicalName(),
                                contextTournament.getId(), contextTournament.getCanonicalName());
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, candidate);
                        enrichTeamCountry(candidate, countryName);
                        return toTeamDto(candidate);
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
                    return toTeamDto(team);
                }
            }
        }

        // 7. Category-scoped Team Lookup (avoids cross-country collisions)
        if (teamName != null) {
            if (category != null) {
                Optional<MdmTeam> byCatAndName = teamRepository.findFirstBySportIdAndCategoryIdAndCanonicalNameIgnoreCase(
                        sportId, category.getId(), teamName);
                if (byCatAndName.isPresent()) {
                    MdmTeam team = byCatAndName.get();
                    if (isAgeCompatible(team.getAgeCategory(), expectedAge) && (team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender))) {
                        if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                        enrichTeamCountry(team, countryName);
                        return toTeamDto(team);
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
                        enrichTeamCountry(team, countryName);
                        return toTeamDto(team);
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
                        return toTeamDto(team);
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
                if (genderMatch && ageMatch) {
                    if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                    saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                    enrichTeamCountry(team, countryName);
                    if (team.getCategory() == null && category != null) {
                        team.setCategory(category);
                        team = teamRepository.save(team);
                    }
                    return toTeamDto(team);
                }
            }

            String normSig = toSlug(parsed != null ? parsed.coreClubName() : teamName);
            Optional<MdmTeam> bySig = teamRepository.findBySportIdAndNormalizedSignatureAndAgeCategoryAndGender(
                    sportId, normSig, expectedAge, expectedGender);
            if (bySig.isPresent()) {
                MdmTeam team = bySig.get();
                if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                enrichTeamCountry(team, countryName);
                if (team.getCategory() == null && category != null) {
                    team.setCategory(category);
                    team = teamRepository.save(team);
                }
                return toTeamDto(team);
            }

            if (!normSig.equals(teamSlug)) {
                Optional<MdmTeam> byCoreSlug = teamRepository.findBySportIdAndSlug(sportId, normSig);
                if (byCoreSlug.isPresent()) {
                    MdmTeam team = byCoreSlug.get();
                    boolean genderMatch = team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender);
                    boolean ageMatch = isAgeCompatible(team.getAgeCategory(), expectedAge);
                    if (genderMatch && ageMatch) {
                        if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                        saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                        enrichTeamCountry(team, countryName);
                        if (team.getCategory() == null && category != null) {
                            team.setCategory(category);
                            team = teamRepository.save(team);
                        }
                        return toTeamDto(team);
                    }
                }
            }
        }

        // 10. Auto-creation of Canonical Team
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Cannot resolve or create team without teamName or sofascoreTeamId");
        }

        MdmSport sport = sportRepository.findById(sportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found with ID: " + sportId));

        String normSig = toSlug(parsed != null ? parsed.coreClubName() : teamName);
        String slug = toSlug(teamName) + (sofaId != null ? "-" + sofaId : "");
        Optional<MdmTeam> existingSlug = teamRepository.findBySportIdAndSlug(sportId, slug);
        if (existingSlug.isPresent()) {
            MdmTeam team = existingSlug.get();
            if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
            saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
            enrichTeamCountry(team, countryName);
            return toTeamDto(team);
        }

        String origin = request.getSourceOrigin() != null && !request.getSourceOrigin().isBlank()
                ? request.getSourceOrigin().toUpperCase()
                : (sofaId != null ? "SOFASCORE" : "NATIVE_LOCAL");

        String effectiveCountry = countryName != null ? countryName : (category != null ? category.getSlug() : null);
        MdmCountry resolvedCountry = resolveCountryEntity(effectiveCountry, false);

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
                .country(resolvedCountry)
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
                .countryName(team.getCountryName())
                .canonicalName(team.getCanonicalName())
                .slug(team.getSlug())
                .ageCategory(team.getAgeCategory())
                .gender(team.getGender())
                .sofascoreId(team.getSofascoreId())
                .sourceOrigin(team.getSourceOrigin())
                .build();
    }

    public void registerTournamentParticipant(MdmTournament tournament, MdmTeam team) {
        if (tournament == null || team == null || tournamentTeamRepository == null) return;
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

    private MdmCountry resolveCountryEntity(String countryName, Boolean isInternational) {
        if (countryRepository == null) return null;
        if (Boolean.TRUE.equals(isInternational)) return null;
        if (countryName == null || countryName.isBlank()) return null;

        CountryNormalizer.CountryInfo cInfo = CountryNormalizer.normalize(countryName, false);
        if (cInfo != null) {
            Optional<MdmCountry> byIso = countryRepository.findByIsoCodeIgnoreCase(cInfo.code());
            if (byIso.isPresent()) return byIso.get();
            Optional<MdmCountry> byName = countryRepository.findByCanonicalNameIgnoreCase(cInfo.slug());
            if (byName.isPresent()) return byName.get();
        }
        return countryRepository.findByCanonicalNameIgnoreCase(countryName.trim()).orElse(null);
    }

    private void enrichTournamentCountry(MdmTournament t, String countryCtx) {
        if (countryCtx != null && !countryCtx.isBlank()) {
            boolean updated = false;
            if (t.getCountryName() == null) {
                t.setCountryName(countryCtx);
                updated = true;
            }
            if (t.getCountry() == null && countryRepository != null) {
                MdmCountry c = resolveCountryEntity(countryCtx, false);
                if (c != null) {
                    t.setCountry(c);
                    updated = true;
                }
            }
            if (updated) {
                tournamentRepository.save(t);
            }
        }
    }

    private void enrichTeamCountry(MdmTeam team, String countryCtx) {
        if (countryCtx != null && !countryCtx.isBlank()) {
            boolean updated = false;
            if (team.getCountryName() == null) {
                team.setCountryName(countryCtx);
                updated = true;
            }
            if (team.getCountry() == null && countryRepository != null) {
                MdmCountry c = resolveCountryEntity(countryCtx, false);
                if (c != null) {
                    team.setCountry(c);
                    updated = true;
                }
            }
            if (updated) {
                teamRepository.save(team);
            }
        }
    }

    private void saveTeamIngestionMap(String systemCode, String rawKey, MdmTeam team) {
        if (systemCode == null || systemCode.isBlank() || rawKey == null || rawKey.isBlank() || team == null) return;
        if (externalSystemRepository == null || teamIngestionMapRepository == null) return;
        try {
            externalSystemRepository.findByCode(systemCode).ifPresent(sys -> {
                teamIngestionMapRepository.insertIgnoreConflict(sys.getId(), rawKey, team.getId(), rawKey);
            });
        } catch (Exception e) {
            log.debug("Team ingestion map insert ignored: {}", e.getMessage());
        }
    }

    private void saveTournamentIngestionMap(String systemCode, String rawKey, MdmTournament tourney) {
        if (systemCode == null || systemCode.isBlank() || rawKey == null || rawKey.isBlank() || tourney == null) return;
        if (externalSystemRepository == null || tournamentIngestionMapRepository == null) return;
        try {
            externalSystemRepository.findByCode(systemCode).ifPresent(sys -> {
                tournamentIngestionMapRepository.insertIgnoreConflict(sys.getId(), rawKey, tourney.getId(), rawKey);
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

    private boolean isAgeCompatible(String teamAge, String expectedAge) {
        if (expectedAge == null || "SENIOR".equalsIgnoreCase(expectedAge)) {
            return teamAge == null || "SENIOR".equalsIgnoreCase(teamAge);
        }
        return expectedAge.equalsIgnoreCase(teamAge);
    }

    private void validateLeagueIntegrity(MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String sourceOrigin, String createdBy) {
        // 1. Gender check
        if (tournament.getGender() != null && !"MIXED".equalsIgnoreCase(tournament.getGender())) {
            if (homeTeam.getGender() != null && !Objects.equals(homeTeam.getGender(), tournament.getGender())) {
                String msg = String.format("Gender mismatch: Home team '%s' (%s) cannot play in %s tournament '%s'",
                        homeTeam.getCanonicalName(), homeTeam.getGender(), tournament.getGender(), tournament.getCanonicalName());
                anomalyReportingService.reportLeagueMismatch(createdBy, "GENDER_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
            if (awayTeam.getGender() != null && !Objects.equals(awayTeam.getGender(), tournament.getGender())) {
                String msg = String.format("Gender mismatch: Away team '%s' (%s) cannot play in %s tournament '%s'",
                        awayTeam.getCanonicalName(), awayTeam.getGender(), tournament.getGender(), tournament.getCanonicalName());
                anomalyReportingService.reportLeagueMismatch(createdBy, "GENDER_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
        }

        // 2. Age Category check
        if (tournament.getAgeCategory() != null) {
            if (homeTeam.getAgeCategory() != null && !Objects.equals(homeTeam.getAgeCategory(), tournament.getAgeCategory())) {
                String msg = String.format("Age category mismatch: Home team '%s' (%s) cannot play in %s tournament '%s'",
                        homeTeam.getCanonicalName(), homeTeam.getAgeCategory(), tournament.getAgeCategory(), tournament.getCanonicalName());
                anomalyReportingService.reportLeagueMismatch(createdBy, "AGE_CATEGORY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
            if (awayTeam.getAgeCategory() != null && !Objects.equals(awayTeam.getAgeCategory(), tournament.getAgeCategory())) {
                String msg = String.format("Age category mismatch: Away team '%s' (%s) cannot play in %s tournament '%s'",
                        awayTeam.getCanonicalName(), awayTeam.getAgeCategory(), tournament.getAgeCategory(), tournament.getCanonicalName());
                anomalyReportingService.reportLeagueMismatch(createdBy, "AGE_CATEGORY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
        }

        // 3. Country / Category check (Domestic leagues only)
        if (tournament.getCategory() != null && !Boolean.TRUE.equals(tournament.getIsCup())) {
            Integer tournCatId = tournament.getCategory().getId();
            if (homeTeam.getCategory() != null && !Objects.equals(homeTeam.getCategory().getId(), tournCatId)) {
                String msg = String.format("Country/Category mismatch: Home team '%s' (category=%d) does not belong to domestic league '%s' (category=%d)",
                        homeTeam.getCanonicalName(), homeTeam.getCategory().getId(), tournament.getCanonicalName(), tournCatId);
                anomalyReportingService.reportLeagueMismatch(createdBy, "COUNTRY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
            if (awayTeam.getCategory() != null && !Objects.equals(awayTeam.getCategory().getId(), tournCatId)) {
                String msg = String.format("Country/Category mismatch: Away team '%s' (category=%d) does not belong to domestic league '%s' (category=%d)",
                        awayTeam.getCanonicalName(), awayTeam.getCategory().getId(), tournament.getCanonicalName(), tournCatId);
                anomalyReportingService.reportLeagueMismatch(createdBy, "COUNTRY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
        }

        // 4. Tournament Roster check (for non-cup leagues with established participants from official catalog)
        if (!Boolean.TRUE.equals(tournament.getIsCup()) && !"SOFASCORE".equalsIgnoreCase(sourceOrigin) && tournament.getSofascoreId() != null) {
            long rosterCount = tournamentTeamRepository.countByTournamentIdAndIsActiveTrue(tournament.getId());
            if (rosterCount >= 16) {
                if (!tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), homeTeam.getId())) {
                    String msg = String.format("League roster violation: Home team '%s' (#%d) is not a registered participant of league '%s' (#%d)",
                            homeTeam.getCanonicalName(), homeTeam.getId(), tournament.getCanonicalName(), tournament.getId());
                    anomalyReportingService.reportLeagueMismatch(createdBy, "ROSTER_VIOLATION", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                    throw new IllegalArgumentException(msg);
                }
                if (!tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), awayTeam.getId())) {
                    String msg = String.format("League roster violation: Away team '%s' (#%d) is not a registered participant of league '%s' (#%d)",
                            awayTeam.getCanonicalName(), awayTeam.getId(), tournament.getCanonicalName(), tournament.getId());
                    anomalyReportingService.reportLeagueMismatch(createdBy, "ROSTER_VIOLATION", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        }
    }

    private void validateRequest(MatchResolutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("MatchResolutionRequest cannot be null");
        }
        if (isSyntheticPlaceholder(request.getHomeTeamName()) || isSyntheticPlaceholder(request.getAwayTeamName())) {
            throw new IllegalArgumentException("Synthetic placeholder match rejected: " + request.getHomeTeamName() + " vs " + request.getAwayTeamName());
        }
        if (request.getSportId() == null) {
            throw new IllegalArgumentException("Sport ID is mandatory");
        }
        if (request.getTournamentId() == null) {
            throw new IllegalArgumentException("Tournament ID is mandatory");
        }
        if (request.getHomeTeamId() == null || request.getAwayTeamId() == null) {
            throw new IllegalArgumentException("Home and Away Team IDs are mandatory");
        }
        if (Objects.equals(request.getHomeTeamId(), request.getAwayTeamId())) {
            throw new IllegalArgumentException("Home team and Away team cannot be identical (ID=" + request.getHomeTeamId() + ")");
        }
        if (request.getStartTimeEpochSec() == null || request.getStartTimeEpochSec() <= 0) {
            throw new IllegalArgumentException("Valid start time epoch is mandatory");
        }
    }

    private boolean isSyntheticPlaceholder(String name) {
        if (name == null || name.isBlank()) return false;
        String lower = name.trim().toLowerCase();
        return lower.equals("хозяева") || lower.equals("гости") || lower.equals("хозяева тура")
                || lower.equals("гости тура") || lower.equals("team 1") || lower.equals("team 2")
                || lower.equals("home") || lower.equals("away");
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
        // 1. Invalidate Ingestion Blueprint caches so subsequent calls pick up new match
        ingestionBlueprintService.invalidateAll();

        // 2. Broadcast via Kafka to running parsers and aggregator
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
        return CanonicalMatchHeaderDto.builder()
                .matchId(match.getId())
                .sportId(match.getSport().getId())
                .sportSlug(match.getSport().getSlug())
                .tournamentId(match.getTournament().getId())
                .tournamentName(match.getTournament().getCanonicalName())
                .homeTeamId(match.getHomeTeam().getId())
                .homeTeamName(match.getHomeTeam().getCanonicalName())
                .awayTeamId(match.getAwayTeam().getId())
                .awayTeamName(match.getAwayTeam().getCanonicalName())
                .startTimeEpochSec(match.getStartTime().toEpochSecond())
                .sofascoreId(match.getSofascoreId())
                .sourceOrigin(match.getSourceOrigin())
                .status(match.getStatus())
                .build();
    }

    public static OffsetDateTime toOffsetDateTime(Long epochTime) {
        if (epochTime == null || epochTime <= 0) return OffsetDateTime.now(ZoneOffset.UTC);
        Instant instant = (epochTime > 100_000_000_000L)
                ? Instant.ofEpochMilli(epochTime)
                : Instant.ofEpochSecond(epochTime);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
