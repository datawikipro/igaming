package pro.datawiki.igaming.mdm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.MatchResolutionRequest;
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer;
import pro.datawiki.igaming.mdm.domain.MdmMatch;
import pro.datawiki.igaming.mdm.domain.MdmSport;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTournament;
import pro.datawiki.igaming.mdm.domain.MdmTournamentTeam;
import pro.datawiki.igaming.dto.util.SimilarityUtils;
import pro.datawiki.igaming.mdm.domain.MdmExternalSystem;
import pro.datawiki.igaming.mdm.domain.MdmTeamIngestionMap;
import pro.datawiki.igaming.mdm.domain.MdmTournamentIngestionMap;
import pro.datawiki.igaming.mdm.repository.MdmExternalSystemRepository;
import pro.datawiki.igaming.mdm.repository.MdmMatchRepository;
import pro.datawiki.igaming.mdm.repository.MdmSportRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamIngestionMapRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentIngestionMapRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentTeamRepository;

import java.text.Normalizer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Authoritative single entry-point for resolving or creating canonical MDM matches.
 * Enforces composite sport integrity constraints at application and database level.
 * Handles both SofaScore Ground Truth ingestion and Bookmaker line creation.
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
    private final pro.datawiki.igaming.mdm.repository.MdmTeamTranslationRepository teamTranslationRepository;

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
            @Autowired(required = false) pro.datawiki.igaming.mdm.repository.MdmTeamTranslationRepository teamTranslationRepository
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
                kafkaTemplate, matchesUpsertedTopic, null, null, null, null);
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

        // 2. Resolve or Auto-create Home Team
        if (request.getHomeTeamId() == null) {
            resolveOrCreateTeam(request, true);
        }

        // 3. Resolve or Auto-create Away Team
        if (request.getAwayTeamId() == null) {
            resolveOrCreateTeam(request, false);
        }

        // 4. Resolve or Auto-create Tournament
        if (request.getTournamentId() == null) {
            resolveOrCreateTournament(request);
        }
    }

    private void resolveOrCreateTeam(MatchResolutionRequest request, boolean isHome) {
        Long sofaId = isHome ? request.getSofascoreHomeTeamId() : request.getSofascoreAwayTeamId();
        String teamName = isHome ? request.getHomeTeamName() : request.getAwayTeamName();
        Integer sportId = request.getSportId();

        if (sofaId != null) {
            Optional<MdmTeam> bySofa = teamRepository.findBySofascoreId(sofaId);
            if (bySofa.isPresent()) {
                if (isHome) request.setHomeTeamId(bySofa.get().getId());
                else request.setAwayTeamId(bySofa.get().getId());
                return;
            }
        }

        var parsed = teamName != null ? TeamSignatureNormalizer.parse(teamName) : null;
        String expectedGender = parsed != null ? parsed.gender().name() : "MALE";
        String expectedAge = (parsed != null && parsed.ageCategory() != TeamSignatureNormalizer.AgeCategory.MAIN)
                ? parsed.ageCategory().name() : "SENIOR";

        if (request.getTournamentId() != null) {
            tournamentRepository.findById(request.getTournamentId()).ifPresent(t -> {
                if (t.getGender() != null && !"MIXED".equalsIgnoreCase(t.getGender())) {
                    // Tournament gender takes precedence for team resolution in gender-restricted leagues
                }
            });
        }

        // Check external system ingestion map first
        if (request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank() && teamName != null && externalSystemRepository != null && teamIngestionMapRepository != null) {
            Optional<MdmExternalSystem> sysOpt = externalSystemRepository.findByCode(request.getCreatedBySource());
            if (sysOpt.isPresent()) {
                Optional<MdmTeamIngestionMap> mapOpt = teamIngestionMapRepository.findByExternalSystemIdAndRawKey(sysOpt.get().getId(), teamName);
                if (mapOpt.isPresent() && mapOpt.get().getCanonicalTeam() != null) {
                    MdmTeam mappedTeam = mapOpt.get().getCanonicalTeam();
                    if (Objects.equals(mappedTeam.getSport().getId(), sportId)) {
                        boolean genderMatch = mappedTeam.getGender() == null || mappedTeam.getGender().equalsIgnoreCase(expectedGender);
                        if (genderMatch) {
                            if (isHome) request.setHomeTeamId(mappedTeam.getId());
                            else request.setAwayTeamId(mappedTeam.getId());
                            return;
                        } else {
                            log.warn("⚠️ [MDM] Rejected mapped team #{} '{}' ({}) because expected gender is {}",
                                    mappedTeam.getId(), mappedTeam.getCanonicalName(), mappedTeam.getGender(), expectedGender);
                        }
                    }
                }
            }
        }

        // Check translation table (e.g. Russian team translations)
        if (teamName != null && sportId != null && teamTranslationRepository != null) {
            Optional<pro.datawiki.igaming.mdm.domain.MdmTeamTranslation> trans = teamTranslationRepository.findFirstByNameIgnoreCase(teamName);
            if (trans.isPresent() && trans.get().getTeam() != null && Objects.equals(trans.get().getTeam().getSport().getId(), sportId)) {
                MdmTeam team = trans.get().getTeam();
                boolean genderMatch = team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender);
                if (genderMatch) {
                    if (isHome) request.setHomeTeamId(team.getId());
                    else request.setAwayTeamId(team.getId());
                    saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                    return;
                }
            }
        }

        if (teamName != null && sportId != null) {
            String teamSlug = toSlug(teamName);
            Optional<MdmTeam> bySlug = teamRepository.findBySportIdAndSlug(sportId, teamSlug);
            if (bySlug.isPresent()) {
                MdmTeam team = bySlug.get();
                boolean genderMatch = team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender);
                if (genderMatch) {
                    if (sofaId != null && team.getSofascoreId() == null) {
                        team.setSofascoreId(sofaId);
                        team = teamRepository.save(team);
                    }
                    if (isHome) request.setHomeTeamId(team.getId());
                    else request.setAwayTeamId(team.getId());
                    return;
                }
            }

            String normSig = toSlug(parsed.coreClubName());
            Optional<MdmTeam> bySig = teamRepository.findBySportIdAndNormalizedSignatureAndAgeCategoryAndGender(
                    sportId, normSig, expectedAge, expectedGender);
            if (bySig.isPresent()) {
                MdmTeam team = bySig.get();
                if (sofaId != null && team.getSofascoreId() == null) {
                    team.setSofascoreId(sofaId);
                    team = teamRepository.save(team);
                }
                if (isHome) request.setHomeTeamId(team.getId());
                else request.setAwayTeamId(team.getId());
                return;
            }

            if (!normSig.equals(teamSlug)) {
                Optional<MdmTeam> byCoreSlug = teamRepository.findBySportIdAndSlug(sportId, normSig);
                if (byCoreSlug.isPresent()) {
                    MdmTeam team = byCoreSlug.get();
                    boolean genderMatch = team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender);
                    if (genderMatch) {
                        if (sofaId != null && team.getSofascoreId() == null) {
                            team.setSofascoreId(sofaId);
                            team = teamRepository.save(team);
                        }
                        if (isHome) request.setHomeTeamId(team.getId());
                        else request.setAwayTeamId(team.getId());
                        return;
                    }
                }
            }
        }

        // Auto-Creation for SofaScore and Clean Bookmaker Lines
        if (teamName != null && sportId != null) {
            MdmSport sport = sportRepository.findById(sportId).orElse(null);
            if (sport != null) {
                String normSig = toSlug(parsed != null ? parsed.coreClubName() : teamName);
                String slug = toSlug(teamName) + (sofaId != null ? "-" + sofaId : "");
                Optional<MdmTeam> existingSlug = teamRepository.findBySportIdAndSlug(sportId, slug);
                if (existingSlug.isPresent()) {
                    MdmTeam team = existingSlug.get();
                    if (sofaId != null && team.getSofascoreId() == null) {
                        team.setSofascoreId(sofaId);
                        team = teamRepository.save(team);
                    }
                    if (isHome) request.setHomeTeamId(team.getId());
                    else request.setAwayTeamId(team.getId());
                    saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                    return;
                }

                String origin = "SOFASCORE".equalsIgnoreCase(request.getSourceOrigin()) ? "SOFASCORE" : "NATIVE_LOCAL";

                MdmTeam newTeam = MdmTeam.builder()
                        .sport(sport)
                        .canonicalName(teamName)
                        .slug(slug)
                        .sofascoreId(sofaId)
                        .normalizedSignature(normSig)
                        .gender(expectedGender)
                        .ageCategory(expectedAge)
                        .sourceOrigin(origin)
                        .build();
                newTeam = teamRepository.save(newTeam);
                log.info("🌟 [MDM] Auto-created canonical team #{} '{}' (sport={}, sofaId={}) from {}",
                        newTeam.getId(), newTeam.getCanonicalName(), sportId, sofaId, origin);
                if (isHome) request.setHomeTeamId(newTeam.getId());
                else request.setAwayTeamId(newTeam.getId());
                saveTeamIngestionMap(request.getCreatedBySource(), teamName, newTeam);
            }
        }
    }

    private void resolveOrCreateTournament(MatchResolutionRequest request) {
        Long sofaId = request.getSofascoreTournamentId();
        String tournName = request.getTournamentName();
        Integer sportId = request.getSportId();

        if (sofaId != null) {
            Optional<MdmTournament> bySofa = tournamentRepository.findBySofascoreId(sofaId);
            if (bySofa.isPresent()) {
                request.setTournamentId(bySofa.get().getId());
                return;
            }
        }

        // Check external system ingestion map first
        if (request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank() && tournName != null && externalSystemRepository != null && tournamentIngestionMapRepository != null) {
            Optional<MdmExternalSystem> sysOpt = externalSystemRepository.findByCode(request.getCreatedBySource());
            if (sysOpt.isPresent()) {
                Optional<MdmTournamentIngestionMap> mapOpt = tournamentIngestionMapRepository.findByExternalSystemIdAndRawKey(sysOpt.get().getId(), tournName);
                if (mapOpt.isPresent() && mapOpt.get().getCanonicalTournament() != null) {
                    MdmTournament mappedTourn = mapOpt.get().getCanonicalTournament();
                    if (Objects.equals(mappedTourn.getSport().getId(), sportId)) {
                        request.setTournamentId(mappedTourn.getId());
                        return;
                    }
                }
            }
        }

        if (tournName != null && sportId != null) {
            String slug = toSlug(tournName);
            Optional<MdmTournament> bySlug = tournamentRepository.findFirstBySportIdAndSlug(sportId, slug);
            if (bySlug.isPresent()) {
                MdmTournament t = bySlug.get();
                if (sofaId != null && t.getSofascoreId() == null) {
                    t.setSofascoreId(sofaId);
                    t = tournamentRepository.save(t);
                }
                request.setTournamentId(t.getId());
                return;
            }
        }

        // Auto-Creation for SofaScore and Clean Bookmaker Lines
        if (tournName != null && sportId != null) {
            MdmSport sport = sportRepository.findById(sportId).orElse(null);
            if (sport != null) {
                String slug = toSlug(tournName) + (sofaId != null ? "-" + sofaId : "");
                Optional<MdmTournament> existingTourn = tournamentRepository.findFirstBySportIdAndSlug(sportId, slug);
                if (existingTourn.isPresent()) {
                    MdmTournament t = existingTourn.get();
                    if (sofaId != null && t.getSofascoreId() == null) {
                        t.setSofascoreId(sofaId);
                        t = tournamentRepository.save(t);
                    }
                    request.setTournamentId(t.getId());
                    saveTournamentIngestionMap(request.getCreatedBySource(), tournName, t);
                    return;
                }

                String origin = "SOFASCORE".equalsIgnoreCase(request.getSourceOrigin()) ? "SOFASCORE" : "NATIVE_LOCAL";

                MdmTournament newTourn = MdmTournament.builder()
                        .sport(sport)
                        .canonicalName(tournName)
                        .slug(slug)
                        .sofascoreId(sofaId)
                        .gender("MALE")
                        .ageCategory("SENIOR")
                        .sourceOrigin(origin)
                        .popularityRank(500)
                        .build();
                newTourn = tournamentRepository.save(newTourn);
                log.info("🌟 [MDM] Auto-created canonical tournament #{} '{}' (sport={}, sofaId={}) from {}",
                        newTourn.getId(), newTourn.getCanonicalName(), sportId, sofaId, origin);
                request.setTournamentId(newTourn.getId());
                saveTournamentIngestionMap(request.getCreatedBySource(), tournName, newTourn);
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

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) return "unknown";
        String latin = SimilarityUtils.transliterateCyrillic(input);
        String normalized = Normalizer.normalize(latin.isBlank() ? input : latin, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private void validateLeagueIntegrity(MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String sourceOrigin, String createdBy) {
        // 1. Gender check (e.g. MALE vs FEMALE)
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

        // 2. Age Category check (e.g. SENIOR vs U19/U21/RESERVE)
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

    private void registerTournamentParticipant(MdmTournament tournament, MdmTeam team) {
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
