package pro.datawiki.igaming.mdm.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.CanonicalTournamentDto;
import pro.datawiki.igaming.dto.TournamentResolutionRequest;
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.mdm.domain.*;
import pro.datawiki.igaming.mdm.repository.*;
import pro.datawiki.igaming.mdm.util.CountryNormalizer;
import pro.datawiki.igaming.mdm.util.TournamentStageNormalizer;

import java.text.Normalizer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Dedicated service for resolving, creating, and managing canonical MDM tournaments
 * and category/country hierarchies across all supported sports.
 */
@Service
@Slf4j
public class MdmTournamentResolutionService {

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

    private static final java.util.Set<String> GENERIC_TOURNAMENT_WORDS = java.util.Set.of(
            "cup", "league", "division", "championship", "tournament", "open", "trophy", "supercup", "pokal", "copa", "coppa", "coupe",
            "women", "woman", "ladies", "girls", "men", "man", "mixed", "juniors", "youth",
            "kubok", "liga", "divizion", "chempionat", "turnir", "pervenstvo", "seriya", "superkubok", "trofej",
            "zhenshchiny", "zhen", "zhenshchin", "zhenskaya", "zhenskij", "muzhchiny", "muzh", "muzhchin", "muzhskaya", "muzhskoj",
            "yuniory", "yunoshi", "molodezh", "molodezhnaya", "rezerv", "tovarishcheskij", "friendly", "club", "klubnyj",
            "etap", "raund", "round", "stage", "playoff", "plej", "off", "kvalifikaciya", "qualification",
            "1st", "2nd", "3rd", "first", "second", "third", "premier", "premer", "vysshaya", "pervaya", "vtoraya"
    );

    private final MdmTournamentRepository tournamentRepository;
    private final MdmCategoryRepository categoryRepository;
    private final MdmCountryRepository countryRepository;
    private final MdmCountryIngestionMapRepository countryIngestionMapRepository;
    private final MdmTournamentIngestionMapRepository tournamentIngestionMapRepository;
    private final MdmTournamentTranslationRepository tournamentTranslationRepository;
    private final MdmTournamentTeamRepository tournamentTeamRepository;
    private final MdmSportRepository sportRepository;
    private final MdmExternalSystemRepository externalSystemRepository;
    private final MatchIntegrityValidator integrityValidator;

    private final Map<String, MdmSport> sportBySlugCache = new ConcurrentHashMap<>();
    private final Map<Integer, MdmSport> sportByIdCache = new ConcurrentHashMap<>();
    private final Map<String, MdmExternalSystem> externalSystemByCodeCache = new ConcurrentHashMap<>();

