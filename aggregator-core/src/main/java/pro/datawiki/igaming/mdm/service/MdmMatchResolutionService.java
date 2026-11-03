/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.kafka.core.KafkaTemplate
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 *  pro.datawiki.igaming.dto.CanonicalMatchHeaderDto
 *  pro.datawiki.igaming.dto.CanonicalTeamDto
 *  pro.datawiki.igaming.dto.CanonicalTournamentDto
 *  pro.datawiki.igaming.dto.MatchResolutionRequest
 *  pro.datawiki.igaming.dto.TeamResolutionRequest
 *  pro.datawiki.igaming.dto.TournamentResolutionRequest
 *  pro.datawiki.igaming.dto.signature.CyberMatchDetector
 *  pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer
 *  pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer$AgeCategory
 *  pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer$ParsedSignature
 *  pro.datawiki.igaming.dto.sport.Sport
 *  pro.datawiki.igaming.dto.util.SimilarityUtils
 */
package pro.datawiki.igaming.mdm.service;

import java.text.Normalizer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.CanonicalTeamDto;
import pro.datawiki.igaming.dto.CanonicalTournamentDto;
import pro.datawiki.igaming.dto.MatchResolutionRequest;
import pro.datawiki.igaming.dto.TeamResolutionRequest;
import pro.datawiki.igaming.dto.TournamentResolutionRequest;
import pro.datawiki.igaming.dto.signature.CyberMatchDetector;
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.util.SimilarityUtils;
import pro.datawiki.igaming.mdm.domain.MdmCategory;
import pro.datawiki.igaming.mdm.domain.MdmCountry;
import pro.datawiki.igaming.mdm.domain.MdmCountryIngestionMap;
import pro.datawiki.igaming.mdm.domain.MdmExternalSystem;
import pro.datawiki.igaming.mdm.domain.MdmMatch;
import pro.datawiki.igaming.mdm.domain.MdmSport;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTeamIngestionMap;
import pro.datawiki.igaming.mdm.domain.MdmTeamMergeMap;
import pro.datawiki.igaming.mdm.domain.MdmTeamTranslation;
import pro.datawiki.igaming.mdm.domain.MdmTournament;
import pro.datawiki.igaming.mdm.domain.MdmTournamentIngestionMap;
import pro.datawiki.igaming.mdm.domain.MdmTournamentTeam;
import pro.datawiki.igaming.mdm.domain.MdmTournamentTranslation;
import pro.datawiki.igaming.mdm.repository.MdmCategoryRepository;
import pro.datawiki.igaming.mdm.repository.MdmCountryIngestionMapRepository;
import pro.datawiki.igaming.mdm.repository.MdmCountryRepository;
import pro.datawiki.igaming.mdm.repository.MdmExternalSystemRepository;
import pro.datawiki.igaming.mdm.repository.MdmMatchRepository;
import pro.datawiki.igaming.mdm.repository.MdmSportRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamIngestionMapRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamMergeMapRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamTranslationRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentIngestionMapRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentTeamRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentTranslationRepository;
import pro.datawiki.igaming.mdm.service.AnomalyReportingService;
import pro.datawiki.igaming.mdm.service.IngestionBlueprintService;
import pro.datawiki.igaming.mdm.util.CountryNormalizer;
import pro.datawiki.igaming.mdm.util.TournamentStageNormalizer;

