package pro.datawiki.igaming.mdm.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.CanonicalTeamDto;
import pro.datawiki.igaming.dto.TeamResolutionRequest;
import pro.datawiki.igaming.dto.signature.CyberMatchDetector;
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.dto.util.SimilarityUtils;
import pro.datawiki.igaming.mdm.domain.*;
import pro.datawiki.igaming.mdm.repository.*;
import pro.datawiki.igaming.mdm.util.CountryNormalizer;

import java.text.Normalizer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Dedicated service for resolving, creating, and managing canonical MDM teams,
 * athletes, club aliases, and roster metadata across all supported sports.
 */
@Service
@Slf4j
public class MdmTeamResolutionService {

    private final MdmTeamRepository teamRepository;
    private final MdmTeamIngestionMapRepository teamIngestionMapRepository;
    private final MdmTeamTranslationRepository teamTranslationRepository;
    private final MdmTeamMergeMapRepository teamMergeMapRepository;
    private final MdmTournamentTeamRepository tournamentTeamRepository;
    private final MdmSportRepository sportRepository;
    private final MdmExternalSystemRepository externalSystemRepository;
    private final MdmCategoryRepository categoryRepository;
    private final MdmCountryRepository countryRepository;
    private final MdmTournamentRepository tournamentRepository;
    private final MatchIntegrityValidator integrityValidator;
    private final MdmTournamentResolutionService tournamentResolutionService;

    private final Map<Long, Long> localTeamToSofaMapCache = new ConcurrentHashMap<>();
    private final Map<String, MdmSport> sportBySlugCache = new ConcurrentHashMap<>();
    private final Map<Integer, MdmSport> sportByIdCache = new ConcurrentHashMap<>();
    private final Map<String, MdmExternalSystem> externalSystemByCodeCache = new ConcurrentHashMap<>();