    private final Cache<String, CanonicalTournamentDto> tournamentResolutionCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(15))
                    .maximumSize(50_000)
                    .build();

    @Autowired
    public MdmTournamentResolutionService(
            MdmTournamentRepository tournamentRepository,
            @Autowired(required = false) MdmCategoryRepository categoryRepository,
            @Autowired(required = false) MdmCountryRepository countryRepository,
            @Autowired(required = false) MdmCountryIngestionMapRepository countryIngestionMapRepository,
            @Autowired(required = false) MdmTournamentIngestionMapRepository tournamentIngestionMapRepository,
            @Autowired(required = false) MdmTournamentTranslationRepository tournamentTranslationRepository,
            @Autowired(required = false) MdmTournamentTeamRepository tournamentTeamRepository,
            @Autowired(required = false) MdmSportRepository sportRepository,
            @Autowired(required = false) MdmExternalSystemRepository externalSystemRepository,
            @Autowired(required = false) MatchIntegrityValidator integrityValidator
    ) {
        this.tournamentRepository = tournamentRepository;
        this.categoryRepository = categoryRepository;
        this.countryRepository = countryRepository;
        this.countryIngestionMapRepository = countryIngestionMapRepository;
        this.tournamentIngestionMapRepository = tournamentIngestionMapRepository;
        this.tournamentTranslationRepository = tournamentTranslationRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.sportRepository = sportRepository;
        this.externalSystemRepository = externalSystemRepository;
        this.integrityValidator = integrityValidator != null ? integrityValidator
                : new MatchIntegrityValidator(null, tournamentTeamRepository, null);
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

        // Canonical Discipline Invariant: resolve true sport from slug and cyber invariants
        if (request.getSportSlug() != null && !request.getSportSlug().isBlank()) {
            pro.datawiki.igaming.dto.sport.Sport resolvedSport = pro.datawiki.igaming.dto.sport.Sports.get(request.getSportSlug());
            if (resolvedSport != null && resolvedSport != pro.datawiki.igaming.dto.sport.Sports.UNKNOWN && resolvedSport != pro.datawiki.igaming.dto.sport.Sports.ESPORTS) {
                int canonicalId = pro.datawiki.igaming.dto.sport.Sports.getId(resolvedSport);
                if (canonicalId > 0 && canonicalId != sportId) {
                    sportId = canonicalId;
                    request.setSportId(canonicalId);
                }
            }
        }
        if (tournName != null && pro.datawiki.igaming.dto.signature.CyberMatchDetector.isCyberLeague(tournName)) {
            if (sportId == 1 || sportId == 6) {
                sportId = 37; // CYBER_FOOTBALL
                request.setSportId(37);
            } else if (sportId == 3) {
                sportId = 41; // CYBER_BASKETBALL
                request.setSportId(41);
            } else if (sportId == 2) {
                sportId = 42; // CYBER_HOCKEY
                request.setSportId(42);
            }
        }

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
        if (resolvedCountry == null && tournName != null && !Boolean.TRUE.equals(isIntl)) {
            CountryNormalizer.CountryInfo extracted = CountryNormalizer.extractCountryFromTournament(tournName);
            if (extracted != null && !extracted.isInternational()) {
                resolvedCountry = resolveCountryEntity(extracted.slug(), false);
            }
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
                        if (Objects.equals(mappedTourn.getSport().getId(), sportId) && isTournamentMappingAcceptable(request.getRawKey(), tournName, mappedTourn, countryCtx, resolvedCountry)) {
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
                        if (Objects.equals(mappedTourn.getSport().getId(), sportId) && isTournamentMappingAcceptable(request.getRawKey(), tournName, mappedTourn, countryCtx, resolvedCountry)) {
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
                    if (Objects.equals(mappedTourn.getSport().getId(), sportId) && isTournamentMappingAcceptable(request.getRawKey(), tournName, mappedTourn, countryCtx, resolvedCountry)) {
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
            String explicitGender = TeamSignatureNormalizer.detectGender(tournName);
            String detectedGender = explicitGender != null ? explicitGender
                    : (request.getGender() != null ? request.getGender().toUpperCase() : "MALE");
            String explicitAge = TeamSignatureNormalizer.detectAgeCategory(tournName);
            String detectedAge = explicitAge != null ? explicitAge
                    : (request.getAgeCategory() != null ? request.getAgeCategory().toUpperCase() : "SENIOR");

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

                    // Strict Country Guard for fuzzy tournament matching:
                    // 1. If both candidate and incoming tournament have known countries, they MUST match
                    if (cand.getCountry() != null && resolvedCountryId != null && !cand.getCountry().getId().equals(resolvedCountryId)) {
                        continue;
                    }
                    // 2. If candidate has a country but resolvedCountryId is null, inspect extracted tournament country
                    if (cand.getCountry() != null && resolvedCountryId == null) {
                        CountryNormalizer.CountryInfo extractedTournCountry = CountryNormalizer.extractCountryFromTournament(tournName);
                        if (extractedTournCountry != null && !extractedTournCountry.isInternational()) {
                            if (cand.getCountry().getCanonicalName() == null
                                    || !extractedTournCountry.slug().equalsIgnoreCase(cand.getCountry().getCanonicalName())) {
                                continue;
                            }
                        } else if (isAmbiguousLeagueName(tournName)) {
                            // Generic league name without country must never fuzzy match a domestic competition
                            continue;
                        }
                    }

                    boolean exactMatch = cand.getCanonicalName().equalsIgnoreCase(tournName) || cand.getSlug().equals(toSlug(tournName));
                    if (exactMatch) {
                        log.info("🎯 [MDM] Matched incoming tournament '{}' to canonical tournament #{} '{}'",
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

        // Strict Discipline Invariant: Cyber tournaments (FIFA/FC24-26/Volta/eAdriatic) must never leak into physical sports
        if (pro.datawiki.igaming.dto.signature.CyberMatchDetector.isCyberLeague(tournName)) {
            if (sportId != 37) {
                log.warn("[MDM-SPORT-INTEGRITY] Reassigning cyber league '{}' from sport #{} to CYBER_FOOTBALL (#37)", tournName, sportId);
                sportId = 37;
                request.setSportId(37);
            }
        }

        final int targetSportId = sportId;
        MdmSport sport = findSportByIdCached(targetSportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found with ID: " + targetSportId));

        boolean isAmbiguous = isAmbiguousLeagueName(tournName);
        String canonicalName = (isAmbiguous && countryCtx != null) ? (countryCtx + ". " + tournName) : tournName;
        String slug = toSlug(canonicalName) + (sofaId != null ? "-" + sofaId : "");

        synchronized ((sportId + ":" + slug).intern()) {
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

            String explicitGender = TeamSignatureNormalizer.detectGender(tournName);
            String detectedGender = explicitGender != null ? explicitGender
                    : (request.getGender() != null ? request.getGender().toUpperCase() : "MALE");
            String explicitAge = TeamSignatureNormalizer.detectAgeCategory(tournName);
            String detectedAge = explicitAge != null ? explicitAge
                    : (request.getAgeCategory() != null ? request.getAgeCategory().toUpperCase() : "SENIOR");

            boolean isCup = Boolean.TRUE.equals(request.getIsCup());

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
            try {
                MdmTournament saved = tournamentRepository != null ? tournamentRepository.save(newTourn) : newTourn;
                if (saved != null) {
                    newTourn = saved;
                }
                if (newTourn.getId() == null) {
                    newTourn.setId(System.nanoTime());
                }
            } catch (org.springframework.dao.DataIntegrityViolationException dive) {
                log.warn("⚠️ [MDM] Collision saving tournament slug '{}': attempting recovery via findFirstBySportIdAndSlug", slug);
                Optional<MdmTournament> recovered = tournamentRepository.findFirstBySportIdAndSlug(sportId, slug);
                if (recovered.isPresent()) {
                    newTourn = recovered.get();
                } else {
                    newTourn.setSlug(slug + "-" + java.util.UUID.randomUUID().toString().substring(0, 6));
                    newTourn = tournamentRepository.save(newTourn);
                }
            }
            log.info("🌟 [MDM] Auto-created canonical tournament #{} '{}' (sport={}, country={}, sofaId={}) from {}",
                    newTourn.getId(), newTourn.getCanonicalName(), sportId, countryCtx, sofaId, origin);

            saveTournamentIngestionMap(request.getCreatedBySource(), buildIngestionKey(tournName, countryCtx), newTourn);
            return toTournamentDto(newTourn);
        }
    }


    @Transactional(readOnly = true)
    public Optional<CanonicalTournamentDto> getTournamentById(Long id) {
        return tournamentRepository.findById(id).map(MdmTournamentResolutionService::toTournamentDto);
    }



    public static CanonicalTournamentDto toTournamentDto(MdmTournament t) {
        if (t == null) return null;
        String effGender = t.getGender();
        String explicitGender = TeamSignatureNormalizer.detectGender(t.getCanonicalName());
        if (explicitGender != null) {
            effGender = explicitGender;
        } else if ("MALE".equalsIgnoreCase(effGender) && TeamSignatureNormalizer.isFemaleContext(t.getCanonicalName())) {
            effGender = "FEMALE";
        }
        String effAge = t.getAgeCategory();
        if ("SENIOR".equalsIgnoreCase(effAge) && t.getCanonicalName() != null) {
            String explicitAge = TeamSignatureNormalizer.detectAgeCategory(t.getCanonicalName());
            if (explicitAge != null) {
                effAge = explicitAge;
            } else {
                var parsed = TeamSignatureNormalizer.parse(t.getCanonicalName(), null, null);
                if (parsed != null && parsed.ageCategory() != TeamSignatureNormalizer.AgeCategory.MAIN) {
                    effAge = parsed.ageCategory().name();
                }
            }
        }
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
                .ageCategory(effAge)
                .gender(effGender)
                .isCup(t.getIsCup())
                .isInternational(t.getCategory() != null && "international".equalsIgnoreCase(t.getCategory().getSlug()))
                .sofascoreId(t.getSofascoreId())
                .sourceOrigin(t.getSourceOrigin())
                .tier(t.getTier())
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
            Integer sportId = tournament.getSport() != null ? tournament.getSport().getId()
                    : (team.getSport() != null ? team.getSport().getId() : 1);
            tournamentTeamRepository.registerParticipantIdempotent(tournament.getId(), team.getId(), sportId);
            log.info("🛡️ [MDM-ROSTER] Registered team '{}' (#{}) as participant of tournament '{}' (#{})",
                    team.getCanonicalName(), team.getId(), tournament.getCanonicalName(), tournament.getId());
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


    public static String detectTournamentGender(String lowerTourn) {
        return TeamSignatureNormalizer.detectGender(lowerTourn);
    }

    @Deprecated
    public static String detectTournamentAge(String lowerTourn) {
        return TeamSignatureNormalizer.detectAgeCategory(lowerTourn);
    }


    public void healTournamentMetadataIfNeeded(MdmTournament tourney) {
        if (tourney == null || tourney.getCanonicalName() == null) return;
        boolean changed = false;
        String name = tourney.getCanonicalName();
        if ("MALE".equalsIgnoreCase(tourney.getGender()) && TeamSignatureNormalizer.isFemaleContext(name)) {
            tourney.setGender("FEMALE");
            changed = true;
            log.info("🛡️ [MDM-HEAL] Self-healed tournament #{} '{}' gender MALE -> FEMALE", tourney.getId(), name);
        }
        if ("SENIOR".equalsIgnoreCase(tourney.getAgeCategory())) {
            String explicitAge = TeamSignatureNormalizer.detectAgeCategory(name);
            if (explicitAge != null) {
                tourney.setAgeCategory(explicitAge);
                changed = true;
                log.info("🛡️ [MDM-HEAL] Self-healed tournament #{} '{}' age SENIOR -> {}", tourney.getId(), name, explicitAge);
            } else {
                var parsed = TeamSignatureNormalizer.parse(name, null, null);
                if (parsed != null && parsed.ageCategory() != TeamSignatureNormalizer.AgeCategory.MAIN) {
                    tourney.setAgeCategory(parsed.ageCategory().name());
                    changed = true;
                    log.info("🛡️ [MDM-HEAL] Self-healed tournament #{} '{}' age SENIOR -> {}", tourney.getId(), name, parsed.ageCategory().name());
                }
            }
        }
        if (changed && tournamentRepository != null) {
            try {
                tournamentRepository.save(tourney);
            } catch (Exception e) {
                log.warn("⚠️ [MDM-HEAL] Failed to persist healed metadata for tournament #{}: {}", tourney.getId(), e.getMessage());
            }
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


    public static boolean isLexicallyCompatible(String rawKey, String canonicalName) {
        if (rawKey == null || canonicalName == null) return false;
        String rawNorm = toSlug(rawKey);
        String canNorm = toSlug(canonicalName);
        if (rawNorm.equals(canNorm)) return true;
        return rawNorm.contains(canNorm) || canNorm.contains(rawNorm);
    }

    private static final Pattern GENERIC_STAGE_OR_FORMAT_PATTERN = Pattern.compile(
            "(?ui)^(?:(?:мужчины|женщины|men|women|мужчины\\.|женщины\\.|\\(мужчины\\)|\\(женщины\\)|групповой\\s*этап|плей-офф|кубок|cup|singles?|doubles?|singel|финал|полуфинал|1/\\d+\\s*финала|региональный\\s*дивизион|региональная\\s*лига|regional\\s*league|regional\\s*division|группа\\s*[a-zа-я0-9]|group\\s*[a-z0-9]|men\\s*singles?|women\\s*singles?|men\\s*doubles?|women\\s*doubles?|суперлига|премьер-лига|высшая\\s*лига|первая\\s*лига|вторая\\s*лига|1-й\\s*дивизион|2-й\\s*дивизион|3-й\\s*дивизион|super\\s*league|premier\\s*league|1st\\s*division|2nd\\s*division|championship|до\\s*\\d+\\s*(?:побед|матчей))[\\s.,-]*)+$"
    );


    public boolean isTournamentMappingAcceptable(String rawKey, String tournName, MdmTournament mappedTourn) {
        return isTournamentMappingAcceptable(rawKey, tournName, mappedTourn, null, null);
    }

    public boolean isTournamentMappingAcceptable(String rawKey, String tournName, MdmTournament mappedTourn, String countryCtx, MdmCountry countryEntity) {
        if (mappedTourn == null) return false;
        String raw = rawKey != null && !rawKey.isBlank() ? rawKey : tournName;
        if (raw == null || raw.isBlank()) return true;
        String trimmed = raw.trim();
        if (GENERIC_STAGE_OR_FORMAT_PATTERN.matcher(trimmed).matches()) {
            log.warn("🛡️ [MDM-GUARD] Bypassing corrupt historical tournament mapping: rawKey='{}' -> tournament #{} '{}' (generic stage/format)",
                    trimmed, mappedTourn.getId(), mappedTourn.getCanonicalName());
            return false;
        }
        boolean rawIsFemale = TeamSignatureNormalizer.isFemaleContext(trimmed);
        String mappedGender = normalizeGender(mappedTourn.getGender());
        if (rawIsFemale && "MALE".equalsIgnoreCase(mappedGender)) {
            log.warn("🛡️ [MDM-GUARD] Bypassing corrupt historical tournament mapping: rawKey='{}' -> MALE tournament #{} '{}'",
                    trimmed, mappedTourn.getId(), mappedTourn.getCanonicalName());
            return false;
        }
        if (!rawIsFemale && "FEMALE".equalsIgnoreCase(mappedGender)) {
            log.warn("🛡️ [MDM-GUARD] Bypassing corrupt historical tournament mapping: male rawKey='{}' -> FEMALE tournament #{} '{}'",
                    trimmed, mappedTourn.getId(), mappedTourn.getCanonicalName());
            return false;
        }

        // 3. Country mismatch check for domestic tournaments
        if (mappedTourn.getCountry() != null && !Boolean.TRUE.equals(mappedTourn.getIsCup())
                && (integrityValidator == null || !integrityValidator.isInternationalTournament(mappedTourn))) {
            MdmCountry expectedCountry = countryEntity;
            if (expectedCountry == null && countryCtx != null && !countryCtx.isBlank() && countryRepository != null) {
                expectedCountry = resolveCountryEntity(countryCtx, false);
            }
            if (expectedCountry == null && (tournName != null || rawKey != null)) {
                String nameForExtraction = tournName != null ? tournName : rawKey;
                var extracted = CountryNormalizer.extractCountryFromTournament(nameForExtraction);
                if (extracted != null && !extracted.isInternational()) {
                    expectedCountry = resolveCountryEntity(extracted.slug(), false);
                }
            }
            if (expectedCountry != null && integrityValidator != null
                    && !integrityValidator.isUniversalPseudoCountry(expectedCountry.getCanonicalName(), expectedCountry.getIsoCode())
                    && !integrityValidator.isUniversalPseudoCountry(mappedTourn.getCountry().getCanonicalName(), mappedTourn.getCountry().getIsoCode())
                    && !integrityValidator.isCountryCompatible(mappedTourn.getCountry(), expectedCountry)) {
                log.warn("🛡️ [MDM-GUARD] Bypassing corrupt historical tournament mapping: rawKey='{}' (expected country='{}' #{}) -> tournament #{} '{}' (mapped country='{}' #{})",
                        trimmed, expectedCountry.getCanonicalName(), expectedCountry.getId(),
                        mappedTourn.getId(), mappedTourn.getCanonicalName(),
                        mappedTourn.getCountry().getCanonicalName(), mappedTourn.getCountry().getId());
                return false;
            }
        }

        if (!isLexicallyCompatible(trimmed, mappedTourn.getCanonicalName())) {
            log.warn("🛡️ [MDM-GUARD] Bypassing corrupt historical tournament mapping: rawKey='{}' -> tournament #{} '{}' (zero lexical similarity)",
                    trimmed, mappedTourn.getId(), mappedTourn.getCanonicalName());
            return false;
        }
        return true;
    }


    public void saveTournamentIngestionMap(String systemCode, String rawKey, MdmTournament tourney) {
        saveTournamentIngestionMap(systemCode, rawKey, null, tourney);
    }

    public void saveTournamentIngestionMap(String systemCode, String rawKey, String sourceId, MdmTournament tourney) {
        if (systemCode == null || systemCode.isBlank() || rawKey == null || rawKey.isBlank() || tourney == null) return;
        if (externalSystemRepository == null || tournamentIngestionMapRepository == null) return;

        String trimmed = rawKey.trim();
        // 1. Guard against generic stage or format names
        if (GENERIC_STAGE_OR_FORMAT_PATTERN.matcher(trimmed).matches()) {
            log.warn("🛡️ [MDM-GUARD] Refusing to map generic stage/format '{}' to tournament #{} '{}'",
                    trimmed, tourney.getId(), tourney.getCanonicalName());
            return;
        }

        // 2. Guard against cross-gender tournament mapping
        boolean rawIsFemale = TeamSignatureNormalizer.isFemaleContext(trimmed);
        String tourneyGender = normalizeGender(tourney.getGender());
        if (rawIsFemale && "MALE".equalsIgnoreCase(tourneyGender)) {
            log.warn("🛡️ [MDM-GUARD] Refusing to map female rawKey '{}' to MALE tournament #{} '{}'",
                    trimmed, tourney.getId(), tourney.getCanonicalName());
            return;
        }

        // 3. Guard against zero lexical overlap
        if (!isLexicallyCompatible(trimmed, tourney.getCanonicalName())) {
            log.warn("🛡️ [MDM-GUARD] Refusing to map disparate tournament rawKey '{}' to tournament #{} '{}' (zero lexical similarity)",
                    trimmed, tourney.getId(), tourney.getCanonicalName());
            return;
        }

        // 4. Guard against country mismatch
        if (tourney.getCountry() != null && !Boolean.TRUE.equals(tourney.getIsCup())
                && (integrityValidator == null || !integrityValidator.isInternationalTournament(tourney))) {
            var extracted = CountryNormalizer.extractCountryFromTournament(trimmed);
            if (extracted != null && !extracted.isInternational() && countryRepository != null) {
                MdmCountry c = resolveCountryEntity(extracted.slug(), false);
                if (c != null && integrityValidator != null
                        && !integrityValidator.isUniversalPseudoCountry(c.getCanonicalName(), c.getIsoCode())
                        && !integrityValidator.isUniversalPseudoCountry(tourney.getCountry().getCanonicalName(), tourney.getCountry().getIsoCode())
                        && !integrityValidator.isCountryCompatible(tourney.getCountry(), c)) {
                    log.warn("🛡️ [MDM-GUARD] Refusing to map tournament rawKey '{}' (country='{}') to tournament #{} '{}' (country='{}')",
                            trimmed, c.getCanonicalName(), tourney.getId(), tourney.getCanonicalName(), tourney.getCountry().getCanonicalName());
                    return;
                }
            }
        }

        try {
            findExternalSystemByCodeCached(systemCode).ifPresent(sys -> {
                tournamentIngestionMapRepository.upsertMapping(sys.getId(), trimmed, sourceId, tourney.getId(), tourney.getCanonicalName());
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
        String s = input;
        if (hasCyrillic(s)) {
            s = pro.datawiki.igaming.dto.util.SimilarityUtils.transliterateCyrillic(s);
        }
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = normalized.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.isBlank() ? "unknown" : slug;
    }

    public static String sanitizeEntityOrigin(String rawOrigin, Long sofaId) {
        if (sofaId != null && sofaId > 0) {
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

    public static String normalizeGender(String gender) {
        return MatchIntegrityValidator.normalizeGender(gender);
    }

    public static boolean isAgeCompatible(String teamAge, String expectedAge) {
        return MatchIntegrityValidator.isAgeCompatible(teamAge, expectedAge, null);
    }

    public boolean isInternationalTournament(MdmTournament tournament) {
        return integrityValidator.isInternationalTournament(tournament);
    }

    public boolean isCountryCompatible(MdmTournament tournament, MdmTeam team) {
        return integrityValidator.isCountryCompatible(tournament, team);
    }
}