@Service
public class MdmMatchResolutionService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(MdmMatchResolutionService.class);
    private static final Set<String> AMBIGUOUS_GENERIC_LEAGUES = Set.of("premier league", "serie a", "serie b", "primera division", "primera divisiones", "super league", "superleague", "first division", "superliga", "division 1", "division 2", "segunda division", "segunda liga", "tercera division", "liga 1", "liga 2", "liga 3", "nacional", "liga nacional", "cup", "fa cup", "national cup", "league cup", "super cup", "supercopa", "super cupen", "league one", "league two", "championship", "bundesliga", "eredivisie", "allsvenskan", "ekstraklasa", "pro league", "\u043f\u0440\u0435\u043c\u044c\u0435\u0440-\u043b\u0438\u0433\u0430", "\u043f\u0440\u0435\u043c\u044c\u0435\u0440 \u043b\u0438\u0433\u0430", "\u043f\u0435\u0440\u0432\u0430\u044f \u043b\u0438\u0433\u0430", "\u0432\u0442\u043e\u0440\u0430\u044f \u043b\u0438\u0433\u0430", "\u0442\u0440\u0435\u0442\u044c\u044f \u043b\u0438\u0433\u0430", "\u0432\u044b\u0441\u0448\u0430\u044f \u043b\u0438\u0433\u0430", "\u043a\u0443\u0431\u043e\u043a", "\u043a\u0443\u0431\u043e\u043a \u0441\u0442\u0440\u0430\u043d\u044b", "\u043a\u0443\u0431\u043e\u043a \u043b\u0438\u0433\u0438", "\u043d\u0430\u0446\u0438\u043e\u043d\u0430\u043b\u044c\u043d\u044b\u0439 \u043a\u0443\u0431\u043e\u043a", "\u0441\u0443\u043f\u0435\u0440\u043a\u0443\u0431\u043e\u043a", "\u0441\u0443\u043f\u0435\u0440\u043b\u0438\u0433\u0430", "\u0447\u0435\u043c\u043f\u0438\u043e\u043d\u0430\u0442", "\u0441\u0435\u0440\u0438\u044f \u0430", "\u0441\u0435\u0440\u0438\u044f \u0432", "\u0441\u0435\u0440\u0438\u044f \u0431", "\u043f\u0440\u0438\u043c\u0435\u0440\u0430", "\u043f\u0440\u0438\u043c\u0435\u0440\u0430 \u0434\u0438\u0432\u0438\u0437\u0438\u043e\u043d", "\u0441\u0435\u0433\u0443\u043d\u0434\u0430", "\u043f\u0440\u043e \u043b\u0438\u0433\u0430", "\u0434\u0438\u0432\u0438\u0437\u0438\u043e\u043d 1", "\u0434\u0438\u0432\u0438\u0437\u0438\u043e\u043d 2");
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
    private final Map<Long, Long> localTeamToSofaMapCache = new ConcurrentHashMap<Long, Long>();
    private static final Pattern SYNTHETIC_LEAGUE_PATTERN = Pattern.compile("(?ui)^\\s*league\\s*\\d+\\s*$|.*\\b(?:fantasy|mythical|\u043c\u0438\u0444\u0438\u0447\u0435\u0441\u043a(?:\u0438\u0439|\u0430\u044f|\u043e\u0435|\u0438\u0435)?|simulated\\s*reality|\\bsrl\\b).*");
    private static final Pattern STATS_OR_OUTRIGHT_PATTERN = Pattern.compile("(?ui).*(?:\\((?:\u0443\u0433\u043b\u043e\u0432\u044b\u0435|\u0444\u043e\u043b\u044b|\u0436\u0435\u043b\u0442\u044b\u0435\\s*\u043a\u0430\u0440\u0442[\u0430-\u044f]*|\u0431\u0440\u043e\u0441\u043a\u0438\\s*\u0432\\s*\u0441\u0442\u0432\u043e\u0440|\u0434\u0432\u043e\u0439\u043d\u044b\u0435\\s*\u043e\u0448\u0438\u0431\u043a\u0438|\u044d\u0439\u0441\u044b|\u043e\u0444\u0441\u0430\u0439\u0434\u044b|\u0443\u0434\u0430\u0440\u044b\\s*\u043e\u0442\\s*\u0432\u043e\u0440\u043e\u0442|\u0432\u0431\u0440\u0430\u0441\u044b\u0432\u0430\u043d\u0438\u044f|\u0448\u0442\u0440\u0430\u0444\u043d\u043e\u0435\\s*\u0432\u0440\u0435\u043c\u044f|\u0443\u0434\u0430\u043b\u0435\u043d\u0438\u044f|\u043f\u043e\u0434\u0431\u043e\u0440\u044b|\u043f\u0435\u0440\u0435\u0434\u0430\u0447\u0438|\u0431\u043b\u043e\u043a[- ]?\u0448\u043e\u0442\u044b|3[- ]?\u043e\u0447\u043a\u043e\u0432\u044b\u0435)\\)|\\b(?:corners?|bookings?|fouls?|offsides?|shots\\s*on\\s*target|\u043f\u043e\u0431\u0435\u0434\u0438\u0442\u0435\u043b\u044c(?:\u043d\u0438\u0446\u0430)?|winner|\u043e\u0431\u043b\u0430\u0434\u0430\u0442\u0435\u043b\u044c\\s*\u043a\u0443\u0431\u043a\u0430|\u0432\u044b\u0445\u043e\u0434\\s*\u0432\\s*(?:\u0444\u0438\u043d\u0430\u043b|\u043f\u043e\u043b\u0443\u0444\u0438\u043d\u0430\u043b|\u043f\u043b\u0435\u0439[- ]?\u043e\u0444\u0444)|\u0447\u0435\u043c\u043f\u0438\u043e\u043d|\u0438\u0442\u043e\u0433\u0438|outright|futures)\\b).*");

    public Long remapTeamId(Long localId) {
        if (localId == null || this.teamMergeMapRepository == null) {
            return localId;
        }
        return this.localTeamToSofaMapCache.computeIfAbsent(localId, id -> this.teamMergeMapRepository.findByLocalId((Long)id).map(MdmTeamMergeMap::getSofaId).orElse((Long)id));
    }

    @Autowired
    public MdmMatchResolutionService(MdmMatchRepository matchRepository, MdmSportRepository sportRepository, MdmTournamentRepository tournamentRepository, MdmTeamRepository teamRepository, MdmTournamentTeamRepository tournamentTeamRepository, IngestionBlueprintService ingestionBlueprintService, AnomalyReportingService anomalyReportingService, @Autowired(required=false) KafkaTemplate<String, Object> kafkaTemplate, @Value(value="${app.kafka.topics.matches-upserted:mdm.matches.upserted}") String matchesUpsertedTopic, @Autowired(required=false) MdmExternalSystemRepository externalSystemRepository, @Autowired(required=false) MdmTeamIngestionMapRepository teamIngestionMapRepository, @Autowired(required=false) MdmTournamentIngestionMapRepository tournamentIngestionMapRepository, @Autowired(required=false) MdmTeamTranslationRepository teamTranslationRepository, @Autowired(required=false) MdmTournamentTranslationRepository tournamentTranslationRepository, @Autowired(required=false) MdmCategoryRepository categoryRepository, @Autowired(required=false) MdmCountryRepository countryRepository, @Autowired(required=false) MdmCountryIngestionMapRepository countryIngestionMapRepository, @Autowired(required=false) MdmTeamMergeMapRepository teamMergeMapRepository) {
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
    }

    public MdmMatchResolutionService(MdmMatchRepository matchRepository, MdmSportRepository sportRepository, MdmTournamentRepository tournamentRepository, MdmTeamRepository teamRepository, MdmTournamentTeamRepository tournamentTeamRepository, IngestionBlueprintService ingestionBlueprintService, AnomalyReportingService anomalyReportingService, KafkaTemplate<String, Object> kafkaTemplate, String matchesUpsertedTopic) {
        this(matchRepository, sportRepository, tournamentRepository, teamRepository, tournamentTeamRepository, ingestionBlueprintService, anomalyReportingService, kafkaTemplate, matchesUpsertedTopic, null, null, null, null, null, null, null, null, null);
    }

    @Transactional
    public CanonicalMatchHeaderDto findOrCreateMatch(MatchResolutionRequest request) {
        String createdBy;
        OffsetDateTime windowEnd;
        OffsetDateTime windowStart;
        Long awayId;
        Long homeId;
        Integer sportId;
        List<MdmMatch> candidates;
        CanonicalMatchHeaderDto anchored;
        Optional<MdmMatch> sofaMatch;
        if (request == null) {
            throw new IllegalArgumentException("MatchResolutionRequest cannot be null");
        }
        OffsetDateTime targetTime = MdmMatchResolutionService.toOffsetDateTime(request.getStartTimeEpochSec());
        this.dispatchTrueSport(request);
        if (this.teamMergeMapRepository != null) {
            if (request.getHomeTeamId() != null) {
                request.setHomeTeamId(this.remapTeamId(request.getHomeTeamId()));
            }
            if (request.getAwayTeamId() != null) {
                request.setAwayTeamId(this.remapTeamId(request.getAwayTeamId()));
            }
        }
        if (request.getSofascoreId() != null && (sofaMatch = this.matchRepository.findBySofascoreId(request.getSofascoreId())).isPresent()) {
            MdmMatch existing = sofaMatch.get();
            boolean changed = false;
            if (targetTime != null && Math.abs(existing.getStartTime().toEpochSecond() - targetTime.toEpochSecond()) > 300L) {
                existing.setStartTime(targetTime);
                changed = true;
            }
            if (changed) {
                existing = (MdmMatch)this.matchRepository.save(existing);
                this.broadcastMatchUpserted(existing);
            }
            if (request.getSofascoreHomeTeamId() != null && existing.getHomeTeam() != null && existing.getHomeTeam().getSofascoreId() == null) {
                this.enrichTeamSofascoreId(existing.getHomeTeam(), request.getSofascoreHomeTeamId());
            }
            if (request.getSofascoreAwayTeamId() != null && existing.getAwayTeam() != null && existing.getAwayTeam().getSofascoreId() == null) {
                this.enrichTeamSofascoreId(existing.getAwayTeam(), request.getSofascoreAwayTeamId());
            }
            return MdmMatchResolutionService.toHeaderDto(existing);
        }
        if (request.getSportId() == null && request.getSportSlug() != null) {
            this.sportRepository.findBySlug(request.getSportSlug().trim().toLowerCase()).ifPresent(s -> request.setSportId(s.getId()));
        }
        if (request.getSportId() != null && targetTime != null && request.getHomeTeamName() != null && request.getAwayTeamName() != null && (anchored = this.tryResolveByTemporalMatchAnchor(request, targetTime)) != null) {
            return anchored;
        }
        this.resolveEntities(request);
        if (this.teamMergeMapRepository != null) {
            if (request.getHomeTeamId() != null) {
                request.setHomeTeamId(this.remapTeamId(request.getHomeTeamId()));
            }
            if (request.getAwayTeamId() != null) {
                request.setAwayTeamId(this.remapTeamId(request.getAwayTeamId()));
            }
        }
        this.validateRequest(request);
        if (this.isSyntheticLeague(request)) {
            String src = request.getCreatedBySource() != null ? request.getCreatedBySource() : "UNKNOWN";
            log.warn("\u26a0\ufe0f [MDM Synthetic Anomaly] Potential synthetic/fantasy/SRL league: '{}' for match '{} vs {}' (system={})", new Object[]{request.getTournamentName(), request.getHomeTeamName(), request.getAwayTeamName(), src});
            if (this.anomalyReportingService != null) {
                this.anomalyReportingService.reportLeagueMismatch(src, "SUSPICIOUS_SYNTHETIC_LEAGUE", request.getTournamentId(), request.getTournamentName(), request.getHomeTeamId(), request.getHomeTeamName(), "Potential synthetic/fantasy/SRL league: " + request.getTournamentName());
            }
        }
        if (!(candidates = this.matchRepository.findCandidateMatches(sportId = request.getSportId(), homeId = request.getHomeTeamId(), awayId = request.getAwayTeamId(), windowStart = targetTime.minusHours(18L), windowEnd = targetTime.plusHours(18L))).isEmpty()) {
            MdmMatch matched = this.findClosestCandidate(candidates, targetTime);
            if (matched.getHomeTeam() != null && matched.getAwayTeam() != null) {
                this.validateMatchGenderAndAge(matched.getTournament(), matched.getHomeTeam(), matched.getAwayTeam(), request.getCreatedBySource());
            }
            boolean updated = false;
            if (targetTime != null && "SCHEDULED".equals(matched.getStatus()) && Math.abs(matched.getStartTime().toEpochSecond() - targetTime.toEpochSecond()) > 300L) {
                log.info("\u23f0 [MDM] Updating start time for match #{} ({}-{}) from {} to {} (source={})", new Object[]{matched.getId(), matched.getHomeTeam().getCanonicalName(), matched.getAwayTeam().getCanonicalName(), matched.getStartTime(), targetTime, request.getCreatedBySource()});
                matched.setStartTime(targetTime);
                updated = true;
            }
            if (request.getSofascoreId() != null && matched.getSofascoreId() == null) {
                log.info("\ud83d\udc8e [MDM] Promoting match #{} ({}) with SofaScore ID {}", new Object[]{matched.getId(), matched.getSourceOrigin(), request.getSofascoreId()});
                matched.setSofascoreId(request.getSofascoreId());
                matched.setSourceOrigin("SOFASCORE");
                if (targetTime != null) {
                    matched.setStartTime(targetTime);
                }
                if (request.getTournamentId() != null) {
                    this.tournamentRepository.findById(request.getTournamentId()).filter(t -> Objects.equals(t.getSport().getId(), sportId)).ifPresent(matched::setTournament);
                }
                if (request.getSofascoreHomeTeamId() != null && matched.getHomeTeam() != null && matched.getHomeTeam().getSofascoreId() == null) {
                    this.enrichTeamSofascoreId(matched.getHomeTeam(), request.getSofascoreHomeTeamId());
                }
                if (request.getSofascoreAwayTeamId() != null && matched.getAwayTeam() != null && matched.getAwayTeam().getSofascoreId() == null) {
                    this.enrichTeamSofascoreId(matched.getAwayTeam(), request.getSofascoreAwayTeamId());
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
                int parsed = CanonicalMatchHeaderDto.parseBestOfFromStrings((String[])new String[]{matched.getStage(), matched.getRound(), matched.getTournament() != null ? matched.getTournament().getCanonicalName() : null});
                matched.setBestOf(parsed);
                updated = true;
            }
            if (updated) {
                matched = (MdmMatch)this.matchRepository.save(matched);
                this.broadcastMatchUpserted(matched);
            }
            return MdmMatchResolutionService.toHeaderDto(matched);
        }
        MdmSport sport = (MdmSport)this.sportRepository.findById(sportId).orElseThrow(() -> new IllegalArgumentException("Sport not found with ID: " + sportId));
        MdmTournament tournament = (MdmTournament)this.tournamentRepository.findById(request.getTournamentId()).orElseThrow(() -> new IllegalArgumentException("Tournament not found with ID: " + request.getTournamentId()));
        MdmTeam homeTeam = (MdmTeam)this.teamRepository.findById(homeId).orElseThrow(() -> new IllegalArgumentException("Home team not found with ID: " + homeId));
        MdmTeam awayTeam = (MdmTeam)this.teamRepository.findById(awayId).orElseThrow(() -> new IllegalArgumentException("Away team not found with ID: " + awayId));
        String string = createdBy = request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank() ? request.getCreatedBySource() : "system";
        if (!Objects.equals(tournament.getSport().getId(), sportId)) {
            String msg = String.format("Integrity violation: Tournament '%s' (sport=%d) does not match requested sport %d", tournament.getCanonicalName(), tournament.getSport().getId(), sportId);
            this.anomalyReportingService.reportLeagueMismatch(createdBy, "SPORT_MISMATCH", tournament.getId(), tournament.getCanonicalName(), null, null, msg);
            throw new IllegalArgumentException(msg);
        }
        if (!Objects.equals(homeTeam.getSport().getId(), sportId)) {
            String msg = String.format("Integrity violation: Home Team '%s' (sport=%d) does not match requested sport %d", homeTeam.getCanonicalName(), homeTeam.getSport().getId(), sportId);
            this.anomalyReportingService.reportLeagueMismatch(createdBy, "SPORT_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
            throw new IllegalArgumentException(msg);
        }
        if (!Objects.equals(awayTeam.getSport().getId(), sportId)) {
            String msg = String.format("Integrity violation: Away Team '%s' (sport=%d) does not match requested sport %d", awayTeam.getCanonicalName(), awayTeam.getSport().getId(), sportId);
            this.anomalyReportingService.reportLeagueMismatch(createdBy, "SPORT_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
            throw new IllegalArgumentException(msg);
        }
        String origin = MdmMatchResolutionService.sanitizeMatchOrigin(request.getSourceOrigin(), request.getSofascoreId());
        this.validateLeagueIntegrity(tournament, homeTeam, awayTeam, origin, createdBy);
        int effectiveBestOf = request.getBestOf() != null && request.getBestOf() > 0 ? request.getBestOf() : CanonicalMatchHeaderDto.parseBestOfFromStrings((String[])new String[]{request.getStage(), request.getRound(), tournament.getCanonicalName()});
        MdmMatch newMatch = MdmMatch.builder().sport(sport).tournament(tournament).homeTeam(homeTeam).awayTeam(awayTeam).startTime(targetTime).sofascoreId(request.getSofascoreId()).sourceOrigin(origin).createdBySource(createdBy).status("SCHEDULED").stage(request.getStage()).round(request.getRound()).bestOf(effectiveBestOf).build();
        MdmMatch saved = (MdmMatch)this.matchRepository.save(newMatch);
        log.info("\u2728 [MDM] Created canonical match #{}: sport={} ('{}'), tourn='{}', {} vs {}, start={}, origin={}/{}", new Object[]{saved.getId(), sport.getId(), sport.getSlug(), tournament.getCanonicalName(), homeTeam.getCanonicalName(), awayTeam.getCanonicalName(), saved.getStartTime(), saved.getSourceOrigin(), saved.getCreatedBySource()});
        this.registerTournamentParticipant(tournament, homeTeam);
        this.registerTournamentParticipant(tournament, awayTeam);
        this.broadcastMatchUpserted(saved);
        return MdmMatchResolutionService.toHeaderDto(saved);
    }

    @Transactional
    public CanonicalMatchHeaderDto cancelMatch(Long matchId, String reason) {
        MdmMatch match = this.matchRepository.findByIdWithDetails(matchId).orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + matchId));
        log.warn("\ud83d\udeab [MDM] Cancelling match #{}: {} vs {} (reason: {})", new Object[]{matchId, match.getHomeTeam().getCanonicalName(), match.getAwayTeam().getCanonicalName(), reason});
        match.setStatus("CANCELLED");
        match = (MdmMatch)this.matchRepository.save(match);
        this.broadcastMatchUpserted(match);
        return MdmMatchResolutionService.toHeaderDto(match);
    }

    @Transactional
    public CanonicalMatchHeaderDto promoteGroundTruth(Long matchId, Long sofascoreId, String sourceOrigin, String reason) {
        MdmMatch match = this.matchRepository.findByIdWithDetails(matchId).orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + matchId));
        log.info("\ud83d\udc8e [MDM] Promoting match #{}: {} vs {} to ground truth (sofascoreId={}, reason: {})", new Object[]{matchId, match.getHomeTeam().getCanonicalName(), match.getAwayTeam().getCanonicalName(), sofascoreId, reason});
        match.setStatus("SCHEDULED");
        if (sourceOrigin != null && !sourceOrigin.isBlank()) {
            match.setSourceOrigin(sourceOrigin);
        } else {
            match.setSourceOrigin("SOFASCORE");
        }
        if (sofascoreId != null) {
            match.setSofascoreId(sofascoreId);
        }
        match = (MdmMatch)this.matchRepository.save(match);
        this.broadcastMatchUpserted(match);
        return MdmMatchResolutionService.toHeaderDto(match);
    }

    private void resolveEntities(MatchResolutionRequest request) {
        if (request == null) {
            return;
        }
        if (request.getSportId() == null && request.getSportSlug() != null) {
            this.sportRepository.findBySlug(request.getSportSlug().trim().toLowerCase()).ifPresent(s -> request.setSportId(s.getId()));
        }
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
        Integer resolvedCountryId = request.getCountryId();
        if (request.getTournamentId() == null) {
            TournamentResolutionRequest tReq = TournamentResolutionRequest.builder().sportId(request.getSportId()).sportSlug(request.getSportSlug()).sofascoreTournamentId(request.getSofascoreTournamentId()).tournamentName(request.getTournamentName()).categoryName(request.getCategoryName()).countryName(request.getCountryName()).countryId(request.getCountryId()).rawCategoryKey(request.getRawCategoryKey()).rawKey(request.getRawTournamentKey()).sourceId(request.getTournamentSourceId()).isInternational(request.getIsInternational()).createdBySource(request.getCreatedBySource()).sourceOrigin(request.getSourceOrigin()).build();
            CanonicalTournamentDto tDto = this.findOrCreateTournament(tReq);
            request.setTournamentId(tDto.getTournamentId());
            if (resolvedCountryId == null && tDto.getCountryId() != null) {
                resolvedCountryId = tDto.getCountryId();
            }
        }
        if (request.getHomeTeamId() == null) {
            TeamResolutionRequest homeReq = TeamResolutionRequest.builder().sportId(request.getSportId()).sportSlug(request.getSportSlug()).tournamentId(request.getTournamentId()).tournamentName(request.getTournamentName()).sofascoreTeamId(request.getSofascoreHomeTeamId()).teamName(request.getHomeTeamName()).rawKey(request.getRawHomeTeamKey()).sourceId(request.getHomeTeamSourceId()).countryName(request.getCountryName()).countryId(resolvedCountryId).categoryName(request.getCategoryName()).rawCategoryKey(request.getRawCategoryKey()).createdBySource(request.getCreatedBySource()).sourceOrigin(request.getSourceOrigin()).build();
            CanonicalTeamDto homeDto = this.findOrCreateTeam(homeReq);
            request.setHomeTeamId(homeDto.getTeamId());
        }
        if (request.getAwayTeamId() == null) {
            TeamResolutionRequest awayReq = TeamResolutionRequest.builder().sportId(request.getSportId()).sportSlug(request.getSportSlug()).tournamentId(request.getTournamentId()).tournamentName(request.getTournamentName()).sofascoreTeamId(request.getSofascoreAwayTeamId()).teamName(request.getAwayTeamName()).rawKey(request.getRawAwayTeamKey()).sourceId(request.getAwayTeamSourceId()).countryName(request.getCountryName()).countryId(resolvedCountryId).categoryName(request.getCategoryName()).rawCategoryKey(request.getRawCategoryKey()).createdBySource(request.getCreatedBySource()).sourceOrigin(request.getSourceOrigin()).build();
            CanonicalTeamDto awayDto = this.findOrCreateTeam(awayReq);
            request.setAwayTeamId(awayDto.getTeamId());
        }
    }

    public MdmCategory resolveCategory(Integer sportId, String countryName, String categoryName, Boolean isInternational) {
        String slug;
        Optional<MdmCategory> bySlug;
        if (this.categoryRepository == null || sportId == null) {
            return null;
        }
        String target = countryName != null && !countryName.isBlank() ? countryName.trim() : (categoryName != null ? categoryName.trim() : null);
        CountryNormalizer.CountryInfo countryInfo = CountryNormalizer.normalize(target, isInternational);
        if (countryInfo != null) {
            Optional<MdmCategory> bySlug2 = this.categoryRepository.findFirstBySportIdAndSlugIgnoreCase(sportId, countryInfo.slug());
            if (bySlug2.isPresent()) {
                return bySlug2.get();
            }
            MdmSport sport = this.sportRepository.findById(sportId).orElse(null);
            if (sport != null) {
                try {
                    MdmCategory newCat = MdmCategory.builder().sport(sport).slug(countryInfo.slug()).code(countryInfo.code()).sourceOrigin("NATIVE_LOCAL").build();
                    return (MdmCategory)this.categoryRepository.save(newCat);
                }
                catch (Exception e) {
                    log.warn("Failed to create category for slug {}: {}", (Object)countryInfo.slug(), (Object)e.getMessage());
                    return this.categoryRepository.findFirstBySportIdAndSlugIgnoreCase(sportId, countryInfo.slug()).orElse(null);
                }
            }
        }
        if (categoryName != null && !categoryName.isBlank() && (bySlug = this.categoryRepository.findFirstBySportIdAndSlugIgnoreCase(sportId, slug = MdmMatchResolutionService.toSlug(categoryName))).isPresent()) {
            return bySlug.get();
        }
        return null;
    }

    @Transactional
    public CanonicalTournamentDto findOrCreateTournament(TournamentResolutionRequest request) {
        boolean isCup;
        String detectedAge;
        String detectedGender;
        Optional<MdmTournamentTranslation> trans;
        Optional<MdmExternalSystem> sysOpt;
        Optional<MdmTournament> bySofa;
        TournamentStageNormalizer.StageResult stageRes;
        Integer sportId;
        if (request == null) {
            throw new IllegalArgumentException("TournamentResolutionRequest cannot be null");
        }
        if (request.getSportId() == null && request.getSportSlug() != null) {
            this.sportRepository.findBySlug(request.getSportSlug().trim().toLowerCase()).ifPresent(s -> request.setSportId(s.getId()));
        }
        if ((sportId = request.getSportId()) == null) {
            throw new IllegalArgumentException("Sport ID is mandatory");
        }
        Long sofaId = request.getSofascoreTournamentId();
        String tournName = request.getTournamentName();
        if (tournName != null && (stageRes = TournamentStageNormalizer.extract(tournName = tournName.trim())).cleanTournamentName() != null && !stageRes.cleanTournamentName().isBlank()) {
            tournName = stageRes.cleanTournamentName().trim();
            request.setTournamentName(tournName);
        }
        String countryName = request.getCountryName() != null ? request.getCountryName().trim() : null;
        Boolean isIntl = request.getIsInternational();
        if (sofaId != null && (bySofa = this.tournamentRepository.findBySofascoreId(sofaId)).isPresent()) {
            MdmTournament t2 = bySofa.get();
            this.enrichTournamentCountry(t2, countryName);
            return MdmMatchResolutionService.toTournamentDto(t2);
        }
        MdmCountry resolvedCountry = null;
        if (request.getCountryId() != null && this.countryRepository != null) {
            resolvedCountry = this.countryRepository.findById(request.getCountryId()).orElse(null);
        }
        if (resolvedCountry == null) {
            resolvedCountry = this.resolveCountryEntity(request.getCreatedBySource(), request.getRawCategoryKey(), countryName, request.getCategoryName(), isIntl);
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
        MdmCategory category = this.resolveCategory(sportId, countryCtx, request.getCategoryName(), isIntl);
        if (category != null && countryCtx == null) {
            countryCtx = category.getSlug();
        }
        if (request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank() && this.externalSystemRepository != null && this.tournamentIngestionMapRepository != null && (sysOpt = this.externalSystemRepository.findByCode(request.getCreatedBySource())).isPresent()) {
            MdmTournament mappedTourn;
            Optional<MdmTournamentIngestionMap> mapOpt;
            Optional<MdmTournamentIngestionMap> mapByRaw;
            MdmTournament mappedTourn2;
            Optional<MdmTournamentIngestionMap> mapBySource;
            Integer sysId = sysOpt.get().getId();
            if (request.getSourceId() != null && !request.getSourceId().isBlank() && (mapBySource = this.tournamentIngestionMapRepository.findFirstByExternalSystemIdAndSourceId(sysId, request.getSourceId().trim())).isPresent() && mapBySource.get().getCanonicalTournament() != null && Objects.equals((mappedTourn2 = mapBySource.get().getCanonicalTournament()).getSport().getId(), sportId)) {
                this.enrichTournamentCountry(mappedTourn2, countryCtx);
                return MdmMatchResolutionService.toTournamentDto(mappedTourn2);
            }
            if (request.getRawKey() != null && !request.getRawKey().isBlank() && (mapByRaw = this.tournamentIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, request.getRawKey().trim())).isPresent() && mapByRaw.get().getCanonicalTournament() != null && Objects.equals((mappedTourn2 = mapByRaw.get().getCanonicalTournament()).getSport().getId(), sportId)) {
                this.enrichTournamentCountry(mappedTourn2, countryCtx);
                return MdmMatchResolutionService.toTournamentDto(mappedTourn2);
            }
            String compositeKey = countryCtx != null && tournName != null ? countryCtx + " - " + tournName : null;
            mapOpt = compositeKey != null ? this.tournamentIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, compositeKey) : Optional.empty();
            if (mapOpt.isEmpty() && tournName != null) {
                mapOpt = this.tournamentIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, tournName);
            }
            if (mapOpt.isPresent() && ((MdmTournamentIngestionMap)mapOpt.get()).getCanonicalTournament() != null && Objects.equals((mappedTourn = ((MdmTournamentIngestionMap)mapOpt.get()).getCanonicalTournament()).getSport().getId(), sportId)) {
                this.enrichTournamentCountry(mappedTourn, countryCtx);
                return MdmMatchResolutionService.toTournamentDto(mappedTourn);
            }
        }
        if (tournName != null && this.tournamentTranslationRepository != null && (trans = this.tournamentTranslationRepository.findFirstByNameIgnoreCase(tournName)).isPresent() && trans.get().getTournament() != null && Objects.equals(trans.get().getTournament().getSport().getId(), sportId)) {
            MdmTournament t3 = trans.get().getTournament();
            if (MdmMatchResolutionService.isAmbiguousLeagueName(tournName) && countryCtx != null && t3.getCountryName() != null && !countryCtx.equalsIgnoreCase(t3.getCountryName())) {
                log.debug("[MDM] Skipping translation match '{}' (country mismatch: {} vs {})", new Object[]{tournName, countryCtx, t3.getCountryName()});
            } else {
                this.enrichTournamentCountry(t3, countryCtx);
                this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), t3);
                return MdmMatchResolutionService.toTournamentDto(t3);
            }
        }
        if (tournName != null) {
            Optional<MdmTournament> byNameAndCountry;
            MdmTournament t4;
            String slug;
            MdmTournament t5;
            boolean isAmbiguous = MdmMatchResolutionService.isAmbiguousLeagueName(tournName);
            if (resolvedCountryId != null) {
                Optional<MdmTournament> byCountryAndName = this.tournamentRepository.findFirstBySportIdAndCountryIdAndCanonicalNameIgnoreCase(sportId, resolvedCountryId, tournName);
                if (byCountryAndName.isPresent()) {
                    t5 = byCountryAndName.get();
                    if (sofaId != null && t5.getSofascoreId() == null) {
                        t5.setSofascoreId(sofaId);
                        t5 = (MdmTournament)this.tournamentRepository.save(t5);
                    }
                    this.enrichTournamentCountry(t5, countryCtx, resolvedCountry);
                    this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), t5);
                    return MdmMatchResolutionService.toTournamentDto(t5);
                }
                slug = MdmMatchResolutionService.toSlug(tournName);
                Optional<MdmTournament> byCountryAndSlug = this.tournamentRepository.findFirstBySportIdAndCountryIdAndSlug(sportId, resolvedCountryId, slug);
                if (byCountryAndSlug.isPresent()) {
                    t4 = byCountryAndSlug.get();
                    if (sofaId != null && t4.getSofascoreId() == null) {
                        t4.setSofascoreId(sofaId);
                        t4 = (MdmTournament)this.tournamentRepository.save(t4);
                    }
                    this.enrichTournamentCountry(t4, countryCtx, resolvedCountry);
                    this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), t4);
                    return MdmMatchResolutionService.toTournamentDto(t4);
                }
            }
            if (category != null) {
                Optional<MdmTournament> byCatAndName = this.tournamentRepository.findFirstBySportIdAndCategoryIdAndCanonicalNameIgnoreCase(sportId, category.getId(), tournName);
                if (byCatAndName.isPresent()) {
                    t5 = byCatAndName.get();
                    if (sofaId != null && t5.getSofascoreId() == null) {
                        t5.setSofascoreId(sofaId);
                        t5 = (MdmTournament)this.tournamentRepository.save(t5);
                    }
                    this.enrichTournamentCountry(t5, countryCtx, resolvedCountry);
                    this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), t5);
                    return MdmMatchResolutionService.toTournamentDto(t5);
                }
                slug = MdmMatchResolutionService.toSlug(tournName);
                Optional<MdmTournament> byCatAndSlug = this.tournamentRepository.findBySportIdAndCategoryIdAndSlug(sportId, category.getId(), slug);
                if (byCatAndSlug.isPresent()) {
                    t4 = byCatAndSlug.get();
                    if (sofaId != null && t4.getSofascoreId() == null) {
                        t4.setSofascoreId(sofaId);
                        t4 = (MdmTournament)this.tournamentRepository.save(t4);
                    }
                    this.enrichTournamentCountry(t4, countryCtx, resolvedCountry);
                    this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), t4);
                    return MdmMatchResolutionService.toTournamentDto(t4);
                }
            }
            if (countryCtx != null && (byNameAndCountry = this.tournamentRepository.findFirstBySportIdAndNameAndCountry(sportId, tournName, countryCtx)).isPresent()) {
                t5 = byNameAndCountry.get();
                if (sofaId != null && t5.getSofascoreId() == null) {
                    t5.setSofascoreId(sofaId);
                    t5 = (MdmTournament)this.tournamentRepository.save(t5);
                }
                this.enrichTournamentCountry(t5, countryCtx, resolvedCountry);
                this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), t5);
                return MdmMatchResolutionService.toTournamentDto(t5);
            }
            if (!isAmbiguous && resolvedCountryId == null && countryCtx == null) {
                Optional<MdmTournament> byName = this.tournamentRepository.findFirstBySportIdAndCanonicalNameIgnoreCase(sportId, tournName);
                if (byName.isPresent()) {
                    t5 = byName.get();
                    if (sofaId != null && t5.getSofascoreId() == null) {
                        t5.setSofascoreId(sofaId);
                        t5 = (MdmTournament)this.tournamentRepository.save(t5);
                    }
                    this.enrichTournamentCountry(t5, countryCtx, resolvedCountry);
                    if (t5.getCategory() == null && category != null) {
                        t5.setCategory(category);
                        t5 = (MdmTournament)this.tournamentRepository.save(t5);
                    }
                    this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), t5);
                    return MdmMatchResolutionService.toTournamentDto(t5);
                }
                slug = MdmMatchResolutionService.toSlug(tournName);
                Optional<MdmTournament> bySlug = this.tournamentRepository.findFirstBySportIdAndSlug(sportId, slug);
                if (bySlug.isPresent()) {
                    t4 = bySlug.get();
                    if (sofaId != null && t4.getSofascoreId() == null) {
                        t4.setSofascoreId(sofaId);
                        t4 = (MdmTournament)this.tournamentRepository.save(t4);
                    }
                    this.enrichTournamentCountry(t4, countryCtx, resolvedCountry);
                    if (t4.getCategory() == null && category != null) {
                        t4.setCategory(category);
                        t4 = (MdmTournament)this.tournamentRepository.save(t4);
                    }
                    this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), t4);
                    return MdmMatchResolutionService.toTournamentDto(t4);
                }
            }
        }
        if (tournName != null && !tournName.isBlank()) {
            String detectedAge2;
            String detectedGender2;
            String lowerTourn = tournName.toLowerCase();
            String string = detectedGender2 = request.getGender() != null ? request.getGender().toUpperCase() : "MALE";
            if (request.getGender() == null && (lowerTourn.contains("\u0436\u0435\u043d\u0449\u0438\u043d") || lowerTourn.contains("women") || lowerTourn.contains("female") || lowerTourn.contains("damas") || lowerTourn.contains("dames") || lowerTourn.contains("frauen"))) {
                detectedGender2 = "FEMALE";
            }
            String string2 = detectedAge2 = request.getAgeCategory() != null ? request.getAgeCategory().toUpperCase() : "SENIOR";
            if (request.getAgeCategory() == null) {
                if (lowerTourn.contains("23") && (lowerTourn.contains("u23") || lowerTourn.contains("u-23") || lowerTourn.contains("\u0434\u043e 23") || lowerTourn.contains("23 \u043b\u0435\u0442") || lowerTourn.contains("23-"))) {
                    detectedAge2 = "U23";
                } else if (lowerTourn.contains("21") && (lowerTourn.contains("u21") || lowerTourn.contains("u-21") || lowerTourn.contains("\u0434\u043e 21") || lowerTourn.contains("21 \u043b\u0435\u0442") || lowerTourn.contains("21-"))) {
                    detectedAge2 = "U21";
                } else if (lowerTourn.contains("20") && (lowerTourn.contains("u20") || lowerTourn.contains("u-20") || lowerTourn.contains("\u0434\u043e 20") || lowerTourn.contains("20 \u043b\u0435\u0442") || lowerTourn.contains("20-"))) {
                    detectedAge2 = "U20";
                } else if (lowerTourn.contains("19") && (lowerTourn.contains("u19") || lowerTourn.contains("u-19") || lowerTourn.contains("\u0434\u043e 19") || lowerTourn.contains("19 \u043b\u0435\u0442") || lowerTourn.contains("19-"))) {
                    detectedAge2 = "U19";
                } else if (lowerTourn.contains("17") && (lowerTourn.contains("u17") || lowerTourn.contains("u-17") || lowerTourn.contains("\u0434\u043e 17") || lowerTourn.contains("17 \u043b\u0435\u0442") || lowerTourn.contains("17-"))) {
                    detectedAge2 = "U17";
                } else if (lowerTourn.contains("16") && (lowerTourn.contains("u16") || lowerTourn.contains("u-16") || lowerTourn.contains("\u0434\u043e 16") || lowerTourn.contains("16 \u043b\u0435\u0442") || lowerTourn.contains("16-"))) {
                    detectedAge2 = "U16";
                } else if (lowerTourn.contains("\u043c\u043e\u043b\u043e\u0434\u0435\u0436") || lowerTourn.contains("\u044e\u043d\u043e\u0448") || lowerTourn.contains("youth") || lowerTourn.contains("primavera")) {
                    detectedAge2 = "U21";
                } else if (lowerTourn.contains("\u0440\u0435\u0437\u0435\u0440\u0432") || lowerTourn.contains("reserve")) {
                    detectedAge2 = "RESERVE";
                }
            }
            List<MdmTournament> candidateTourns = null;
            if (resolvedCountryId != null) {
                candidateTourns = this.tournamentRepository.findBySportIdAndCountryId(sportId, resolvedCountryId);
            } else if (countryCtx != null) {
                String targetCountry = countryCtx;
                candidateTourns = this.tournamentRepository.findBySportId(sportId).stream().filter(t -> targetCountry.equalsIgnoreCase(t.getCountryName()) || t.getCategory() != null && targetCountry.equalsIgnoreCase(t.getCategory().getSlug())).toList();
            } else if (!MdmMatchResolutionService.isAmbiguousLeagueName(tournName)) {
                candidateTourns = this.tournamentRepository.findBySportId(sportId);
            }
            if (candidateTourns != null && !candidateTourns.isEmpty()) {
                for (MdmTournament cand : candidateTourns) {
                    boolean genderMatch = cand.getGender() == null || cand.getGender().equalsIgnoreCase(detectedGender2);
                    boolean ageMatch = MdmMatchResolutionService.isAgeCompatible(cand.getAgeCategory(), detectedAge2);
                    if (!genderMatch || !ageMatch || !SimilarityUtils.areTournamentsSimilar((String)cand.getCanonicalName(), (String)tournName)) continue;
                    log.info("\ud83c\udfaf [MDM] Fuzzy matched incoming tournament '{}' to existing canonical tournament #{} '{}'", new Object[]{tournName, cand.getId(), cand.getCanonicalName()});
                    if (sofaId != null && cand.getSofascoreId() == null) {
                        cand.setSofascoreId(sofaId);
                        cand = (MdmTournament)this.tournamentRepository.save(cand);
                    }
                    this.enrichTournamentCountry(cand, countryCtx, resolvedCountry);
                    this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), cand);
                    return MdmMatchResolutionService.toTournamentDto(cand);
                }
            }
        }
        if (tournName == null || tournName.isBlank()) {
            throw new IllegalArgumentException("Cannot resolve or create tournament without tournamentName or sofascoreTournamentId");
        }
        MdmSport sport = (MdmSport)this.sportRepository.findById(sportId).orElseThrow(() -> new IllegalArgumentException("Sport not found with ID: " + sportId));
        boolean isAmbiguous = MdmMatchResolutionService.isAmbiguousLeagueName(tournName);
        Object canonicalName = isAmbiguous && countryCtx != null ? countryCtx + ". " + tournName : tournName;
        String slug = MdmMatchResolutionService.toSlug((String)canonicalName) + (String)(sofaId != null ? "-" + sofaId : "");
        Optional<MdmTournament> existingTourn = this.tournamentRepository.findFirstBySportIdAndSlug(sportId, slug);
        if (existingTourn.isPresent()) {
            MdmTournament t6 = existingTourn.get();
            if (sofaId != null && t6.getSofascoreId() == null) {
                t6.setSofascoreId(sofaId);
                t6 = (MdmTournament)this.tournamentRepository.save(t6);
            }
            this.enrichTournamentCountry(t6, countryCtx);
            this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), t6);
            return MdmMatchResolutionService.toTournamentDto(t6);
        }
        String origin = MdmMatchResolutionService.sanitizeEntityOrigin(request.getSourceOrigin(), sofaId);
        String lowerTourn = tournName.toLowerCase();
        String string = detectedGender = request.getGender() != null ? request.getGender().toUpperCase() : "MALE";
        if (request.getGender() == null && (lowerTourn.contains("\u0436\u0435\u043d\u0449\u0438\u043d") || lowerTourn.contains("women") || lowerTourn.contains("female") || lowerTourn.contains("damas") || lowerTourn.contains("dames") || lowerTourn.contains("frauen"))) {
            detectedGender = "FEMALE";
        }
        String string3 = detectedAge = request.getAgeCategory() != null ? request.getAgeCategory().toUpperCase() : "SENIOR";
        if (request.getAgeCategory() == null) {
            if (lowerTourn.contains("23") && (lowerTourn.contains("u23") || lowerTourn.contains("u-23") || lowerTourn.contains("\u0434\u043e 23") || lowerTourn.contains("23 \u043b\u0435\u0442") || lowerTourn.contains("23-"))) {
                detectedAge = "U23";
            } else if (lowerTourn.contains("21") && (lowerTourn.contains("u21") || lowerTourn.contains("u-21") || lowerTourn.contains("\u0434\u043e 21") || lowerTourn.contains("21 \u043b\u0435\u0442") || lowerTourn.contains("21-"))) {
                detectedAge = "U21";
            } else if (lowerTourn.contains("20") && (lowerTourn.contains("u20") || lowerTourn.contains("u-20") || lowerTourn.contains("\u0434\u043e 20") || lowerTourn.contains("20 \u043b\u0435\u0442") || lowerTourn.contains("20-"))) {
                detectedAge = "U20";
            } else if (lowerTourn.contains("19") && (lowerTourn.contains("u19") || lowerTourn.contains("u-19") || lowerTourn.contains("\u0434\u043e 19") || lowerTourn.contains("19 \u043b\u0435\u0442") || lowerTourn.contains("19-"))) {
                detectedAge = "U19";
            } else if (lowerTourn.contains("17") && (lowerTourn.contains("u17") || lowerTourn.contains("u-17") || lowerTourn.contains("\u0434\u043e 17") || lowerTourn.contains("17 \u043b\u0435\u0442") || lowerTourn.contains("17-"))) {
                detectedAge = "U17";
            } else if (lowerTourn.contains("16") && (lowerTourn.contains("u16") || lowerTourn.contains("u-16") || lowerTourn.contains("\u0434\u043e 16") || lowerTourn.contains("16 \u043b\u0435\u0442") || lowerTourn.contains("16-"))) {
                detectedAge = "U16";
            } else if (lowerTourn.contains("\u043c\u043e\u043b\u043e\u0434\u0435\u0436") || lowerTourn.contains("\u044e\u043d\u043e\u0448") || lowerTourn.contains("youth") || lowerTourn.contains("primavera")) {
                detectedAge = "U21";
            } else if (lowerTourn.contains("\u0440\u0435\u0437\u0435\u0440\u0432") || lowerTourn.contains("reserve")) {
                detectedAge = "RESERVE";
            }
        }
        boolean bl = isCup = Boolean.TRUE.equals(request.getIsCup()) || lowerTourn.contains("cup") || lowerTourn.contains("\u043a\u0443\u0431\u043e\u043a") || lowerTourn.contains("trophy") || lowerTourn.contains("copa") || lowerTourn.contains("pokal");
        if (resolvedCountry == null) {
            resolvedCountry = this.resolveCountryEntity(countryCtx, isIntl);
        }
        MdmTournament newTourn = MdmTournament.builder().sport(sport).category(category).canonicalName((String)canonicalName).slug(slug).sofascoreId(sofaId).gender(detectedGender).ageCategory(detectedAge).isCup(isCup).countryName(countryCtx).country(resolvedCountry).sourceOrigin(origin).popularityRank(500).build();
        newTourn = (MdmTournament)this.tournamentRepository.save(newTourn);
        log.info("\ud83c\udf1f [MDM] Auto-created canonical tournament #{} '{}' (sport={}, country={}, sofaId={}) from {}", new Object[]{newTourn.getId(), newTourn.getCanonicalName(), sportId, countryCtx, sofaId, origin});
        this.saveTournamentIngestionMap(request.getCreatedBySource(), MdmMatchResolutionService.buildIngestionKey(tournName, countryCtx), newTourn);
        return MdmMatchResolutionService.toTournamentDto(newTourn);
    }

    @Transactional
    public CanonicalTeamDto findOrCreateTeam(TeamResolutionRequest request) {
        String effectiveCountry;
        MdmTeam team;
        Optional<MdmTeamTranslation> trans;
        List<MdmTeam> rosterTeams;
        Optional<MdmExternalSystem> sysOpt;
        String expectedAge;
        String expectedGender;
        TeamSignatureNormalizer.ParsedSignature parsed;
        Integer teamCountryId;
        String countryName;
        Optional<MdmTeam> bySofa;
        Integer sportId;
        if (request == null) {
            throw new IllegalArgumentException("TeamResolutionRequest cannot be null");
        }
        if (request.getSportId() == null && request.getSportSlug() != null) {
            this.sportRepository.findBySlug(request.getSportSlug().trim().toLowerCase()).ifPresent(s -> request.setSportId(s.getId()));
        }
        if ((sportId = request.getSportId()) == null) {
            throw new IllegalArgumentException("Sport ID is mandatory");
        }
        Long sofaId = request.getSofascoreTeamId();
        String teamName = request.getTeamName();
        if (teamName != null) {
            teamName = teamName.trim();
        }
        if (sofaId != null && (bySofa = this.teamRepository.findBySofascoreId(sofaId)).isPresent()) {
            MdmTeam team2 = bySofa.get();
            this.enrichTeamCountry(team2, request.getCountryName());
            return MdmMatchResolutionService.toTeamDto(team2);
        }
        MdmTournament contextTournament = null;
        if (request.getTournamentId() != null) {
            contextTournament = this.tournamentRepository.findById(request.getTournamentId()).orElse(null);
        }
        String tournamentContext = contextTournament != null ? contextTournament.getCanonicalName() : request.getTournamentName();
        MdmCountry resolvedTeamCountry = null;
        if (request.getCountryId() != null && this.countryRepository != null) {
            resolvedTeamCountry = this.countryRepository.findById(request.getCountryId()).orElse(null);
        }
        if (resolvedTeamCountry == null && contextTournament != null && contextTournament.getCountry() != null) {
            resolvedTeamCountry = contextTournament.getCountry();
        }
        if ((countryName = request.getCountryName()) == null && resolvedTeamCountry != null) {
            countryName = resolvedTeamCountry.getCanonicalName();
        } else if (countryName == null && contextTournament != null && contextTournament.getCountryName() != null) {
            countryName = contextTournament.getCountryName();
        }
        if (resolvedTeamCountry == null) {
            resolvedTeamCountry = this.resolveCountryEntity(request.getCreatedBySource(), request.getRawCategoryKey(), countryName, request.getCategoryName(), false);
        }
        Integer n = teamCountryId = resolvedTeamCountry != null ? resolvedTeamCountry.getId() : null;
        if (countryName == null && resolvedTeamCountry != null) {
            countryName = resolvedTeamCountry.getCanonicalName();
        }
        MdmCategory category = null;
        if (countryName != null || request.getCategoryName() != null) {
            category = this.resolveCategory(sportId, countryName, request.getCategoryName(), false);
        }
        if (category == null && contextTournament != null && contextTournament.getCategory() != null) {
            category = contextTournament.getCategory();
        }
        TeamSignatureNormalizer.ParsedSignature parsedSignature = parsed = teamName != null ? TeamSignatureNormalizer.parse((String)teamName, (String)tournamentContext, null) : null;
        expectedGender = request.getGender() != null ? request.getGender().toUpperCase() : (parsed != null ? parsed.gender().name() : "MALE");
        expectedAge = request.getAgeCategory() != null ? request.getAgeCategory().toUpperCase() : (parsed != null && parsed.ageCategory() != TeamSignatureNormalizer.AgeCategory.MAIN ? parsed.ageCategory().name() : "SENIOR");
        if (contextTournament != null) {
            if (contextTournament.getGender() != null && !"MIXED".equalsIgnoreCase(contextTournament.getGender()) && request.getGender() == null) {
                expectedGender = contextTournament.getGender().toUpperCase();
            }
            if (contextTournament.getAgeCategory() != null && !"SENIOR".equalsIgnoreCase(contextTournament.getAgeCategory()) && request.getAgeCategory() == null) {
                expectedAge = contextTournament.getAgeCategory().toUpperCase();
            }
        }
        if (request.getCreatedBySource() != null && !request.getCreatedBySource().isBlank() && this.externalSystemRepository != null && this.teamIngestionMapRepository != null && (sysOpt = this.externalSystemRepository.findByCode(request.getCreatedBySource())).isPresent()) {
            Object mapOpt;
            Optional<MdmTeamIngestionMap> mapByRaw;
            boolean ageMatch;
            boolean genderMatch;
            MdmTeam mappedTeam;
            Optional<MdmTeamIngestionMap> mapBySource;
            Integer sysId = sysOpt.get().getId();
            if (request.getSourceId() != null && !request.getSourceId().isBlank() && (mapBySource = this.teamIngestionMapRepository.findFirstByExternalSystemIdAndSourceId(sysId, request.getSourceId().trim())).isPresent() && mapBySource.get().getCanonicalTeam() != null && Objects.equals((mappedTeam = mapBySource.get().getCanonicalTeam()).getSport().getId(), sportId)) {
                genderMatch = mappedTeam.getGender() == null || mappedTeam.getGender().equalsIgnoreCase(expectedGender);
                ageMatch = MdmMatchResolutionService.isAgeCompatible(mappedTeam.getAgeCategory(), expectedAge);
                if (genderMatch && ageMatch) {
                    if (contextTournament != null) {
                        this.registerTournamentParticipant(contextTournament, mappedTeam);
                    }
                    this.enrichTeamCountry(mappedTeam, countryName);
                    return this.finalizeTeamResolution(mappedTeam, sofaId);
                }
                log.warn("\u26a0\ufe0f [MDM] Rejected source-mapped team #{} '{}' (gender={}, age={}) because expected is (gender={}, age={})", new Object[]{mappedTeam.getId(), mappedTeam.getCanonicalName(), mappedTeam.getGender(), mappedTeam.getAgeCategory(), expectedGender, expectedAge});
            }
            if (request.getRawKey() != null && !request.getRawKey().isBlank() && (mapByRaw = this.teamIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, request.getRawKey().trim())).isPresent() && mapByRaw.get().getCanonicalTeam() != null && Objects.equals((mappedTeam = mapByRaw.get().getCanonicalTeam()).getSport().getId(), sportId)) {
                genderMatch = mappedTeam.getGender() == null || mappedTeam.getGender().equalsIgnoreCase(expectedGender);
                ageMatch = MdmMatchResolutionService.isAgeCompatible(mappedTeam.getAgeCategory(), expectedAge);
                if (genderMatch && ageMatch) {
                    if (contextTournament != null) {
                        this.registerTournamentParticipant(contextTournament, mappedTeam);
                    }
                    this.enrichTeamCountry(mappedTeam, countryName);
                    return this.finalizeTeamResolution(mappedTeam, sofaId);
                }
                log.warn("\u26a0\ufe0f [MDM] Rejected rawKey-mapped team #{} '{}' (gender={}, age={}) because expected is (gender={}, age={})", new Object[]{mappedTeam.getId(), mappedTeam.getCanonicalName(), mappedTeam.getGender(), mappedTeam.getAgeCategory(), expectedGender, expectedAge});
            }
            if (teamName != null && ((Optional)(mapOpt = this.teamIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, teamName))).isPresent() && ((MdmTeamIngestionMap)((Optional)mapOpt).get()).getCanonicalTeam() != null && Objects.equals((mappedTeam = ((MdmTeamIngestionMap)((Optional)mapOpt).get()).getCanonicalTeam()).getSport().getId(), sportId)) {
                genderMatch = mappedTeam.getGender() == null || mappedTeam.getGender().equalsIgnoreCase(expectedGender);
                ageMatch = MdmMatchResolutionService.isAgeCompatible(mappedTeam.getAgeCategory(), expectedAge);
                if (genderMatch && ageMatch) {
                    if (contextTournament != null) {
                        this.registerTournamentParticipant(contextTournament, mappedTeam);
                    }
                    this.enrichTeamCountry(mappedTeam, countryName);
                    return this.finalizeTeamResolution(mappedTeam, sofaId);
                }
                log.warn("\u26a0\ufe0f [MDM] Rejected mapped team #{} '{}' (gender={}, age={}) because expected is (gender={}, age={})", new Object[]{mappedTeam.getId(), mappedTeam.getCanonicalName(), mappedTeam.getGender(), mappedTeam.getAgeCategory(), expectedGender, expectedAge});
            }
        }
        if (contextTournament != null && teamName != null && this.tournamentTeamRepository != null && (rosterTeams = this.tournamentTeamRepository.findActiveTeamsByTournamentId(contextTournament.getId())) != null && !rosterTeams.isEmpty()) {
            String rawClean = MdmMatchResolutionService.toSlug(teamName).replaceAll("(?i)\\b(u\\d+|reserve|women|fc|sc|cd|cf|sad|19\\d\\d|20\\d\\d)\\b", "").replaceAll("[^a-z]", "");
            for (MdmTeam candidate : rosterTeams) {
                String candClean;
                boolean genderOk;
                if (!Objects.equals(candidate.getSport().getId(), sportId)) continue;
                boolean ageOk = MdmMatchResolutionService.isAgeCompatible(candidate.getAgeCategory(), expectedAge);
                boolean bl = genderOk = candidate.getGender() == null || candidate.getGender().equalsIgnoreCase(expectedGender);
                if (!ageOk || !genderOk || contextTournament.getCountry() != null && candidate.getCountry() != null && !Boolean.TRUE.equals(contextTournament.getIsCup()) && !this.isInternationalTournament(contextTournament) && !this.isCountryCompatible(contextTournament, candidate)) continue;
                boolean match = SimilarityUtils.areTeamsSimilar((String)teamName, (String)candidate.getCanonicalName(), (String)tournamentContext, (String)contextTournament.getCanonicalName());
                if (!(match || rawClean.isBlank() || (candClean = MdmMatchResolutionService.toSlug(candidate.getCanonicalName()).replaceAll("(?i)\\b(u\\d+|reserve|women|fc|sc|cd|cf|sad|19\\d\\d|20\\d\\d)\\b", "").replaceAll("[^a-z]", "")).isBlank())) {
                    boolean isRawGeneric = SimilarityUtils.GENERIC_CLUB_WORDS.contains(rawClean);
                    boolean isCandGeneric = SimilarityUtils.GENERIC_CLUB_WORDS.contains(candClean);
                    if (!isRawGeneric && !isCandGeneric) {
                        boolean bl2 = match = candClean.equals(rawClean) || rawClean.length() >= 6 && candClean.contains(rawClean) || candClean.length() >= 6 && rawClean.contains(candClean);
                        if (!match) {
                            String rawPhonetic;
                            String candPhonetic = candClean.replace('s', 'z');
                            match = candPhonetic.equals(rawPhonetic = rawClean.replace('s', 'z')) || rawPhonetic.length() >= 6 && candPhonetic.contains(rawPhonetic) || candPhonetic.length() >= 6 && rawPhonetic.contains(candPhonetic);
                        }
                    } else {
                        match = candClean.equals(rawClean);
                    }
                }
                if (!match) continue;
                log.info("\ud83c\udfaf [MDM] Resolved team '{}' to tournament participant #{} '{}' in tournament #{} '{}'", new Object[]{teamName, candidate.getId(), candidate.getCanonicalName(), contextTournament.getId(), contextTournament.getCanonicalName()});
                this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, candidate);
                this.enrichTeamCountry(candidate, countryName);
                return this.finalizeTeamResolution(candidate, sofaId);
            }
        }
        if (teamName != null && this.teamTranslationRepository != null && (trans = this.teamTranslationRepository.findFirstByNameIgnoreCase(teamName)).isPresent() && trans.get().getTeam() != null && Objects.equals(trans.get().getTeam().getSport().getId(), sportId)) {
            team = trans.get().getTeam();
            boolean genderMatch = team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender);
            boolean ageMatch = MdmMatchResolutionService.isAgeCompatible(team.getAgeCategory(), expectedAge);
            if (genderMatch && ageMatch) {
                if (contextTournament != null) {
                    this.registerTournamentParticipant(contextTournament, team);
                }
                this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                this.enrichTeamCountry(team, countryName);
                return this.finalizeTeamResolution(team, sofaId);
            }
        }
        if (teamName != null) {
            Optional<MdmTeam> byCoreSlug;
            String normSig;
            Optional<MdmTeam> bySig;
            Optional<MdmTeam> byCountry;
            String teamSlug;
            if (teamCountryId != null) {
                MdmTeam team3;
                Optional<MdmTeam> byCountryAndName = this.teamRepository.findFirstBySportIdAndCountryIdAndCanonicalNameIgnoreCase(sportId, teamCountryId, teamName);
                if (byCountryAndName.isPresent() && MdmMatchResolutionService.isAgeCompatible((team = byCountryAndName.get()).getAgeCategory(), expectedAge) && (team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender))) {
                    if (contextTournament != null) {
                        this.registerTournamentParticipant(contextTournament, team);
                    }
                    this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                    this.enrichTeamCountry(team, countryName, resolvedTeamCountry);
                    return this.finalizeTeamResolution(team, sofaId);
                }
                teamSlug = MdmMatchResolutionService.toSlug(teamName);
                Optional<MdmTeam> byCountrySlug = this.teamRepository.findFirstBySportIdAndCountryIdAndSlug(sportId, teamCountryId, teamSlug);
                if (byCountrySlug.isPresent() && MdmMatchResolutionService.isAgeCompatible((team3 = byCountrySlug.get()).getAgeCategory(), expectedAge) && (team3.getGender() == null || team3.getGender().equalsIgnoreCase(expectedGender))) {
                    if (contextTournament != null) {
                        this.registerTournamentParticipant(contextTournament, team3);
                    }
                    this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, team3);
                    this.enrichTeamCountry(team3, countryName, resolvedTeamCountry);
                    return this.finalizeTeamResolution(team3, sofaId);
                }
            }
            if (category != null) {
                MdmTeam team4;
                Optional<MdmTeam> byCatAndName = this.teamRepository.findFirstBySportIdAndCategoryIdAndCanonicalNameIgnoreCase(sportId, category.getId(), teamName);
                if (byCatAndName.isPresent() && MdmMatchResolutionService.isAgeCompatible((team = byCatAndName.get()).getAgeCategory(), expectedAge) && (team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender))) {
                    if (contextTournament != null) {
                        this.registerTournamentParticipant(contextTournament, team);
                    }
                    this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                    this.enrichTeamCountry(team, countryName, resolvedTeamCountry);
                    return this.finalizeTeamResolution(team, sofaId);
                }
                teamSlug = MdmMatchResolutionService.toSlug(teamName);
                Optional<MdmTeam> byCatAndSlug = this.teamRepository.findFirstBySportIdAndCategoryIdAndSlug(sportId, category.getId(), teamSlug);
                if (byCatAndSlug.isPresent() && MdmMatchResolutionService.isAgeCompatible((team4 = byCatAndSlug.get()).getAgeCategory(), expectedAge) && (team4.getGender() == null || team4.getGender().equalsIgnoreCase(expectedGender))) {
                    if (contextTournament != null) {
                        this.registerTournamentParticipant(contextTournament, team4);
                    }
                    this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, team4);
                    this.enrichTeamCountry(team4, countryName, resolvedTeamCountry);
                    return this.finalizeTeamResolution(team4, sofaId);
                }
            }
            if (countryName != null && !countryName.isBlank() && (byCountry = this.teamRepository.findFirstBySportIdAndNameAndCountry(sportId, teamName, countryName)).isPresent() && MdmMatchResolutionService.isAgeCompatible((team = byCountry.get()).getAgeCategory(), expectedAge) && (team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender))) {
                if (contextTournament != null) {
                    this.registerTournamentParticipant(contextTournament, team);
                }
                this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                return this.finalizeTeamResolution(team, sofaId);
            }
            String teamSlug2 = MdmMatchResolutionService.toSlug(teamName);
            Optional<MdmTeam> bySlug = this.teamRepository.findBySportIdAndSlug(sportId, teamSlug2);
            if (bySlug.isPresent()) {
                MdmTeam team5 = (MdmTeam)bySlug.get();
                boolean genderMatch = team5.getGender() == null || team5.getGender().equalsIgnoreCase(expectedGender);
                boolean ageMatch = MdmMatchResolutionService.isAgeCompatible(team5.getAgeCategory(), expectedAge);
                if (genderMatch && ageMatch) {
                    if (contextTournament != null) {
                        this.registerTournamentParticipant(contextTournament, team5);
                    }
                    this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, team5);
                    this.enrichTeamCountry(team5, countryName);
                    if (team5.getCategory() == null && category != null) {
                        team5.setCategory(category);
                        team5 = (MdmTeam)this.teamRepository.save(team5);
                    }
                    return this.finalizeTeamResolution(team5, sofaId);
                }
            }
            if ((bySig = this.teamRepository.findFirstBySportIdAndNormalizedSignatureAndAgeCategoryAndGender(sportId, normSig = MdmMatchResolutionService.toSlug(parsed != null ? parsed.coreClubName() : teamName), expectedAge, expectedGender)).isPresent()) {
                MdmTeam team6 = bySig.get();
                if (contextTournament != null) {
                    this.registerTournamentParticipant(contextTournament, team6);
                }
                this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, team6);
                this.enrichTeamCountry(team6, countryName);
                if (team6.getCategory() == null && category != null) {
                    team6.setCategory(category);
                    team6 = (MdmTeam)this.teamRepository.save(team6);
                }
                return this.finalizeTeamResolution(team6, sofaId);
            }
            if (!normSig.equals(teamSlug2) && (byCoreSlug = this.teamRepository.findBySportIdAndSlug(sportId, normSig)).isPresent()) {
                MdmTeam team7 = byCoreSlug.get();
                boolean genderMatch = team7.getGender() == null || team7.getGender().equalsIgnoreCase(expectedGender);
                boolean ageMatch = MdmMatchResolutionService.isAgeCompatible(team7.getAgeCategory(), expectedAge);
                if (genderMatch && ageMatch) {
                    if (contextTournament != null) {
                        this.registerTournamentParticipant(contextTournament, team7);
                    }
                    this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, team7);
                    this.enrichTeamCountry(team7, countryName);
                    if (team7.getCategory() == null && category != null) {
                        team7.setCategory(category);
                        team7 = (MdmTeam)this.teamRepository.save(team7);
                    }
                    return this.finalizeTeamResolution(team7, sofaId);
                }
            }
        }
        if (teamName != null) {
            List<MdmTeam> candidates = Collections.emptyList();
            if (teamCountryId != null) {
                candidates = this.teamRepository.findBySportIdAndCountryId(sportId, teamCountryId);
            } else if (category != null) {
                candidates = this.teamRepository.findBySportIdAndCategoryId(sportId, category.getId());
            }
            if (candidates != null && !candidates.isEmpty()) {
                for (MdmTeam cand : candidates) {
                    boolean genderMatch = cand.getGender() == null || cand.getGender().equalsIgnoreCase(expectedGender);
                    boolean ageMatch = MdmMatchResolutionService.isAgeCompatible(cand.getAgeCategory(), expectedAge);
                    if (!genderMatch || !ageMatch || !SimilarityUtils.areTeamsSimilar((String)teamName, (String)cand.getCanonicalName(), (String)tournamentContext, (String)(contextTournament != null ? contextTournament.getCanonicalName() : null))) continue;
                    log.info("\ud83c\udfaf [MDM-COUNTRY-FUZZY] Matched incoming team '{}' to existing canonical team #{} '{}' (sport={}, countryId={})", new Object[]{teamName, cand.getId(), cand.getCanonicalName(), sportId, teamCountryId});
                    if (contextTournament != null) {
                        this.registerTournamentParticipant(contextTournament, cand);
                    }
                    this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, cand);
                    this.enrichTeamCountry(cand, countryName, resolvedTeamCountry);
                    return this.finalizeTeamResolution(cand, sofaId);
                }
            }
        }
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Cannot resolve or create team without teamName or sofascoreTeamId");
        }
        MdmSport sport = (MdmSport)this.sportRepository.findById(sportId).orElseThrow(() -> new IllegalArgumentException("Sport not found with ID: " + sportId));
        String normSig = MdmMatchResolutionService.toSlug(parsed != null ? parsed.coreClubName() : teamName);
        String slug = MdmMatchResolutionService.toSlug(teamName) + (String)(sofaId != null ? "-" + sofaId : "");
        Optional<MdmTeam> existingSlug = this.teamRepository.findBySportIdAndSlug(sportId, slug);
        if (existingSlug.isPresent()) {
            MdmTeam team8 = existingSlug.get();
            if (contextTournament != null) {
                this.registerTournamentParticipant(contextTournament, team8);
            }
            this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, team8);
            this.enrichTeamCountry(team8, countryName);
            return this.finalizeTeamResolution(team8, sofaId);
        }
        String origin = MdmMatchResolutionService.sanitizeEntityOrigin(request.getSourceOrigin(), sofaId);
        effectiveCountry = countryName != null ? countryName : (category != null ? category.getSlug() : null);
        if (resolvedTeamCountry == null && effectiveCountry != null) {
            resolvedTeamCountry = this.resolveCountryEntity(effectiveCountry, false);
        }
        MdmTeam newTeam = MdmTeam.builder().sport(sport).category(category).canonicalName(teamName).slug(slug).sofascoreId(sofaId).normalizedSignature(normSig).gender(expectedGender).ageCategory(expectedAge).countryName(effectiveCountry).country(resolvedTeamCountry).sourceOrigin(origin).build();
        newTeam = (MdmTeam)this.teamRepository.save(newTeam);
        log.info("\ud83c\udf1f [MDM] Auto-created canonical team #{} '{}' (sport={}, country={}, category={}, sofaId={}) from {}", new Object[]{newTeam.getId(), newTeam.getCanonicalName(), sportId, countryName, category != null ? category.getSlug() : "none", sofaId, origin});
        if (contextTournament != null) {
            this.registerTournamentParticipant(contextTournament, newTeam);
        }
        this.saveTeamIngestionMap(request.getCreatedBySource(), teamName, newTeam);
        return MdmMatchResolutionService.toTeamDto(newTeam);
    }

    @Transactional(readOnly=true)
    public Optional<CanonicalTournamentDto> getTournamentById(Long id) {
        return this.tournamentRepository.findById(id).map(MdmMatchResolutionService::toTournamentDto);
    }

    @Transactional(readOnly=true)
    public Optional<CanonicalTeamDto> getTeamById(Long id) {
        return this.teamRepository.findById(id).map(MdmMatchResolutionService::toTeamDto);
    }

    public static CanonicalTournamentDto toTournamentDto(MdmTournament t) {
        if (t == null) {
            return null;
        }
        return CanonicalTournamentDto.builder().tournamentId(t.getId()).sportId(t.getSport().getId()).sportSlug(t.getSport().getSlug()).categoryId(t.getCategory() != null ? t.getCategory().getId() : null).categorySlug(t.getCategory() != null ? t.getCategory().getSlug() : null).countryId(t.getCountry() != null ? t.getCountry().getId() : null).countryName(t.getCountryName()).canonicalName(t.getCanonicalName()).slug(t.getSlug()).ageCategory(t.getAgeCategory()).gender(t.getGender()).isCup(t.getIsCup()).isInternational(Boolean.valueOf(t.getCategory() != null && "international".equalsIgnoreCase(t.getCategory().getSlug()))).sofascoreId(t.getSofascoreId()).sourceOrigin(t.getSourceOrigin()).build();
    }

    public static CanonicalTeamDto toTeamDto(MdmTeam team) {
        if (team == null) {
            return null;
        }
        return CanonicalTeamDto.builder().teamId(team.getId()).sportId(team.getSport().getId()).sportSlug(team.getSport().getSlug()).categoryId(team.getCategory() != null ? team.getCategory().getId() : null).categorySlug(team.getCategory() != null ? team.getCategory().getSlug() : null).countryId(team.getCountry() != null ? team.getCountry().getId() : null).countryName(team.getCountryName()).canonicalName(team.getCanonicalName()).slug(team.getSlug()).ageCategory(team.getAgeCategory()).gender(team.getGender()).sofascoreId(team.getSofascoreId()).sourceOrigin(team.getSourceOrigin()).build();
    }

    public void registerTournamentParticipant(MdmTournament tournament, MdmTeam team) {
        if (tournament == null || team == null || this.tournamentTeamRepository == null) {
            return;
        }
        if (!(tournament.getCountry() == null || team.getCountry() == null || Boolean.TRUE.equals(tournament.getIsCup()) || this.isInternationalTournament(tournament) || this.isCountryCompatible(tournament, team))) {
            log.warn("\ud83d\udeab [MDM-ROSTER] Refusing to register foreign team '{}' (#{}, country={}) into domestic tournament '{}' (#{}, country={})", new Object[]{team.getCanonicalName(), team.getId(), team.getCountry().getCanonicalName(), tournament.getCanonicalName(), tournament.getId(), tournament.getCountry().getCanonicalName()});
            return;
        }
        if (!this.tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), team.getId())) {
            try {
                MdmTournamentTeam participant = MdmTournamentTeam.builder().tournament(tournament).team(team).sport(tournament.getSport()).isActive(true).build();
                this.tournamentTeamRepository.save(participant);
                log.info("\ud83d\udee1\ufe0f [MDM-ROSTER] Registered team '{}' (#{}) as participant of tournament '{}' (#{})", new Object[]{team.getCanonicalName(), team.getId(), tournament.getCanonicalName(), tournament.getId()});
            }
            catch (Exception e) {
                log.debug("Participant already registered or conflict: {}", (Object)e.getMessage());
            }
        }
    }

    private static boolean isAmbiguousLeagueName(String name) {
        if (name == null) {
            return false;
        }
        return AMBIGUOUS_GENERIC_LEAGUES.contains(name.trim().toLowerCase());
    }

    private static String buildIngestionKey(String tournName, String countryCtx) {
        if (countryCtx != null && !countryCtx.isBlank() && MdmMatchResolutionService.isAmbiguousLeagueName(tournName)) {
            return countryCtx + " - " + tournName;
        }
        return tournName;
    }

    public MdmCountry resolveCountryEntity(String systemCode, String rawCategoryKey, String countryName, String categoryName, Boolean isInternational) {
        String target;
        Optional<MdmExternalSystem> sysOpt;
        if (this.countryRepository == null) {
            return null;
        }
        if (Boolean.TRUE.equals(isInternational)) {
            return null;
        }
        if (systemCode != null && !systemCode.isBlank() && this.countryIngestionMapRepository != null && this.externalSystemRepository != null && (sysOpt = this.externalSystemRepository.findByCode(systemCode)).isPresent()) {
            Optional<MdmCountryIngestionMap> map;
            Integer sysId = sysOpt.get().getId();
            if (rawCategoryKey != null && !rawCategoryKey.isBlank() && (map = this.countryIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, rawCategoryKey.trim())).isPresent() && map.get().getCanonicalCountry() != null) {
                return map.get().getCanonicalCountry();
            }
            if (countryName != null && !countryName.isBlank() && (map = this.countryIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, countryName.trim())).isPresent() && map.get().getCanonicalCountry() != null) {
                return map.get().getCanonicalCountry();
            }
            if (categoryName != null && !categoryName.isBlank() && (map = this.countryIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, categoryName.trim())).isPresent() && map.get().getCanonicalCountry() != null) {
                return map.get().getCanonicalCountry();
            }
        }
        target = countryName != null && !countryName.isBlank() ? countryName.trim() : (categoryName != null && !categoryName.isBlank() ? categoryName.trim() : null);
        if (target == null) {
            return null;
        }
        CountryNormalizer.CountryInfo cInfo = CountryNormalizer.normalize(target, false);
        MdmCountry country = null;
        if (cInfo != null) {
            Optional<MdmCountry> byIso = this.countryRepository.findFirstByIsoCodeIgnoreCase(cInfo.code());
            if (byIso.isPresent()) {
                country = byIso.get();
            } else {
                Optional<MdmCountry> byName = this.countryRepository.findFirstByCanonicalNameIgnoreCase(cInfo.slug());
                if (byName.isPresent()) {
                    country = byName.get();
                }
            }
        }
        if (country == null) {
            country = this.countryRepository.findFirstByCanonicalNameIgnoreCase(target).orElse(null);
        }
        if (country != null && systemCode != null && !systemCode.isBlank() && this.countryIngestionMapRepository != null && this.externalSystemRepository != null) {
            this.saveCountryIngestionMap(systemCode, rawCategoryKey != null ? rawCategoryKey : target, country);
            if (rawCategoryKey != null && !rawCategoryKey.equalsIgnoreCase(target)) {
                this.saveCountryIngestionMap(systemCode, target, country);
            }
        }
        return country;
    }

    public MdmCountry resolveCountryEntity(String countryName, Boolean isInternational) {
        return this.resolveCountryEntity(null, null, countryName, null, isInternational);
    }

    private void enrichTournamentCountry(MdmTournament t, String countryCtx) {
        this.enrichTournamentCountry(t, countryCtx, null);
    }

    private void enrichTournamentCountry(MdmTournament t, String countryCtx, MdmCountry countryEntity) {
        if (t == null) {
            return;
        }
        boolean updated = false;
        if (countryCtx != null && !countryCtx.isBlank() && t.getCountryName() == null) {
            t.setCountryName(countryCtx);
            updated = true;
        }
        if (t.getCountry() == null) {
            MdmCountry c;
            if (countryEntity != null) {
                t.setCountry(countryEntity);
                updated = true;
            } else if (countryCtx != null && !countryCtx.isBlank() && this.countryRepository != null && (c = this.resolveCountryEntity(countryCtx, false)) != null) {
                t.setCountry(c);
                updated = true;
            }
        }
        if (updated) {
            this.tournamentRepository.save(t);
        }
    }

    private void enrichTeamCountry(MdmTeam team, String countryCtx) {
        this.enrichTeamCountry(team, countryCtx, null);
    }

    private void enrichTeamCountry(MdmTeam team, String countryCtx, MdmCountry countryEntity) {
        if (team == null) {
            return;
        }
        boolean updated = false;
        if (countryCtx != null && !countryCtx.isBlank() && team.getCountryName() == null) {
            team.setCountryName(countryCtx);
            updated = true;
        }
        if (team.getCountry() == null) {
            MdmCountry c;
            if (countryEntity != null) {
                team.setCountry(countryEntity);
                updated = true;
            } else if (countryCtx != null && !countryCtx.isBlank() && this.countryRepository != null && (c = this.resolveCountryEntity(countryCtx, false)) != null) {
                team.setCountry(c);
                updated = true;
            }
        }
        if (updated) {
            this.teamRepository.save(team);
        }
    }

    private MdmTeam resolveCanonicalTeam(MdmTeam team) {
        if (team == null || this.teamMergeMapRepository == null) {
            return team;
        }
        Long sofaId = this.remapTeamId(team.getId());
        if (!Objects.equals(sofaId, team.getId())) {
            return this.teamRepository.findById(sofaId).orElse(team);
        }
        return team;
    }

    private CanonicalTeamDto finalizeTeamResolution(MdmTeam team, Long sofaId) {
        if ((team = this.resolveCanonicalTeam(team)) != null && sofaId != null && team.getSofascoreId() == null) {
            this.enrichTeamSofascoreId(team, sofaId);
        }
        return MdmMatchResolutionService.toTeamDto(team);
    }

    private void enrichTeamSofascoreId(MdmTeam team, Long sofaId) {
        if (team == null || sofaId == null) {
            return;
        }
        if (team.getSofascoreId() != null) {
            return;
        }
        try {
            Optional<MdmTeam> existing = this.teamRepository.findBySofascoreId(sofaId);
            if (existing.isEmpty()) {
                team.setSofascoreId(sofaId);
                this.teamRepository.save(team);
                log.info("\ud83d\udc8e [MDM] Linked SofaScore ID {} to canonical team #{} '{}'", new Object[]{sofaId, team.getId(), team.getCanonicalName()});
            }
        }
        catch (Exception e) {
            log.warn("\u26a0\ufe0f [MDM] Could not link SofaScore ID {} to team #{}: {}", new Object[]{sofaId, team.getId(), e.getMessage()});
        }
    }

    private void saveCountryIngestionMap(String systemCode, String rawKey, MdmCountry country) {
        if (systemCode == null || systemCode.isBlank() || rawKey == null || rawKey.isBlank() || country == null) {
            return;
        }
        if (this.externalSystemRepository == null || this.countryIngestionMapRepository == null) {
            return;
        }
        try {
            this.externalSystemRepository.findByCode(systemCode).ifPresent(sys -> this.countryIngestionMapRepository.insertIgnoreConflict(sys.getId(), rawKey.trim(), country.getId(), country.getCanonicalName()));
        }
        catch (Exception e) {
            log.debug("Country ingestion map insert ignored: {}", (Object)e.getMessage());
        }
    }

    private void saveTeamIngestionMap(String systemCode, String rawKey, MdmTeam team) {
        this.saveTeamIngestionMap(systemCode, rawKey, null, team);
    }

    private void saveTeamIngestionMap(String systemCode, String rawKey, String sourceId, MdmTeam team) {
        team = this.resolveCanonicalTeam(team);
        if (systemCode == null || systemCode.isBlank() || rawKey == null || rawKey.isBlank() || team == null) {
            return;
        }
        if (this.externalSystemRepository == null || this.teamIngestionMapRepository == null) {
            return;
        }
        try {
            String teamNameLower;
            MdmTeam targetTeam = team;
            boolean hasFemaleMarker = rawKey.matches("(?i).*(\\bwomen\\b|\\(w\\)|\\(\u0436\u0435\u043d\\)|-w$).*");
            if ("FEMALE".equalsIgnoreCase(targetTeam.getGender()) && !hasFemaleMarker) {
                log.warn("\ud83d\udee1\ufe0f [MDM-GUARD] Refusing to overwrite rawKey '{}' for system '{}' with FEMALE team #{} '{}'. Female teams without explicit female marker in rawKey must not hijack neutral keys!", new Object[]{rawKey, systemCode, targetTeam.getId(), targetTeam.getCanonicalName()});
                return;
            }
            boolean hasYouthMarker = rawKey.matches("(?i).*(\\bu\\d+\\b|\\(\u0434\u043e \\d+\\)|reserve|youth|-u\\d+$).*");
            if (!"SENIOR".equalsIgnoreCase(targetTeam.getAgeCategory()) && !hasYouthMarker) {
                log.warn("\ud83d\udee1\ufe0f [MDM-GUARD] Refusing to overwrite rawKey '{}' for system '{}' with youth/reserve team #{} '{}'. Youth teams without explicit youth marker in rawKey must not hijack neutral keys!", new Object[]{rawKey, systemCode, targetTeam.getId(), targetTeam.getCanonicalName()});
                return;
            }
            boolean hasGamerTag = rawKey.matches("(?i).*\\([A-Za-z0-9_]{3,}\\).*");
            if (hasGamerTag && targetTeam.getSport() != null && targetTeam.getSport().getId() == 1 && !(teamNameLower = targetTeam.getCanonicalName().toLowerCase()).contains("cyber") && !teamNameLower.contains("esports") && !teamNameLower.contains("esport")) {
                log.warn("\ud83d\udee1\ufe0f [MDM-GUARD] Refusing to map cyber gamer rawKey '{}' to real football team #{} '{}'", new Object[]{rawKey, systemCode, targetTeam.getId(), targetTeam.getCanonicalName()});
                return;
            }
            if (!this.isLexicallyCompatible(rawKey, targetTeam.getCanonicalName())) {
                log.warn("\ud83d\udee1\ufe0f [MDM-GUARD] Refusing to map disparate rawKey '{}' to team #{} '{}' (zero lexical similarity)", new Object[]{rawKey, systemCode, targetTeam.getId(), targetTeam.getCanonicalName()});
                return;
            }
            this.externalSystemRepository.findByCode(systemCode).ifPresent(sys -> this.teamIngestionMapRepository.upsertMapping(sys.getId(), rawKey.trim(), sourceId, targetTeam.getId(), targetTeam.getCanonicalName()));
        }
        catch (Exception e) {
            log.debug("Team ingestion map insert ignored: {}", (Object)e.getMessage());
        }
    }

    private boolean isLexicallyCompatible(String rawKey, String canonicalName) {
        Set canTokens;
        String canNorm;
        if (rawKey == null || canonicalName == null) {
            return false;
        }
        String rawNorm = SimilarityUtils.normalizeToLatin((String)rawKey).toLowerCase();
        if (rawNorm.equals(canNorm = SimilarityUtils.normalizeToLatin((String)canonicalName).toLowerCase())) {
            return true;
        }
        if (rawNorm.contains(canNorm) || canNorm.contains(rawNorm)) {
            return true;
        }
        if (SimilarityUtils.jaroWinklerSimilarity((String)rawNorm, (String)canNorm) >= 0.75) {
            return true;
        }
        Set rawTokens = Arrays.stream(rawNorm.split("[^a-z0-9]+")).filter(t -> t.length() >= 3 && !SimilarityUtils.GENERIC_CLUB_WORDS.contains(t)).collect(Collectors.toSet());
        return !Collections.disjoint(rawTokens, canTokens = Arrays.stream(canNorm.split("[^a-z0-9]+")).filter(t -> t.length() >= 3 && !SimilarityUtils.GENERIC_CLUB_WORDS.contains(t)).collect(Collectors.toSet()));
    }

    private void saveTournamentIngestionMap(String systemCode, String rawKey, MdmTournament tourney) {
        this.saveTournamentIngestionMap(systemCode, rawKey, null, tourney);
    }

    private void saveTournamentIngestionMap(String systemCode, String rawKey, String sourceId, MdmTournament tourney) {
        if (systemCode == null || systemCode.isBlank() || rawKey == null || rawKey.isBlank() || tourney == null) {
            return;
        }
        if (this.externalSystemRepository == null || this.tournamentIngestionMapRepository == null) {
            return;
        }
        try {
            this.externalSystemRepository.findByCode(systemCode).ifPresent(sys -> this.tournamentIngestionMapRepository.upsertMapping(sys.getId(), rawKey.trim(), sourceId, tourney.getId(), tourney.getCanonicalName()));
        }
        catch (Exception e) {
            log.debug("Tournament ingestion map insert ignored: {}", (Object)e.getMessage());
        }
    }

    public static boolean hasCyrillic(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        for (int i = 0; i < input.length(); ++i) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(input.charAt(i));
            if (block != Character.UnicodeBlock.CYRILLIC) continue;
            return true;
        }
        return false;
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "unknown";
        }
        String latin = MdmMatchResolutionService.hasCyrillic(input) ? SimilarityUtils.transliterateCyrillic((String)input) : input;
        String normalized = Normalizer.normalize(latin.isBlank() ? input : latin, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }

    public static boolean isAgeCompatible(String teamAge, String expectedAge) {
        String a1 = teamAge == null || teamAge.isBlank() ? "SENIOR" : teamAge.trim().toUpperCase();
        String a2 = expectedAge == null || expectedAge.isBlank() ? "SENIOR" : expectedAge.trim().toUpperCase();
        return Objects.equals(a1, a2);
    }

    public boolean isInternationalTournament(MdmTournament tournament) {
        String nLower;
        String sLower;
        String slug;
        Sport s;
        if (tournament == null) {
            return false;
        }
        MdmSport mdmSport = tournament.getSport();
        if (mdmSport != null && (s = mdmSport.asDomain()) != null && !s.isTeamSport()) {
            return true;
        }
        if (tournament.getCategory() != null && (slug = tournament.getCategory().getSlug()) != null && ((sLower = slug.toLowerCase()).contains("international") || sLower.contains("world") || sLower.contains("europe") || sLower.contains("south-america") || sLower.contains("asia") || sLower.contains("africa") || sLower.contains("north-central-america") || sLower.contains("oceania"))) {
            return true;
        }
        String name = tournament.getCanonicalName();
        return name != null && ((nLower = name.toLowerCase()).contains("champions league") || nLower.contains("europa league") || nLower.contains("conference league") || nLower.contains("copa libertadores") || nLower.contains("copa sudamericana") || nLower.contains("world cup") || nLower.contains("nations league") || nLower.contains("friendly") || nLower.contains("atp") || nLower.contains("wta") || nLower.contains("itf") || nLower.contains("challenger") || nLower.contains("liga pro") || nLower.contains("setka cup") || nLower.contains("tt cup") || nLower.contains("win cup"));
    }

    public boolean isCountryCompatible(MdmTournament tournament, MdmTeam team) {
        String tmIso;
        if (tournament == null || team == null) {
            return true;
        }
        MdmCountry tc = tournament.getCountry();
        MdmCountry tmc = team.getCountry();
        if (tc == null || tmc == null) {
            return true;
        }
        if (Objects.equals(tc.getId(), tmc.getId())) {
            return true;
        }
        String tName = tc.getCanonicalName() != null ? tc.getCanonicalName().toLowerCase().trim() : "";
        String tmName = tmc.getCanonicalName() != null ? tmc.getCanonicalName().toLowerCase().trim() : "";
        String tIso = tc.getIsoCode() != null ? tc.getIsoCode().toUpperCase().trim() : "";
        String string = tmIso = tmc.getIsoCode() != null ? tmc.getIsoCode().toUpperCase().trim() : "";
        if (tName.equals(tmName)) {
            return true;
        }
        if ((tName.equals("usa") || tName.contains("united states") || "US".equals(tIso) || "USA".equals(tIso)) && (tmName.equals("usa") || tmName.contains("united states") || "US".equals(tmIso) || "USA".equals(tmIso))) {
            return true;
        }
        if ((tName.equals("usa") || tName.contains("united states") || "US".equals(tIso) || "USA".equals(tIso)) && (tmName.equals("canada") || "CA".equals(tmIso) || "CAN".equals(tmIso))) {
            return true;
        }
        if ((tName.equals("england") || "GB".equals(tIso) || "ENG".equals(tIso)) && (tmName.equals("wales") || "WAL".equals(tmIso))) {
            return true;
        }
        if ((tName.equals("france") || "FR".equals(tIso) || "FRA".equals(tIso)) && (tmName.equals("monaco") || "MC".equals(tmIso) || "MCO".equals(tmIso))) {
            return true;
        }
        if ((tName.equals("switzerland") || "CH".equals(tIso) || "CHE".equals(tIso)) && (tmName.equals("liechtenstein") || "LI".equals(tmIso) || "LIE".equals(tmIso))) {
            return true;
        }
        return !(!tName.equals("australia") && !"AU".equals(tIso) && !"AUS".equals(tIso) || !tmName.equals("new zealand") && !"NZ".equals(tmIso) && !"NZL".equals(tmIso));
    }

    private void validateLeagueIntegrity(MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String sourceOrigin, String createdBy) {
        long rosterCount;
        boolean isIndividualSport;
        MdmSport sport;
        Sport sportObj;
        this.validateMatchGenderAndAge(tournament, homeTeam, awayTeam, createdBy);
        if (tournament.getGender() != null && !"MIXED".equalsIgnoreCase(tournament.getGender())) {
            if (homeTeam.getGender() != null && !Objects.equals(homeTeam.getGender(), tournament.getGender())) {
                String msg = String.format("Gender mismatch: Home team '%s' (%s) cannot play in %s tournament '%s'", homeTeam.getCanonicalName(), homeTeam.getGender(), tournament.getGender(), tournament.getCanonicalName());
                this.anomalyReportingService.reportLeagueMismatch(createdBy, "GENDER_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
            if (awayTeam.getGender() != null && !Objects.equals(awayTeam.getGender(), tournament.getGender())) {
                String msg = String.format("Gender mismatch: Away team '%s' (%s) cannot play in %s tournament '%s'", awayTeam.getCanonicalName(), awayTeam.getGender(), tournament.getGender(), tournament.getCanonicalName());
                this.anomalyReportingService.reportLeagueMismatch(createdBy, "GENDER_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
        }
        if (tournament.getAgeCategory() != null) {
            if (homeTeam.getAgeCategory() != null && !Objects.equals(homeTeam.getAgeCategory(), tournament.getAgeCategory())) {
                String msg = String.format("Age category mismatch: Home team '%s' (%s) cannot play in %s tournament '%s'", homeTeam.getCanonicalName(), homeTeam.getAgeCategory(), tournament.getAgeCategory(), tournament.getCanonicalName());
                this.anomalyReportingService.reportLeagueMismatch(createdBy, "AGE_CATEGORY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
            if (awayTeam.getAgeCategory() != null && !Objects.equals(awayTeam.getAgeCategory(), tournament.getAgeCategory())) {
                String msg = String.format("Age category mismatch: Away team '%s' (%s) cannot play in %s tournament '%s'", awayTeam.getCanonicalName(), awayTeam.getAgeCategory(), tournament.getAgeCategory(), tournament.getCanonicalName());
                this.anomalyReportingService.reportLeagueMismatch(createdBy, "AGE_CATEGORY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
        }
        Sport sport2 = sportObj = (sport = tournament.getSport()) != null ? sport.asDomain() : null;
        isIndividualSport = sportObj != null ? !sportObj.isTeamSport() : Boolean.FALSE.equals(sport != null ? sport.getIsTeamSport() : true);
        if (!(isIndividualSport || Boolean.TRUE.equals(tournament.getIsCup()) || this.isInternationalTournament(tournament))) {
            if (tournament.getCountry() != null) {
                if (homeTeam.getCountry() != null && !this.isCountryCompatible(tournament, homeTeam)) {
                    String msg = String.format("Country mismatch: Home team '%s' (country=%s #%d) cannot play in domestic tournament '%s' (country=%s #%d)", homeTeam.getCanonicalName(), homeTeam.getCountry().getCanonicalName(), homeTeam.getCountry().getId(), tournament.getCanonicalName(), tournament.getCountry().getCanonicalName(), tournament.getCountry().getId());
                    this.anomalyReportingService.reportLeagueMismatch(createdBy, "COUNTRY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                    throw new IllegalArgumentException(msg);
                }
                if (awayTeam.getCountry() != null && !this.isCountryCompatible(tournament, awayTeam)) {
                    String msg = String.format("Country mismatch: Away team '%s' (country=%s #%d) cannot play in domestic tournament '%s' (country=%s #%d)", awayTeam.getCanonicalName(), awayTeam.getCountry().getCanonicalName(), awayTeam.getCountry().getId(), tournament.getCanonicalName(), tournament.getCountry().getCanonicalName(), tournament.getCountry().getId());
                    this.anomalyReportingService.reportLeagueMismatch(createdBy, "COUNTRY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                    throw new IllegalArgumentException(msg);
                }
            }
            if (tournament.getCategory() != null) {
                Integer tournCatId = tournament.getCategory().getId();
                if (homeTeam.getCategory() != null && !Objects.equals(homeTeam.getCategory().getId(), tournCatId)) {
                    String msg = String.format("Country/Category mismatch: Home team '%s' (category=%d) does not belong to domestic league '%s' (category=%d)", homeTeam.getCanonicalName(), homeTeam.getCategory().getId(), tournament.getCanonicalName(), tournCatId);
                    this.anomalyReportingService.reportLeagueMismatch(createdBy, "COUNTRY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                    throw new IllegalArgumentException(msg);
                }
                if (awayTeam.getCategory() != null && !Objects.equals(awayTeam.getCategory().getId(), tournCatId)) {
                    String msg = String.format("Country/Category mismatch: Away team '%s' (category=%d) does not belong to domestic league '%s' (category=%d)", awayTeam.getCanonicalName(), awayTeam.getCategory().getId(), tournament.getCanonicalName(), tournCatId);
                    this.anomalyReportingService.reportLeagueMismatch(createdBy, "COUNTRY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        if (!Boolean.TRUE.equals(tournament.getIsCup()) && !"SOFASCORE".equalsIgnoreCase(sourceOrigin) && tournament.getSofascoreId() != null && (rosterCount = this.tournamentTeamRepository.countByTournamentIdAndIsActiveTrue(tournament.getId())) >= 16L) {
            if (!this.tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), homeTeam.getId())) {
                String msg = String.format("League roster violation: Home team '%s' (#%d) is not a registered participant of league '%s' (#%d)", homeTeam.getCanonicalName(), homeTeam.getId(), tournament.getCanonicalName(), tournament.getId());
                this.anomalyReportingService.reportLeagueMismatch(createdBy, "ROSTER_VIOLATION", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
            if (!this.tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), awayTeam.getId())) {
                String msg = String.format("League roster violation: Away team '%s' (#%d) is not a registered participant of league '%s' (#%d)", awayTeam.getCanonicalName(), awayTeam.getId(), tournament.getCanonicalName(), tournament.getId());
                this.anomalyReportingService.reportLeagueMismatch(createdBy, "ROSTER_VIOLATION", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                throw new IllegalArgumentException(msg);
            }
        }
        boolean homeIsCyber = CyberMatchDetector.isCyberParticipant((String)homeTeam.getCanonicalName());
        boolean awayIsCyber = CyberMatchDetector.isCyberParticipant((String)awayTeam.getCanonicalName());
        boolean tournIsCyber = CyberMatchDetector.isCyberLeague((String)tournament.getCanonicalName());
        if ((homeIsCyber || awayIsCyber) && !tournIsCyber) {
            String msg = String.format("Cyber simulation participant ('%s' vs '%s') cannot play in official non-cyber tournament '%s'", homeTeam.getCanonicalName(), awayTeam.getCanonicalName(), tournament.getCanonicalName());
            this.anomalyReportingService.reportLeagueMismatch(createdBy, "CYBER_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeIsCyber ? homeTeam.getId() : awayTeam.getId(), homeIsCyber ? homeTeam.getCanonicalName() : awayTeam.getCanonicalName(), msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public void validateMatchGenderAndAge(MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String createdBy) {
        if (homeTeam == null || awayTeam == null) {
            return;
        }
        String homeGender = MdmMatchResolutionService.normalizeGender(homeTeam.getGender());
        String awayGender = MdmMatchResolutionService.normalizeGender(awayTeam.getGender());
        String homeAge = MdmMatchResolutionService.normalizeAge(homeTeam.getAgeCategory());
        String awayAge = MdmMatchResolutionService.normalizeAge(awayTeam.getAgeCategory());
        boolean genderMatch = Objects.equals(homeGender, awayGender);
        boolean ageMatch = Objects.equals(homeAge, awayAge);
        if (!genderMatch || !ageMatch) {
            String mismatchType = !genderMatch && !ageMatch ? "GENDER_AND_AGE_MISMATCH" : (!genderMatch ? "GENDER_MISMATCH" : "AGE_CATEGORY_MISMATCH");
            String reason = String.format("%s: Mutant match blocked: Home team '%s' (gender=%s, age=%s) vs Away team '%s' (gender=%s, age=%s) in tournament '%s'", !genderMatch ? "Gender mismatch" : "Age category mismatch", homeTeam.getCanonicalName(), homeGender, homeAge, awayTeam.getCanonicalName(), awayGender, awayAge, tournament != null ? tournament.getCanonicalName() : "N/A");
            log.error("\ud83d\udea8 [MDM-MUTANT-BLOCKED] {}", (Object)reason);
            if (this.anomalyReportingService != null) {
                this.anomalyReportingService.reportMutantMatch(createdBy, tournament, homeTeam, awayTeam, mismatchType, reason);
                if (tournament != null) {
                    this.anomalyReportingService.reportLeagueMismatch(createdBy, mismatchType, tournament.getId(), tournament.getCanonicalName(), !genderMatch ? homeTeam.getId() : awayTeam.getId(), !genderMatch ? homeTeam.getCanonicalName() : awayTeam.getCanonicalName(), reason);
                }
            }
            throw new IllegalArgumentException("\ud83d\udea8 [MDM-MUTANT-BLOCKED] " + reason);
        }
    }

    public static String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return "MALE";
        }
        String g = gender.trim().toUpperCase();
        if (g.contains("FEMALE") || g.contains("WOMEN") || g.contains("WOMAN") || g.contains("LADIES") || g.contains("GIRL")) {
            return "FEMALE";
        }
        if (g.contains("MIX")) {
            return "MIXED";
        }
        return "MALE";
    }

    public static String normalizeAge(String ageCategory) {
        if (ageCategory == null || ageCategory.isBlank()) {
            return "SENIOR";
        }
        return ageCategory.trim().toUpperCase();
    }

    public static boolean isGenderCompatible(String g1, String g2) {
        return Objects.equals(MdmMatchResolutionService.normalizeGender(g1), MdmMatchResolutionService.normalizeGender(g2));
    }

    private boolean isSyntheticLeague(MatchResolutionRequest request) {
        if (request == null || request.getTournamentName() == null) {
            return false;
        }
        Integer sportId = request.getSportId();
        if (sportId != null && this.isEsportsSport(sportId)) {
            return false;
        }
        String t = request.getTournamentName().trim();
        return SYNTHETIC_LEAGUE_PATTERN.matcher(t).matches();
    }

    private boolean isEsportsSport(int sportId) {
        return sportId == 6 || sportId >= 33 && sportId <= 42;
    }

    private boolean isMalformedDoubles(MatchResolutionRequest request) {
        if (request == null || request.getSportId() == null) {
            return false;
        }
        int sportId = request.getSportId();
        if (sportId != 4 && sportId != 5) {
            return false;
        }
        String h = request.getHomeTeamName();
        String a = request.getAwayTeamName();
        if (h == null || a == null) {
            return false;
        }
        return h.contains("/") != a.contains("/");
    }

    private void dispatchTrueSport(MatchResolutionRequest request) {
        if (request == null) {
            return;
        }
        if (request.getSportId() == null && request.getSportSlug() != null) {
            this.sportRepository.findBySlug(request.getSportSlug().trim().toLowerCase()).ifPresent(s -> request.setSportId(s.getId()));
        }
        String tName = request.getTournamentName() != null ? request.getTournamentName() : "";
        String tLower = tName.toLowerCase().trim();
        if (request.getSportId() != null && request.getSportId() != 4 && request.getSportId() != 5 && (tLower.contains("atp") || tLower.contains("wta") || tLower.contains("itf") || tLower.contains("\u0440\u043e\u043b\u0430\u043d \u0433\u0430\u0440\u0440\u043e\u0441") || tLower.contains("\u0443\u0438\u043c\u0431\u043b\u0434\u043e\u043d") || tLower.contains("australian open") || tLower.contains("us open") || tLower.contains("\u0447\u0435\u043b\u043b\u0435\u043d\u0434\u0436\u0435\u0440") && !tLower.contains("challenger league") || tLower.contains("wtt"))) {
            if (tLower.contains("wtt") || tLower.contains("\u043d\u0430\u0441\u0442\u043e\u043b\u044c\u043d\u044b\u0439") || tLower.contains("table tennis") || tLower.contains("setka cup") || tLower.contains("liga pro")) {
                request.setSportId(Integer.valueOf(5));
                request.setSportSlug("table-tennis");
            } else {
                request.setSportId(Integer.valueOf(4));
                request.setSportSlug("tennis");
            }
            log.warn("\ud83d\udee1\ufe0f [MDM Disinfection] Re-routed tournament '{}' from corrupted sport to {}", (Object)tName, (Object)request.getSportSlug());
        }
        if (request.getSportId() != null && request.getSportId() != 2 && request.getSportId() != 30 && request.getSportId() != 42 && (tLower.contains("nhl") || tLower.contains("\u043d\u0445\u043b") || tLower.contains("\u043a\u0445\u043b") || tLower.contains("\u0432\u0445\u043b") || tLower.contains("\u043c\u0445\u043b"))) {
            if (tLower.contains("\u043a\u0438\u0431\u0435\u0440") || tLower.contains("cyber") || tLower.contains("3x4") || tLower.contains("4 \u043c\u0438\u043d\u0443\u0442\u044b")) {
                request.setSportId(Integer.valueOf(42));
                request.setSportSlug("cyber-hockey");
            } else if (tLower.contains("3x3") || tLower.contains("3\u04453") || tLower.contains("short") || tLower.contains("3hl") || tLower.contains("rhl")) {
                request.setSportId(Integer.valueOf(30));
                request.setSportSlug("short-hockey");
            } else {
                request.setSportId(Integer.valueOf(2));
                request.setSportSlug("ice-hockey");
            }
            log.warn("\ud83d\udee1\ufe0f [MDM Disinfection] Re-routed tournament '{}' to {}", (Object)tName, (Object)request.getSportSlug());
        }
        if (request.getSportId() != null && request.getSportId() != 3 && request.getSportId() != 29 && request.getSportId() != 41 && (tLower.contains("nba") || tLower.contains("\u043d\u0431\u0430") || tLower.contains("\u0435\u0432\u0440\u043e\u043b\u0438\u0433\u0430") || tLower.contains("euroleague"))) {
            if (tLower.contains("\u043a\u0438\u0431\u0435\u0440") || tLower.contains("cyber") || tLower.contains("2k") || tLower.contains("4x5") || tLower.contains("4\u04455")) {
                request.setSportId(Integer.valueOf(41));
                request.setSportSlug("cyber-basketball");
            } else if (tLower.contains("3x3") || tLower.contains("3\u04453")) {
                request.setSportId(Integer.valueOf(29));
                request.setSportSlug("basketball-3x3");
            } else {
                request.setSportId(Integer.valueOf(3));
                request.setSportSlug("basketball");
            }
            log.warn("\ud83d\udee1\ufe0f [MDM Disinfection] Re-routed tournament '{}' to {}", (Object)tName, (Object)request.getSportSlug());
        }
        if (request.getSportId() != null && request.getSportId() == 6) {
            if (tLower.contains("fc ") || tLower.contains("fc.") || tLower.contains("fc 2") || tLower.contains("fifa") || tLower.contains("\u043a\u0438\u0431\u0435\u0440\u0444\u0443\u0442\u0431\u043e\u043b") || tLower.contains("volta") || tLower.contains("gt sports")) {
                request.setSportId(Integer.valueOf(37));
                request.setSportSlug("cyber-football");
            } else if (tLower.contains("nhl") || tLower.contains("\u043a\u0438\u0431\u0435\u0440\u0445\u043e\u043a\u043a\u0435\u0439")) {
                request.setSportId(Integer.valueOf(42));
                request.setSportSlug("cyber-hockey");
            } else if (tLower.contains("nba") || tLower.contains("\u043a\u0438\u0431\u0435\u0440\u0431\u0430\u0441\u043a\u0435\u0442\u0431\u043e\u043b") || tLower.contains("2k")) {
                request.setSportId(Integer.valueOf(41));
                request.setSportSlug("cyber-basketball");
            } else if (tLower.contains("cs2") || tLower.contains("counter-strike") || tLower.contains("cct ") || tLower.contains("esea") || tLower.contains("esl challenger") || tLower.contains("blast premier")) {
                request.setSportId(Integer.valueOf(33));
                request.setSportSlug("cs2");
            } else if (tLower.contains("dota") || tLower.contains("dreamleague") || tLower.contains("the international")) {
                request.setSportId(Integer.valueOf(34));
                request.setSportSlug("dota2");
            } else if (tLower.contains("league of legends") || tLower.contains("lck") || tLower.contains("lec") || tLower.contains("lpl") || tLower.contains("lcs") || tLower.contains("emea masters")) {
                request.setSportId(Integer.valueOf(35));
                request.setSportSlug("league-of-legends");
            } else if (tLower.contains("valorant") || tLower.contains("vct")) {
                request.setSportId(Integer.valueOf(36));
                request.setSportSlug("valorant");
            }
        }
        if (request.getSportId() != null && request.getSportId() == 3) {
            if (tLower.contains("3x3") || tLower.contains("3\u04453")) {
                request.setSportId(Integer.valueOf(29));
                request.setSportSlug("basketball-3x3");
            } else if (tLower.contains("\u043a\u0438\u0431\u0435\u0440") || tLower.contains("cyber") || tLower.contains("2k") || tLower.contains("4x5")) {
                request.setSportId(Integer.valueOf(41));
                request.setSportSlug("cyber-basketball");
            }
        }
        if (request.getSportId() != null && request.getSportId() == 2) {
            if (tLower.contains("3x3") || tLower.contains("3\u04453") || tLower.contains("short") || tLower.contains("3hl") || tLower.contains("rhl") || tLower.contains("\u0448\u043e\u0440\u0442")) {
                request.setSportId(Integer.valueOf(30));
                request.setSportSlug("short-hockey");
            } else if (tLower.contains("\u043a\u0438\u0431\u0435\u0440") || tLower.contains("cyber") || tLower.contains("nhl 2") || tLower.contains("united esports") || tLower.contains("h2h")) {
                request.setSportId(Integer.valueOf(42));
                request.setSportSlug("cyber-hockey");
            }
        }
        if (request.getSportId() != null && request.getSportId() == 1) {
            boolean hasGamerHandle;
            String hName = request.getHomeTeamName() != null ? request.getHomeTeamName() : "";
            String aName = request.getAwayTeamName() != null ? request.getAwayTeamName() : "";
            boolean bl = hasGamerHandle = hName.matches("(?i).*\\([A-Za-z0-9_]{3,}\\).*") || aName.matches("(?i).*\\([A-Za-z0-9_]{3,}\\).*");
            if (hasGamerHandle || tLower.contains("cyber") || tLower.contains("\u043a\u0438\u0431\u0435\u0440") || tLower.contains("fifa") || tLower.contains("fc 2") || tLower.contains("fc 24") || tLower.contains("fc 25") || tLower.contains("fc 26") || tLower.contains("esportsbattle") || tLower.contains("esport battle") || tLower.contains("esports battle") || tLower.contains("gt league") || tLower.contains("gt sports") || tLower.contains("live arena") || tLower.contains("volta") || tLower.contains("h2h liga") || tLower.contains("united esports")) {
                request.setSportId(Integer.valueOf(37));
                request.setSportSlug("cyber-football");
                log.warn("\ud83d\udee1\ufe0f [MDM Disinfection] Re-routed football match '{}: {} vs {}' to cyber-football (sport_id=37)", new Object[]{tName, hName, aName});
            } else if (tLower.contains("short football") || tLower.contains("\u0448\u043e\u0440\u0442-\u0444\u0443\u0442\u0431\u043e\u043b") || tLower.contains("\u0448\u043e\u0440\u0442 \u0444\u0443\u0442\u0431\u043e\u043b") || tLower.contains("budnesliga") || tLower.contains("subsoccer") || tLower.matches("(?i).*\\b(?:2x2|3x3|4x4|5x5|6x6|7x7|8x8)\\b.*")) {
                request.setSportId(Integer.valueOf(28));
                request.setSportSlug("short-football");
                log.warn("\ud83d\udee1\ufe0f [MDM Disinfection] Re-routed football tournament '{}' to short-football (sport_id=28)", (Object)tName);
            }
        }
    }

    private void validateRequest(MatchResolutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("MatchResolutionRequest cannot be null");
        }
        if (this.isSyntheticPlaceholder(request.getHomeTeamName()) || this.isSyntheticPlaceholder(request.getAwayTeamName())) {
            throw new IllegalArgumentException("Synthetic placeholder match rejected: " + request.getHomeTeamName() + " vs " + request.getAwayTeamName());
        }
        if (this.isStatsOrOutrightMatch(request)) {
            throw new IllegalArgumentException("Stats props or outright pseudo-match rejected: " + request.getHomeTeamName() + " vs " + request.getAwayTeamName() + " in " + request.getTournamentName());
        }
        if (this.isMalformedDoubles(request)) {
            throw new IllegalArgumentException("Malformed doubles mismatch (singles vs doubles): '" + request.getHomeTeamName() + "' vs '" + request.getAwayTeamName() + "'");
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
        if (request.getStartTimeEpochSec() == null || request.getStartTimeEpochSec() <= 0L) {
            throw new IllegalArgumentException("Valid start time epoch is mandatory");
        }
    }

    private boolean isStatsOrOutrightMatch(MatchResolutionRequest request) {
        if (request == null) {
            return false;
        }
        if (this.matchesStatsOrOutright(request.getHomeTeamName())) {
            return true;
        }
        if (this.matchesStatsOrOutright(request.getAwayTeamName())) {
            return true;
        }
        return this.matchesStatsOrOutright(request.getTournamentName());
    }

    private boolean matchesStatsOrOutright(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return STATS_OR_OUTRIGHT_PATTERN.matcher(name).matches();
    }

    private boolean isSyntheticPlaceholder(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String lower = name.trim().toLowerCase();
        return lower.equals("\u0445\u043e\u0437\u044f\u0435\u0432\u0430") || lower.equals("\u0433\u043e\u0441\u0442\u0438") || lower.equals("\u0445\u043e\u0437\u044f\u0435\u0432\u0430 \u0442\u0443\u0440\u0430") || lower.equals("\u0433\u043e\u0441\u0442\u0438 \u0442\u0443\u0440\u0430") || lower.equals("team 1") || lower.equals("team 2") || lower.equals("home") || lower.equals("away");
    }

    private MdmMatch findClosestCandidate(List<MdmMatch> candidates, OffsetDateTime targetTime) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        long targetSec = targetTime.toEpochSecond();
        MdmMatch best = candidates.get(0);
        long minDiff = Math.abs(best.getStartTime().toEpochSecond() - targetSec);
        for (int i = 1; i < candidates.size(); ++i) {
            MdmMatch current = candidates.get(i);
            long diff = Math.abs(current.getStartTime().toEpochSecond() - targetSec);
            if (diff >= minDiff) continue;
            minDiff = diff;
            best = current;
        }
        return best;
    }

    private CanonicalMatchHeaderDto tryResolveByTemporalMatchAnchor(MatchResolutionRequest request, OffsetDateTime targetTime) {
        List<MdmMatch> directCandidates;
        Integer sportId = request.getSportId();
        if (sportId == null || targetTime == null) {
            return null;
        }
        OffsetDateTime windowStart = targetTime.minusHours(3L);
        OffsetDateTime windowEnd = targetTime.plusHours(3L);
        String rawHome = request.getHomeTeamName();
        String rawAway = request.getAwayTeamName();
        String tournContext = request.getTournamentName();
        Long preResolvedHomeId = null;
        Long preResolvedAwayId = null;
        if (this.externalSystemRepository != null && this.teamIngestionMapRepository != null && request.getCreatedBySource() != null && rawHome != null && rawAway != null) {
            try {
                Optional<MdmExternalSystem> sysOpt = this.externalSystemRepository.findByCode(request.getCreatedBySource());
                if (sysOpt.isPresent()) {
                    Integer sysId = sysOpt.get().getId();
                    Optional<MdmTeamIngestionMap> homeMap = this.teamIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, rawHome.trim());
                    Optional<MdmTeamIngestionMap> awayMap = this.teamIngestionMapRepository.findByExternalSystemIdAndRawKey(sysId, rawAway.trim());
                    if (homeMap.isPresent() && homeMap.get().getCanonicalTeam() != null) {
                        preResolvedHomeId = this.resolveCanonicalTeam(homeMap.get().getCanonicalTeam()).getId();
                    }
                    if (awayMap.isPresent() && awayMap.get().getCanonicalTeam() != null) {
                        preResolvedAwayId = this.resolveCanonicalTeam(awayMap.get().getCanonicalTeam()).getId();
                    }
                }
            }
            catch (Exception sysOpt) {
                // empty catch block
            }
        }
        if (preResolvedHomeId != null && preResolvedAwayId != null && !(directCandidates = this.matchRepository.findCandidateMatches(sportId, preResolvedHomeId, preResolvedAwayId, windowStart, windowEnd)).isEmpty()) {
            MdmMatch cand = this.findClosestCandidate(directCandidates, targetTime);
            MdmTeam canonicalHome = this.resolveCanonicalTeam(cand.getHomeTeam());
            MdmTeam canonicalAway = this.resolveCanonicalTeam(cand.getAwayTeam());
            if (!MdmMatchResolutionService.isGenderCompatible(canonicalHome.getGender(), canonicalAway.getGender()) || !MdmMatchResolutionService.isAgeCompatible(canonicalHome.getAgeCategory(), canonicalAway.getAgeCategory())) {
                this.validateMatchGenderAndAge(cand.getTournament(), canonicalHome, canonicalAway, request.getCreatedBySource());
            }
            log.info("\ud83c\udfaf [MDM-ANCHOR-FAST] Matched via pre-resolved IDs (#{} vs #{}) to canonical match #{} ('{}' vs '{}')", new Object[]{preResolvedHomeId, preResolvedAwayId, cand.getId(), canonicalHome.getCanonicalName(), canonicalAway.getCanonicalName()});
            if (cand.getTournament() != null) {
                request.setTournamentId(cand.getTournament().getId());
            }
            request.setHomeTeamId(canonicalHome.getId());
            request.setAwayTeamId(canonicalAway.getId());
            if (cand.getSofascoreId() != null && request.getSofascoreId() == null) {
                request.setSofascoreId(cand.getSofascoreId());
            }
            this.saveTeamIngestionMap(request.getCreatedBySource(), rawHome, canonicalHome);
            this.saveTeamIngestionMap(request.getCreatedBySource(), rawAway, canonicalAway);
            if (tournContext != null && cand.getTournament() != null) {
                this.saveTournamentIngestionMap(request.getCreatedBySource(), tournContext, cand.getTournament());
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
            if ("SCHEDULED".equals(cand.getStatus()) && Math.abs(cand.getStartTime().toEpochSecond() - targetTime.toEpochSecond()) > 300L) {
                cand.setStartTime(targetTime);
                updated = true;
            }
            if (request.getSofascoreId() != null && cand.getSofascoreId() == null) {
                cand.setSofascoreId(request.getSofascoreId());
                cand.setSourceOrigin("SOFASCORE");
                updated = true;
            }
            if (updated) {
                cand = (MdmMatch)this.matchRepository.save(cand);
                this.broadcastMatchUpserted(cand);
            }
            return MdmMatchResolutionService.toHeaderDto(cand);
        }
        List<MdmMatch> windowMatches = this.matchRepository.findMatchesInWindow(sportId, windowStart, windowEnd);
        if (windowMatches == null || windowMatches.isEmpty()) {
            return null;
        }
        for (MdmMatch cand : windowMatches) {
            boolean homeSimNoContext;
            boolean match;
            MdmTeam canonicalHome = this.resolveCanonicalTeam(cand.getHomeTeam());
            MdmTeam canonicalAway = this.resolveCanonicalTeam(cand.getAwayTeam());
            if (!MdmMatchResolutionService.isGenderCompatible(canonicalHome.getGender(), canonicalAway.getGender()) || !MdmMatchResolutionService.isAgeCompatible(canonicalHome.getAgeCategory(), canonicalAway.getAgeCategory())) continue;
            String homeGenderCtx = canonicalHome.getGender() != null && "FEMALE".equalsIgnoreCase(canonicalHome.getGender()) ? "(w)" : null;
            String awayGenderCtx = canonicalAway.getGender() != null && "FEMALE".equalsIgnoreCase(canonicalAway.getGender()) ? "(w)" : null;
            String tournName = cand.getTournament() != null ? cand.getTournament().getCanonicalName() : "";
            String homeCtxCand = tournName + (String)(homeGenderCtx != null ? " " + homeGenderCtx : "");
            String awayCtxCand = tournName + (String)(awayGenderCtx != null ? " " + awayGenderCtx : "");
            boolean homeSim = SimilarityUtils.areTeamsSimilar((String)rawHome, (String)canonicalHome.getCanonicalName(), (String)tournContext, (String)homeCtxCand);
            boolean bl = match = homeSim && SimilarityUtils.areTeamsSimilar((String)rawAway, (String)canonicalAway.getCanonicalName(), (String)tournContext, (String)awayCtxCand);
            if (!match && (homeSimNoContext = SimilarityUtils.areTeamsSimilar((String)rawHome, (String)canonicalHome.getCanonicalName(), null, (String)homeGenderCtx))) {
                match = SimilarityUtils.areTeamsSimilar((String)rawAway, (String)canonicalAway.getCanonicalName(), null, (String)awayGenderCtx);
            }
            if (!match) {
                boolean revHomeNoContext;
                boolean revHome = SimilarityUtils.areTeamsSimilar((String)rawHome, (String)canonicalAway.getCanonicalName(), (String)tournContext, (String)awayCtxCand);
                if (revHome) {
                    match = SimilarityUtils.areTeamsSimilar((String)rawAway, (String)canonicalHome.getCanonicalName(), (String)tournContext, (String)homeCtxCand);
                }
                if (!match && (revHomeNoContext = SimilarityUtils.areTeamsSimilar((String)rawHome, (String)canonicalAway.getCanonicalName(), null, (String)awayGenderCtx))) {
                    match = SimilarityUtils.areTeamsSimilar((String)rawAway, (String)canonicalHome.getCanonicalName(), null, (String)homeGenderCtx);
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
            if (!match && timeDiffSec <= 3600L) {
                boolean homeDirectSim = SimilarityUtils.areTeamsSimilar((String)rawHome, (String)canonicalHome.getCanonicalName(), null, (String)homeGenderCtx);
                if (homeDirectSim && this.isLexicallyCompatible(rawAway, canonicalAway.getCanonicalName()) && SimilarityUtils.jaroWinklerSimilarity((String)SimilarityUtils.normalizeToLatin((String)rawAway), (String)SimilarityUtils.normalizeToLatin((String)canonicalAway.getCanonicalName())) >= 0.85) {
                    match = true;
                } else {
                    boolean awayDirectSim = SimilarityUtils.areTeamsSimilar((String)rawAway, (String)canonicalAway.getCanonicalName(), null, (String)awayGenderCtx);
                    if (awayDirectSim && this.isLexicallyCompatible(rawHome, canonicalHome.getCanonicalName()) && SimilarityUtils.jaroWinklerSimilarity((String)SimilarityUtils.normalizeToLatin((String)rawHome), (String)SimilarityUtils.normalizeToLatin((String)canonicalHome.getCanonicalName())) >= 0.85) {
                        match = true;
                    }
                }
            }
            if (!match) continue;
            log.info("\ud83c\udfaf [MDM-ANCHOR] Matched incoming match '{}' vs '{}' (start={}) to canonical match #{} ('{}' vs '{}') in tourn '{}' (sofascoreId={})", new Object[]{rawHome, rawAway, targetTime, cand.getId(), canonicalHome.getCanonicalName(), canonicalAway.getCanonicalName(), tournName, cand.getSofascoreId()});
            if (cand.getTournament() != null) {
                request.setTournamentId(cand.getTournament().getId());
            }
            request.setHomeTeamId(canonicalHome.getId());
            request.setAwayTeamId(canonicalAway.getId());
            if (cand.getSofascoreId() != null && request.getSofascoreId() == null) {
                request.setSofascoreId(cand.getSofascoreId());
            }
            this.saveTeamIngestionMap(request.getCreatedBySource(), rawHome, canonicalHome);
            this.saveTeamIngestionMap(request.getCreatedBySource(), rawAway, canonicalAway);
            if (tournContext != null && cand.getTournament() != null) {
                this.saveTournamentIngestionMap(request.getCreatedBySource(), tournContext, cand.getTournament());
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
            if (targetTime != null && "SCHEDULED".equals(cand.getStatus()) && Math.abs(cand.getStartTime().toEpochSecond() - targetTime.toEpochSecond()) > 300L) {
                log.info("\u23f0 [MDM-ANCHOR] Updating start time for match #{} ({}-{}) from {} to {} (source={})", new Object[]{cand.getId(), canonicalHome.getCanonicalName(), canonicalAway.getCanonicalName(), cand.getStartTime(), targetTime, request.getCreatedBySource()});
                cand.setStartTime(targetTime);
                updated = true;
            }
            if (request.getSofascoreId() != null && cand.getSofascoreId() == null) {
                cand.setSofascoreId(request.getSofascoreId());
                cand.setSourceOrigin("SOFASCORE");
                updated = true;
            }
            if (updated) {
                cand = (MdmMatch)this.matchRepository.save(cand);
                this.broadcastMatchUpserted(cand);
            }
            return MdmMatchResolutionService.toHeaderDto(cand);
        }
        return null;
    }

    private void broadcastMatchUpserted(MdmMatch match) {
        if (this.kafkaTemplate != null) {
            try {
                CanonicalMatchHeaderDto header = MdmMatchResolutionService.toHeaderDto(match);
                String messageKey = match.getSport().getId() + ":" + match.getHomeTeam().getId() + ":" + match.getAwayTeam().getId();
                this.kafkaTemplate.send(this.matchesUpsertedTopic, messageKey, header);
            }
            catch (Exception e) {
                log.warn("Failed to broadcast match upserted event to Kafka: {}", (Object)e.getMessage());
            }
        }
    }

    public static CanonicalMatchHeaderDto toHeaderDto(MdmMatch match) {
        if (match == null) {
            return null;
        }
        MdmTournament t = match.getTournament();
        MdmTeam ht = match.getHomeTeam();
        MdmTeam at = match.getAwayTeam();
        return CanonicalMatchHeaderDto.builder().matchId(match.getId()).sportId(match.getSport().getId()).sportSlug(match.getSport().getSlug()).tournamentId(t != null ? t.getId() : null).tournamentName(t != null ? t.getCanonicalName() : null).countryId(t != null && t.getCountry() != null ? t.getCountry().getId() : null).countryName(t != null && t.getCountry() != null ? t.getCountry().getCanonicalName() : (t != null ? t.getCountryName() : null)).gender(t != null ? t.getGender() : null).ageCategory(t != null ? t.getAgeCategory() : null).isCup(t != null ? t.getIsCup() : null).homeTeamId(ht != null ? ht.getId() : null).homeTeamName(ht != null ? ht.getCanonicalName() : null).homeCountryId(ht != null && ht.getCountry() != null ? ht.getCountry().getId() : null).homeCountryName(ht != null && ht.getCountry() != null ? ht.getCountry().getCanonicalName() : (ht != null ? ht.getCountryName() : null)).awayTeamId(at != null ? at.getId() : null).awayTeamName(at != null ? at.getCanonicalName() : null).awayCountryId(at != null && at.getCountry() != null ? at.getCountry().getId() : null).awayCountryName(at != null && at.getCountry() != null ? at.getCountry().getCanonicalName() : (at != null ? at.getCountryName() : null)).startTimeEpochSec(Long.valueOf(match.getStartTime().toEpochSecond())).sofascoreId(match.getSofascoreId()).mlEligible(Boolean.valueOf(t != null && Boolean.TRUE.equals(t.getMlEligible()))).coverageTier(t != null ? t.getCoverageTier() : null).sourceOrigin(match.getSourceOrigin()).status(match.getStatus()).score1(match.getScore1()).score2(match.getScore2()).regularScore1(match.getRegularScore1()).regularScore2(match.getRegularScore2()).overtimeScore1(match.getOvertimeScore1()).overtimeScore2(match.getOvertimeScore2()).penaltiesScore1(match.getPenaltiesScore1()).penaltiesScore2(match.getPenaltiesScore2()).stage(match.getStage()).round(match.getRound()).bestOf(match.getBestOf()).build();
    }

    private static String sanitizeEntityOrigin(String rawOrigin, Long sofaId) {
        String o;
        if (sofaId != null) {
            return "SOFASCORE";
        }
        if (rawOrigin != null && ("SOFASCORE".equals(o = rawOrigin.trim().toUpperCase()) || "OPERATOR".equals(o) || "NATIVE_LOCAL".equals(o))) {
            return o;
        }
        return "NATIVE_LOCAL";
    }

    private static String sanitizeMatchOrigin(String rawOrigin, Long sofaId) {
        String o;
        if (sofaId != null) {
            return "SOFASCORE";
        }
        if (rawOrigin != null && ("SOFASCORE".equals(o = rawOrigin.trim().toUpperCase()) || "OPERATOR".equals(o) || "BOOKMAKER".equals(o))) {
            return o;
        }
        return "BOOKMAKER";
    }

    public static OffsetDateTime toOffsetDateTime(Long epochTime) {
        if (epochTime == null || epochTime <= 0L) {
            return OffsetDateTime.now(ZoneOffset.UTC);
        }
        Instant instant = epochTime > 100000000000L ? Instant.ofEpochMilli(epochTime) : Instant.ofEpochSecond(epochTime);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}