    private final Cache<String, CanonicalTeamDto> teamResolutionCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(15))
                    .maximumSize(100_000)
                    .build();

    @Autowired
    public MdmTeamResolutionService(
            MdmTeamRepository teamRepository,
            @Autowired(required = false) MdmTeamIngestionMapRepository teamIngestionMapRepository,
            @Autowired(required = false) MdmTeamTranslationRepository teamTranslationRepository,
            @Autowired(required = false) MdmTeamMergeMapRepository teamMergeMapRepository,
            @Autowired(required = false) MdmTournamentTeamRepository tournamentTeamRepository,
            @Autowired(required = false) MdmSportRepository sportRepository,
            @Autowired(required = false) MdmExternalSystemRepository externalSystemRepository,
            @Autowired(required = false) MdmCategoryRepository categoryRepository,
            @Autowired(required = false) MdmCountryRepository countryRepository,
            @Autowired(required = false) MdmTournamentRepository tournamentRepository,
            @Autowired(required = false) MatchIntegrityValidator integrityValidator,
            @Autowired(required = false) MdmTournamentResolutionService tournamentResolutionService
    ) {
        this.teamRepository = teamRepository;
        this.teamIngestionMapRepository = teamIngestionMapRepository;
        this.teamTranslationRepository = teamTranslationRepository;
        this.teamMergeMapRepository = teamMergeMapRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.sportRepository = sportRepository;
        this.externalSystemRepository = externalSystemRepository;
        this.categoryRepository = categoryRepository;
        this.countryRepository = countryRepository;
        this.tournamentRepository = tournamentRepository;
        this.integrityValidator = integrityValidator != null ? integrityValidator
                : new MatchIntegrityValidator(null, tournamentTeamRepository, teamRepository);
        this.tournamentResolutionService = tournamentResolutionService;
    }

    public Optional<MdmSport> findSportBySlugCached(String slug) {
        if (slug == null || sportRepository == null) return Optional.empty();
        String rawKey = slug.trim().toLowerCase().replace('_', '-').replaceAll("\\s+", "-");
        final String key = switch (rawKey) {
            case "soccer", "fotboll", "futbol", "futebol", "fussball" -> "football";
            case "hockey", "ishockey", "eishockey", "eis-hockey" -> "ice-hockey";
            case "water-polo", "water_polo" -> "waterpolo";
            case "e-basketball", "ebasketball", "esports-basketball" -> "cyber-basketball";
            case "e-football", "efootball", "fifa", "esports-football" -> "cyber-football";
            case "e-hockey", "ehockey", "esports-hockey" -> "cyber-hockey";
            case "ping-pong", "tabletennis", "bordtennis", "tischtennis" -> "table-tennis";
            case "handboll", "handball", "гандбол" -> "handball";
            case "innebandy", "salibandy", "флорбол" -> "floorball";
            case "australian-football", "aussie-football" -> "aussie-rules";
            case "ufc" -> "mma";
            case "csgo", "cs-go", "counter-strike", "counterstrike" -> "cs2";
            case "dota", "dota-2" -> "dota2";
            case "lol" -> "league-of-legends";
            default -> rawKey;
        };
        MdmSport cached = sportBySlugCache.get(key);
        if (cached != null) return Optional.of(cached);
        Optional<MdmSport> found = sportRepository.findBySlug(key);
        if (found.isEmpty() && key.contains("-")) {
            found = sportRepository.findBySlug(key.replace("-", ""));
        }
        if (found.isEmpty()) {
            pro.datawiki.igaming.dto.sport.Sport dSport = pro.datawiki.igaming.dto.sport.Sports.get(rawKey);
            if (dSport != null && dSport != pro.datawiki.igaming.dto.sport.Sports.UNKNOWN) {
                int id = pro.datawiki.igaming.dto.sport.Sports.getId(dSport);
                if (id > 0) {
                    found = findSportByIdCached(id);
                } else if (dSport.getSlug() != null && !dSport.getSlug().equalsIgnoreCase(key)) {
                    found = sportRepository.findBySlug(dSport.getSlug().trim().toLowerCase());
                }
            }
        }
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
            if (request.getSportId() == null) {
                pro.datawiki.igaming.dto.sport.Sport sp = pro.datawiki.igaming.dto.sport.Sports.get(request.getSportSlug());
                if (sp != null && sp != pro.datawiki.igaming.dto.sport.Sports.UNKNOWN) {
                    int id = pro.datawiki.igaming.dto.sport.Sports.getId(sp);
                    if (id > 0) request.setSportId(id);
                }
            }
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
            if (contextTournament.getGender() != null && !"MIXED".equalsIgnoreCase(contextTournament.getGender())
                    && request.getGender() == null && (parsed == null || parsed.gender() == TeamSignatureNormalizer.Gender.MALE)) {
                expectedGender = contextTournament.getGender().toUpperCase();
            }
            if (contextTournament.getAgeCategory() != null && !"SENIOR".equalsIgnoreCase(contextTournament.getAgeCategory())
                    && request.getAgeCategory() == null && (parsed == null || parsed.ageCategory() == TeamSignatureNormalizer.AgeCategory.MAIN)) {
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
                TeamSignatureNormalizer.ParsedSignature rawSig = TeamSignatureNormalizer.parse(teamName, tournamentContext, null);
                String rawClean = toSlug(rawSig.coreClubName()).replaceAll("[^a-z]", "");

                for (MdmTeam candidate : rosterTeams) {
                    if (!Objects.equals(candidate.getSport().getId(), sportId)) continue;
                    TeamSignatureNormalizer.ParsedSignature candSig = TeamSignatureNormalizer.parse(candidate.getCanonicalName(), contextTournament.getCanonicalName(), null);
                    if (!rawSig.isStrictlyCompatible(candSig)) continue;

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
                    if (!match && !rawClean.isBlank() && !"unknown".equals(rawClean)) {
                        String candClean = toSlug(candSig.coreClubName()).replaceAll("[^a-z]", "");
                        if (!candClean.isBlank() && !"unknown".equals(candClean)) {
                            boolean isRawGeneric = SimilarityUtils.GENERIC_CLUB_WORDS.contains(rawClean);
                            boolean isCandGeneric = SimilarityUtils.GENERIC_CLUB_WORDS.contains(candClean);
                            if (!isRawGeneric && !isCandGeneric) {
                                match = candClean.equals(rawClean) || (rawClean.length() >= 6 && candClean.contains(rawClean)) || (candClean.length() >= 6 && rawClean.contains(candClean));
                                if (!match) {
                                    String candPhonetic = candClean.replace('s', 'z');
                                    String rawPhonetic = rawClean.replace('s', 'z');
                                    match = candPhonetic.equals(rawPhonetic) || (rawPhonetic.length() >= 6 && candPhonetic.contains(rawPhonetic)) || (candPhonetic.length() >= 6 && rawPhonetic.contains(candPhonetic));
                                }
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

        final int targetSportId = sportId;
        MdmSport sport = findSportByIdCached(targetSportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found with ID: " + targetSportId));

        String normSig = toSlug(parsed != null ? parsed.coreClubName() : teamName);
        String ageSuffix = (expectedAge != null && !"SENIOR".equalsIgnoreCase(expectedAge)) ? "-" + expectedAge.toLowerCase() : "";
        String genderSuffix = "FEMALE".equalsIgnoreCase(expectedGender) && !toSlug(teamName).contains("women") && !toSlug(teamName).contains("zhen") ? "-women" : "";
        String slug = toSlug(teamName) + ageSuffix + genderSuffix + (sofaId != null ? "-" + sofaId : "");
        Optional<MdmTeam> existingSlug = teamRepository.findBySportIdAndSlug(sportId, slug);
        if (existingSlug.isPresent()) {
            MdmTeam team = existingSlug.get();
            boolean genderMatch = team.getGender() == null || team.getGender().equalsIgnoreCase(expectedGender);
            boolean ageMatch = isAgeCompatible(team.getAgeCategory(), expectedAge);
            if (!ageMatch && "SENIOR".equalsIgnoreCase(expectedAge)) {
                // If existing team has bogus age in DB (e.g. U20 on Boca Juniors, cyber teams, or junior club brands)
                // and has no explicit age in its canonical name, heal it to SENIOR
                if (TeamSignatureNormalizer.isJuniorClubBrand(team.getCanonicalName().toLowerCase())
                        || sportId == 37 || sportId == 41 || sportId == 42 || sportId == 6
                        || TeamSignatureNormalizer.detectAgeCategory(team.getCanonicalName()) == null) {
                    log.info("🛡️ [MDM-HEAL] Self-healing team #{} '{}' age {} -> SENIOR from slug resolution (sport={})",
                            team.getId(), team.getCanonicalName(), team.getAgeCategory(), sportId);
                    team.setAgeCategory("SENIOR");
                    try {
                        team = teamRepository.save(team);
                        ageMatch = true;
                    } catch (Exception ignored) {}
                }
            }
            if (genderMatch && ageMatch) {
                if (contextTournament != null) registerTournamentParticipant(contextTournament, team);
                saveTeamIngestionMap(request.getCreatedBySource(), teamName, team);
                enrichTeamCountry(team, countryName);
                return finalizeTeamResolution(team, sofaId);
            }
            // If still not matching, differentiate slug to avoid uk_mdm_team_sport_slug collision
            slug = slug + "-" + (expectedAge != null ? expectedAge.toLowerCase() : "alt") + "-" + java.util.UUID.randomUUID().toString().substring(0, 6);
        }

        String origin = sanitizeEntityOrigin(request.getSourceOrigin(), sofaId);

        String effectiveCountry = countryName != null ? countryName : (category != null ? category.getSlug() : null);
        if (resolvedTeamCountry == null && effectiveCountry != null) {
            resolvedTeamCountry = resolveCountryEntity(effectiveCountry, false);
        }

        String canonicalName = teamName;
        if ("FEMALE".equalsIgnoreCase(expectedGender) && (parsed == null || parsed.gender() == TeamSignatureNormalizer.Gender.MALE)) {
            canonicalName = canonicalName + " (ж)";
        }
        if (expectedAge != null && !"SENIOR".equalsIgnoreCase(expectedAge) && (parsed == null || parsed.ageCategory() == TeamSignatureNormalizer.AgeCategory.MAIN)) {
            canonicalName = canonicalName + " " + expectedAge;
        }

        MdmTeam newTeam = MdmTeam.builder()
                .sport(sport)
                .category(category)
                .canonicalName(canonicalName)
                .slug(slug)
                .sofascoreId(sofaId)
                .normalizedSignature(normSig)
                .gender(expectedGender)
                .ageCategory(expectedAge)
                .countryName(effectiveCountry)
                .country(resolvedTeamCountry)
                .sourceOrigin(origin)
                .build();
        try {
            newTeam = teamRepository.save(newTeam);
        } catch (org.springframework.dao.DataIntegrityViolationException dive) {
            log.warn("⚠️ [MDM] Collision saving team slug '{}': attempting recovery via findBySportIdAndSlug", slug);
            Optional<MdmTeam> recovered = teamRepository.findBySportIdAndSlug(sportId, slug);
            if (recovered.isPresent()) {
                newTeam = recovered.get();
            } else {
                newTeam.setSlug(slug + "-" + java.util.UUID.randomUUID().toString().substring(0, 6));
                newTeam = teamRepository.save(newTeam);
            }
        }
        log.info("🌟 [MDM] Auto-created canonical team #{} '{}' (sport={}, country={}, category={}, sofaId={}) from {}",
                newTeam.getId(), newTeam.getCanonicalName(), sportId, countryName, category != null ? category.getSlug() : "none", sofaId, origin);

        if (contextTournament != null) {
            registerTournamentParticipant(contextTournament, newTeam);
        }
        saveTeamIngestionMap(request.getCreatedBySource(), teamName, newTeam);
        return toTeamDto(newTeam);
    }


    @Transactional(readOnly = true)
    public Optional<CanonicalTeamDto> getTeamById(Long id) {
        return teamRepository.findById(id).map(MdmTeamResolutionService::toTeamDto);
    }



    public static CanonicalTeamDto toTeamDto(MdmTeam team) {
        if (team == null) return null;
        String effGender = team.getGender();
        if ("MALE".equalsIgnoreCase(effGender)) {
            String explicitGender = TeamSignatureNormalizer.detectGender(team.getCanonicalName());
            if ("FEMALE".equals(explicitGender) || TeamSignatureNormalizer.isFemaleContext(team.getCanonicalName())) {
                effGender = "FEMALE";
            }
        }
        String effAge = team.getAgeCategory();
        if ("SENIOR".equalsIgnoreCase(effAge) && team.getCanonicalName() != null) {
            String explicitAge = TeamSignatureNormalizer.detectAgeCategory(team.getCanonicalName());
            if (explicitAge != null) {
                effAge = explicitAge;
            } else {
                var parsed = TeamSignatureNormalizer.parse(team.getCanonicalName(), null, null);
                if (parsed != null && parsed.ageCategory() != TeamSignatureNormalizer.AgeCategory.MAIN) {
                    effAge = parsed.ageCategory().name();
                }
            }
        }
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
                .ageCategory(effAge)
                .gender(effGender)
                .sofascoreId(team.getSofascoreId())
                .sourceOrigin(team.getSourceOrigin())
                .logoUrl(team.getLogoUrl())
                .logoSource(team.getLogoSource())
                .isOfficialLogo(team.getIsOfficialLogo())
                .build();
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



    public MdmTeam resolveCanonicalTeam(MdmTeam team) {
        if (team == null) return null;
        if (teamMergeMapRepository != null) {
            Long sofaId = remapTeamId(team.getId());
            if (!Objects.equals(sofaId, team.getId())) {
                team = teamRepository.findById(sofaId).orElse(team);
            }
        }
        healTeamMetadataIfNeeded(team);
        return team;
    }



    private CanonicalTeamDto finalizeTeamResolution(MdmTeam team, Long sofaId) {
        team = resolveCanonicalTeam(team);
        if (team != null) {
            healTeamMetadataIfNeeded(team);
            if (sofaId != null && team.getSofascoreId() == null) {
                enrichTeamSofascoreId(team, sofaId);
            }
        }
        return toTeamDto(team);
    }



    private void healTeamMetadataIfNeeded(MdmTeam team) {
        if (team == null || team.getCanonicalName() == null) return;
        boolean changed = false;
        var parsed = TeamSignatureNormalizer.parse(team.getCanonicalName(), null, null);
        if (parsed != null) {
            if ("MALE".equalsIgnoreCase(team.getGender()) && parsed.gender() == TeamSignatureNormalizer.Gender.FEMALE) {
                team.setGender("FEMALE");
                changed = true;
                log.info("🛡️ [MDM-HEAL] Self-healed team #{} '{}' gender MALE -> FEMALE", team.getId(), team.getCanonicalName());
            }
            if (parsed.ageCategory() != TeamSignatureNormalizer.AgeCategory.MAIN
                    && !parsed.ageCategory().name().equalsIgnoreCase(team.getAgeCategory())) {
                team.setAgeCategory(parsed.ageCategory().name());
                changed = true;
                log.info("🛡️ [MDM-HEAL] Self-healed team #{} '{}' age {} -> {}", team.getId(), team.getCanonicalName(), team.getAgeCategory(), parsed.ageCategory().name());
            }
        }
        if (changed) {
            try {
                teamRepository.save(team);
            } catch (Exception e) {
                log.warn("⚠️ [MDM-HEAL] Failed to persist healed metadata for team #{}: {}", team.getId(), e.getMessage());
            }
        }
    }



    public void enrichTeamSofascoreId(MdmTeam team, Long sofaId) {
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


    public void saveTeamIngestionMap(String systemCode, String rawKey, MdmTeam team) {
        saveTeamIngestionMap(systemCode, rawKey, null, team);
    }

    public void saveTeamIngestionMap(String systemCode, String rawKey, String sourceId, MdmTeam team) {
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


    public void healTeamMetadataWithTournamentContext(MdmTeam team, MdmTournament tournament) {
        healTeamMetadataWithTournamentContext(team, tournament, null);
    }

    public void healTeamMetadataWithTournamentContext(MdmTeam team, MdmTournament tournament, String sourceOrigin) {
        if (team == null) return;
        boolean changed = false;
        String name = team.getCanonicalName();
        String lowerName = name != null ? name.toLowerCase() : "";

        // 1. Self-heal gender from team's own canonical name
        if ("MALE".equalsIgnoreCase(team.getGender())) {
            String explicitGender = TeamSignatureNormalizer.detectGender(name);
            if ("FEMALE".equals(explicitGender) || TeamSignatureNormalizer.isFemaleContext(name)) {
                team.setGender("FEMALE");
                changed = true;
                log.info("🛡️ [MDM-HEAL] Self-healed team #{} '{}' gender MALE -> FEMALE from name", team.getId(), name);
            }
        }

        // 2. Self-heal gender from tournament context
        if (tournament != null) {
            if (!"FEMALE".equalsIgnoreCase(tournament.getGender()) && !"MIXED".equalsIgnoreCase(tournament.getGender())
                    && TeamSignatureNormalizer.isFemaleContext(tournament.getCanonicalName())) {
                tournament.setGender("FEMALE");
                if (tournamentRepository != null && tournament.getId() != null) {
                    tournamentRepository.save(tournament);
                }
                log.info("🛡️ [MDM-HEAL] Self-healed tournament #{} '{}' gender -> FEMALE from tournament name",
                        tournament.getId(), tournament.getCanonicalName());
            }
        }
        if (tournament != null && "FEMALE".equalsIgnoreCase(tournament.getGender()) && !"FEMALE".equalsIgnoreCase(team.getGender())) {
            Integer sId = tournament.getSport() != null ? tournament.getSport().getId() : (team.getSport() != null ? team.getSport().getId() : null);
            boolean isIndividual = sId != null && (sId == 4 || sId == 5 || sId == 13 || sId == 14 || sId == 15 || sId == 25);
            boolean isOfficialRosterParticipant = tournamentTeamRepository != null && tournament.getId() != null && team.getId() != null
                    && tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), team.getId());
            boolean participatesInFemaleLeague = tournamentTeamRepository != null && team.getId() != null
                    && tournamentTeamRepository.existsByTeamIdAndFemaleTournament(team.getId());
            boolean isSofaScoreFemaleContext = tournament.getSofascoreId() != null
                    && ("SOFASCORE".equalsIgnoreCase(sourceOrigin) || TeamSignatureNormalizer.isFemaleContext(tournament.getCanonicalName()));

            if (isIndividual || isOfficialRosterParticipant || participatesInFemaleLeague || isSofaScoreFemaleContext || TeamSignatureNormalizer.isFemaleContext(name)) {
                team.setGender("FEMALE");
                changed = true;
                log.info("🛡️ [MDM-HEAL] Self-healed team #{} '{}' gender MALE -> FEMALE from tournament context '{}'",
                        team.getId(), name, tournament.getCanonicalName());
            }
        }

        // 3. Self-heal age category from team's own canonical name
        if ("SENIOR".equalsIgnoreCase(team.getAgeCategory()) && name != null && !TeamSignatureNormalizer.isJuniorClubBrand(lowerName)) {
            if (lowerName.endsWith("-м") || lowerName.contains("(мол)") || lowerName.contains("(юн)")) {
                team.setAgeCategory("U21");
                changed = true;
                log.info("🛡️ [MDM-HEAL] Self-healed team #{} '{}' age SENIOR -> U21 from name", team.getId(), name);
            } else if (lowerName.endsWith("(рез)")) {
                team.setAgeCategory("RESERVE");
                changed = true;
                log.info("🛡️ [MDM-HEAL] Self-healed team #{} '{}' age SENIOR -> RESERVE from name", team.getId(), name);
            } else {
                String explicitAge = TeamSignatureNormalizer.detectAgeCategory(name);
                if (explicitAge != null) {
                    team.setAgeCategory(explicitAge);
                    changed = true;
                    log.info("🛡️ [MDM-HEAL] Self-healed team #{} '{}' age SENIOR -> {} from name", team.getId(), name, explicitAge);
                } else {
                    var parsed = TeamSignatureNormalizer.parse(name, null, null);
                    if (parsed != null && !TeamSignatureNormalizer.isJuniorClubBrand(lowerName) && parsed.ageCategory() != TeamSignatureNormalizer.AgeCategory.MAIN) {
                        team.setAgeCategory(parsed.ageCategory().name());
                        changed = true;
                        log.info("🛡️ [MDM-HEAL] Self-healed team #{} '{}' age SENIOR -> {} from parsed signature",
                                team.getId(), name, parsed.ageCategory().name());
                    }
                }
            }
        }

        // 4. Self-heal team country from authoritative domestic league context
        if (tournament != null && tournament.getCountry() != null
                && !Boolean.TRUE.equals(tournament.getIsCup())
                && integrityValidator != null && !integrityValidator.isInternationalTournament(tournament)
                && tournament.getSofascoreId() != null) {
            boolean isRosterParticipant = tournamentTeamRepository != null && tournament.getId() != null && team.getId() != null
                    && (tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), team.getId())
                        || tournamentTeamRepository.existsByTeamIdAndCountryId(team.getId(), tournament.getCountry().getId()));
            boolean isSofaScoreDomesticMatch = "SOFASCORE".equalsIgnoreCase(sourceOrigin);

            if ((isRosterParticipant || isSofaScoreDomesticMatch) && !integrityValidator.isCountryCompatible(tournament, team)) {
                log.info("🛡️ [MDM-HEAL] Self-healed team #{} '{}' country {} (#{}) -> {} (#{}) from official domestic league '{}'",
                        team.getId(), name,
                        team.getCountry() != null ? team.getCountry().getCanonicalName() : "null",
                        team.getCountry() != null ? team.getCountry().getId() : 0,
                        tournament.getCountry().getCanonicalName(), tournament.getCountry().getId(),
                        tournament.getCanonicalName());
                team.setCountry(tournament.getCountry());
                team.setCountryName(tournament.getCountry().getCanonicalName());
                changed = true;
            }
        }

        // 5. Self-heal team category from tournament if team has generic placeholder category and tournament has country category
        if (tournament != null && tournament.getCategory() != null && team.getCategory() != null) {
            boolean teamCatGeneric = MatchIntegrityValidator.isGenericCategory(team.getCategory());
            boolean tournCatGeneric = MatchIntegrityValidator.isGenericCategory(tournament.getCategory());
            if (teamCatGeneric && !tournCatGeneric) {
                if (team.getCountry() != null && tournament.getCountry() != null
                        && Objects.equals(team.getCountry().getId(), tournament.getCountry().getId())) {
                    team.setCategory(tournament.getCategory());
                    changed = true;
                    log.info("🛡️ [MDM-HEAL] Self-healed team #{} '{}' category -> {} (#{})",
                            team.getId(), name, tournament.getCategory().getSlug(), tournament.getCategory().getId());
                }
            }
        }

        if (changed && teamRepository != null) {
            try {
                teamRepository.save(team);
            } catch (Exception e) {
                log.warn("Failed to persist healed team metadata for #{}: {}", team.getId(), e.getMessage());
            }
        }
    }




    public MdmCategory resolveCategory(Integer sportId, String countryName, String categoryName, Boolean isInternational) {
        if (tournamentResolutionService != null) {
            return tournamentResolutionService.resolveCategory(sportId, countryName, categoryName, isInternational);
        }
        if (categoryRepository == null || sportId == null) return null;
        String target = countryName != null && !countryName.isBlank() ? countryName.trim() : (categoryName != null ? categoryName.trim() : null);
        CountryNormalizer.CountryInfo ci = CountryNormalizer.normalize(target, isInternational);
        return ci != null ? categoryRepository.findFirstBySportIdAndSlugIgnoreCase(sportId, ci.slug()).orElse(null) : null;
    }

    public MdmCountry resolveCountryEntity(String systemCode, String rawCategoryKey, String countryName, String categoryName, Boolean isInternational) {
        if (tournamentResolutionService != null) {
            return tournamentResolutionService.resolveCountryEntity(systemCode, rawCategoryKey, countryName, categoryName, isInternational);
        }
        return resolveCountryEntity(countryName != null ? countryName : categoryName, isInternational);
    }

    public MdmCountry resolveCountryEntity(String countryName, Boolean isInternational) {
        if (tournamentResolutionService != null) {
            return tournamentResolutionService.resolveCountryEntity(countryName, isInternational);
        }
        if (countryRepository == null || countryName == null || countryName.isBlank()) return null;
        CountryNormalizer.CountryInfo ci = CountryNormalizer.normalize(countryName, isInternational);
        return ci != null && ci.code() != null ? countryRepository.findFirstByIsoCodeIgnoreCase(ci.code()).orElse(null) : null;
    }

    public void registerTournamentParticipant(MdmTournament tournament, MdmTeam team) {
        if (tournamentResolutionService != null) {
            tournamentResolutionService.registerTournamentParticipant(tournament, team);
        } else if (tournamentTeamRepository != null && tournament != null && team != null && tournament.getId() != null && team.getId() != null) {
            if (!tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), team.getId())) {
                try {
                    tournamentTeamRepository.save(MdmTournamentTeam.builder().tournament(tournament).team(team).build());
                } catch (Exception ignored) {}
            }
        }
    }

    public static boolean isLexicallyCompatible(String rawKey, String canonicalName) {
        return MdmTournamentResolutionService.isLexicallyCompatible(rawKey, canonicalName);
    }

    public boolean isInternationalTournament(MdmTournament tournament) {
        return integrityValidator.isInternationalTournament(tournament);
    }

    public boolean isCountryCompatible(MdmTournament tournament, MdmTeam team) {
        return integrityValidator.isCountryCompatible(tournament, team);
    }

    public static String toSlug(String input) {
        return MdmTournamentResolutionService.toSlug(input);
    }

    public static boolean hasCyrillic(String input) {
        return MdmTournamentResolutionService.hasCyrillic(input);
    }

    public static String sanitizeEntityOrigin(String rawOrigin, Long sofaId) {
        return MdmTournamentResolutionService.sanitizeEntityOrigin(rawOrigin, sofaId);
    }

    public static boolean isAgeCompatible(String teamAge, String expectedAge) {
        return MatchIntegrityValidator.isAgeCompatible(teamAge, expectedAge, null);
    }
}
